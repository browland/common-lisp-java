package compiler;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

/**
 * Needs to be lazy due to the need to generate the assembly in the correct order regardless of what order the calls
 * are made.  So we store state about the asm we want to generate and we'll have one method which can dump the asm.
 * So we need a context to hold the state about what we're working on currently - call this AsmContext.
 */
public class AsmGenerator {
    // Current generation context (represents main or a function).
    private AsmContext context;

    public AsmGenerator() {
        context = new AsmContext();
        contextStack.push(context);

        // TODO refactor this
        dataSegmentContext.write(".data\n");
        cstringContext.write(".cstring\n");
    }

    // Probably will only have depth of 2 max?  Usually main context unless we're defining a function when we're 2 deep.
    // We always pop back to get to main context and then push one function at a time while we're compiling it.
    private Deque<AsmContext> contextStack = new LinkedList<>();

    // All function contexts - main, and all user defined functions
    private List<AsmContext> functionContexts = new LinkedList<>();

    private AsmContext dataSegmentContext = new AsmContext();
    private AsmContext cstringContext = new AsmContext();

    public void initMainFunction() {
        context.write(
                """
        .text
        .global _main
        _main:
        .p2align 3
          stp x29, x30, [sp, #-16]!  ;; allocate stack and store frame pointer and link register
          mov x29, sp                ;; set frame pointer
          bl _init                   ;; init symbol table etc
        """);
    }

    public void initFunction(String name) {
        context.write("""
  .p2align 3                 ;; Align to 8 bytes so we can use our low 3 bit tagging scheme
  _%s:
  stp x29, x30, [sp, #-16]!  ;; allocate stack and store frame pointer and link register
  mov x29, sp                ;; set frame pointer
""".formatted(name));

    }

    public void endFunction() {
        context.write("""
  add sp, sp, #16            ;; free stack space for saved FP, LR
  ldp x29, x30, [x29]
  ret
""");

    }

    public void printResultAndCleanUpMainFunction() {
        context.write("""
  bl _printResult
  ldp x29, x30, [x29]
  add sp, sp, #16
  ret
""");
    }

    public void reserveSpaceOnStack(int stackBytes) {
        context.write("""
    sub sp, sp, #%d           ;; reserve space for operands
  """.formatted(stackBytes));
    }

    /**
     * pos starts from 0 and is used to determine the register
     */
    public void writeLongToRegister(int regNum, long fixNum) {
        context.write("""
    mov x%d, #%d          ;; move operand (fixnum) to provided register
  """.formatted(regNum, fixNum));
    }

    public void storeResultToStack(int operandNum) {
        context.write("""
  str x0, [sp, #%d]  ;; store result from x0 to stack to free x0 for further operand processing
""".formatted(operandNum*8));
    }

    /**
     * operandNum starts from 0
     * Deprecated as it's too opaque how this works.  Also we use the sp which moves around, rather than the fp.
     */
    @Deprecated
    public void storeOperandFromRegisterToStack(int pos) {
        context.write("""
      str x%d, [sp, #%d]   ;; load evaluated operand into register ready for operator call
    """.formatted(pos, pos*8));
    }

    /**
     * operandNum starts from 0
     */
    public void storeOperandFromRegisterToStack(int regNum, int framePointerOffset) {
        context.write("""
      str x%d, [x29, #%d]   ;; load evaluated operand into register ready for operator call
    """.formatted(regNum, framePointerOffset));
    }

    /**
     * operandNum starts from 0
     */
    public void loadOperandFromStackIntoRegister(int stackPos, int regNum) {
        context.write("""
      ldr x%d, [sp, #%d]   ;; load evaluated operand into register ready for operator call
    """.formatted(regNum, stackPos*8));
    }

    /**
     */
    public void loadOperandFromStackOffsetIntoRegister(int stackOffset, int regNum) {
        context.write("""
      ldr x%d, [fp, #%d]   ;; load binding from stack into register
    """.formatted(regNum, stackOffset));
    }

    public void untagFunctionPtr() {
        context.write("""
      bl _untag_ptr
    """);
    }

    public void callFunction(int functionPtrRegister) {
        context.write("""
      blr x%d
    """.formatted(functionPtrRegister));
    }

    public void freeSpaceOnStack(int stackBytes) {
        context.write("""
                  add sp, sp, #%d
                """.formatted(stackBytes));
    }

    public void generateSymbolLookup(String symbolName, Namespace namespace) {
        //      For each defined symbol we'll have a global with a well-defined name.  So far we're calling them t_symbol_ptr and nil_symbol_ptr and so on
        //      The built-in symbols will be defined in runtime.c and any user-defined ones will be added to the .cstring section of generated asm.
        //      E.g. for a user-defined symbol myvar, we'd generate a 'quad' data with name myvar_symbol_ptr, we'd strdup the char* symbol name so it's on the
        //      heap, we'd then store that pointer with the appropriate tagged bits in myvar_symbol_ptr.
        //      So for lookup, we can reference the pointer name by its well-defined name, get its value, check its type, remove the tag bits, dereference it, 
        //      and that's the symbol.  Its symbol table entry would use the same tagged pointer.

        // Generate the well-defined name of the runtime symbol holding the tagged pointer
        String symPointerName = "_" + symbolName + "_sym";
        int offset = namespace == Namespace.VARIABLE ? 8 : 16;
        context.write("""
  adrp x0, %s@PAGE                     ; get page of tagged symbol pointer variable
  add x0, x0, %s@PAGEOFF               ; add offset of tagged symbol pointer variable so x0 contains its address
  ldr x0, [x0, #%d]                    ; dereference pointer so we return (in x0) the actual tagged (symbol) pointer
""".formatted(symPointerName, symPointerName, offset));
    }

    public String addToSymbolTable(String symbolName) {
        // Generate the well-defined name of the runtime symbol holding the tagged pointer
        String symPointerName = "_" + symbolName + "_sym";
        String strPointerName = "_" + symbolName + "_str";

        switchToDataSegmentContext();
        context.write("""
.p2align 3
%s:
    .quad %s
    .quad 0
    .quad 0
                """.formatted(symPointerName, strPointerName));
        switchFromDataSegmentContext();

        switchToCStringContext();
        context.write(strPointerName + ":\n");
        context.write("  .asciz \"" + symbolName + "\"\n");
        switchFromCStringContext();
        return strPointerName;
    }

    public void generateTypeCheckForSymbol() {
        context.write("""
  bl _typecheck_symbol
""");
    }

    public void generateCheckForT() {
        context.write("""
  bl _is_t
""");
    }

    public void generateJumpInstructionForNonZeroReturnValue(String jumpLabel) {
        context.write("""
  cbnz x0, %s
""".formatted(jumpLabel));
    }

    public void generateUnconditionalJump(String jumpLabel) {
        context.write("""
  b %s
""".formatted(jumpLabel));
    }

    public void generateLabel(String label) {
        context.write("""
                .global %s
                %s:
                """.formatted(label, label));
    }

    public void dumpAsm(BufferedWriter bw) throws IOException {
        context.dumpAsm(bw);
        for (AsmContext functionContext : functionContexts) {
            functionContext.dumpAsm(bw);
        }
        dataSegmentContext.dumpAsm(bw);
        cstringContext.dumpAsm(bw);
        bw.close();
    }

    public void startFunctionDef() {
        AsmContext functionContext = new AsmContext();
        contextStack.push(functionContext);
        functionContexts.add(functionContext);
        context = functionContext;
    }

    public void endFunctionDef() {
        contextStack.pop();
        context = contextStack.peek();
    }

    public void switchToDataSegmentContext() {
        contextStack.push(dataSegmentContext);
        context = dataSegmentContext;
    }

    public void switchFromDataSegmentContext() {
        contextStack.pop();
        context = contextStack.peek();
    }

    public void switchToCStringContext() {
        contextStack.push(cstringContext);
        context = cstringContext;
    }

    public void switchFromCStringContext() {
        contextStack.pop();
        context = contextStack.peek();
    }

    public void putFunction(String name) {
        // Can't (?) call putFunction in C as we have the dynamically-generated name of the symbol but not obvious to me
        // how to reach it by calling into C.
        String symbolPointerName = "_" + name + "_sym";
        String functionLabel = "_" + name;
        context.write("""
  adrp x0, %s@PAGE
  add x0, x0, %s@PAGEOFF
  adrp x1, %s@PAGE
  add x1, x1, %s@PAGEOFF
  orr x1, x1, #0x2           ; tag the function ptr
  str x1, [x0, #16]          ; store function ptr to function slot for the symbol
""".formatted(symbolPointerName, symbolPointerName, functionLabel, functionLabel));
    }

    public void mkCaptures(int capturesLen) {
        context.write("""
  mov x0, #%d                ; set up capturesLen arg
  bl _alloc_captures         ; this leaves our captures array ptr in x0
  """.formatted(capturesLen));
    }

    public void addCapture(int sourceFramePointerOffset, int captureIndex) {
        context.write("""
                ldr x1, [x29, #%d]
                mov x2, #%d
                bl _add_capture
                """.formatted(sourceFramePointerOffset, captureIndex));
    }

    public void putClosure(String name) {
        // We'll generate tagged fxn ptr, we only expect captures array in x0
        // Just like putFunction() except we tag with 0x3L
        String functionLabel = "_" + name;
        context.write("""
  ;;; xl should be ptr to captures array (already in x0 from last addCapture() or mkCaptures())
  mov x1, x0
  ;;; x0 should be ptr to actual fxn code
  adrp x0, %s@PAGE           ; set up real fxn ptr in x0
  add x0, x0, %s@PAGEOFF     ; ...
  ;orr x0, x0, #0x3           ; tag real fxn ptr
  
  bl _mk_closure
  
""".formatted(functionLabel, functionLabel));
    }

    public void writeRegisterToSymbolValue(int registerNum, String symbolValue) {
        // put value to symbol table into variable namespace
        // we use x8 for our own internal work to avoid clobbering x0 to x7 in which we assume our values may be
        String symbolPointerName = "_" + symbolValue + "_sym";
        // TODO hardcoded to offset 8 which is the value slot for the symbol; needs namespace passing in
        context.write("""
  adrp x8, %s@PAGE
  add x8, x8, %s@PAGEOFF
  str x%d, [x8, #8]
""".formatted(symbolPointerName, symbolPointerName, registerNum));
    }

    public void writeComment(String comment) {
        context.write(";" + comment);
    }

    public void loadCapturedVariable(int captureIndex, int closurePtrFPOffset) {
        // Load closure ptr from stack into x0; capture index goes into x1
        context.write("""
                ldr x0, [x29, #%d]
                mov x1, #%d
                bl _load_captured_variable
                """.formatted(closurePtrFPOffset, captureIndex));
    }

    public void closureToFunctionPtr() {
        context.write("""
                bl _tagged_closure_ptr_to_fxn_ptr
                """);
    }

    public void moveStackPointerToRegister(int offsetFromStackPointer, int regNum) {
        context.write("""
                add x%d, sp, #%d
                """.formatted(regNum, offsetFromStackPointer));
    }
}

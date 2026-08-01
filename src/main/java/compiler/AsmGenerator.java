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

    // TODO keeping this around for code for string literals
    public void generateGlobals() {
        // Anon. string literals, will be needed when we use string literals in code
        //String myAsm = "";
        //List<StringLiteral> stringLiterals = context.getStringLiterals();
        //if (! stringLiterals.isEmpty()) {
        //    myAsm += ".cstring\n";
        //    for (StringLiteral stringLiteral : stringLiterals) {
        //        myAsm += stringLiteral.name() + ":\n";
        //        myAsm += "  .asciz \"" + stringLiteral.value() + "\"\n";
        //    }
        //}

        //// Static strings for things like messages
        //myAsm += """
//_error_msg:
//  .asciz \"ERROR (incorrect value type)\\n\"""";

        context.write("""
.data
.p2align 3
""");
        List<String> taggedSymbolNames = context.getTaggedSymbolNames();
        for (String taggedSymbolName : taggedSymbolNames) {
            context.write(""" 
%s:
    .quad 0
""".formatted(taggedSymbolName));
        }
    }

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
     * pos starts from 0 and is used to determine the stack offset
     */
    public void pushFixNumToStack(int pos, long fixNum) {
        context.write("""
    mov x0, #%d          ;; move operand (fixnum) to x0
    str x0, [sp, #%d]  ;; store fixnum on stack to free x0 for further operand processing
  """.formatted(fixNum, pos*8));
    }

    /**
     * pos starts from 0 and is used to determine the register
     */
    public void writeFixNumToRegister(int pos, long fixNum) {
        context.write("""
    mov x%d, #%d          ;; move operand (fixnum) to provided register
  """.formatted(pos, fixNum));
    }

    public void storeResultToStack(int operandNum) {
        context.write("""
  str x0, [sp, #%d]  ;; store result from x0 to stack to free x0 for further operand processing
""".formatted(operandNum*8));
    }

    /**
     * operandNum starts from 0
     */
    public void storeOperandFromRegisterToStack(int pos) {
        context.write("""
      str x%d, [sp, #%d]   ;; load evaluated operand into register ready for operator call
    """.formatted(pos, pos*8));
    }

    /**
     * operandNum starts from 0
     */
    public void loadOperandFromStackIntoRegister(int stackPos, int regNum) {
        context.write("""
      ldr x%d, [sp, #%d]   ;; load evaluated operand into register ready for operator call
    """.formatted(regNum, stackPos*8));
    }

    public void loadFunctionPtr() {
        // TODO hardcoded to `add` for now
        context.write("""
      bl _get_add_function_ptr
    """);
    }

    public void untagFunctionPtr() {
        context.write("""
      bl _untag_fxn_ptr
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

    public void generateLoadSymbolTaggedPtr(String symbolName) {
        //      For each defined symbol we'll have a global with a well-defined name.  So far we're calling them t_symbol_ptr and nil_symbol_ptr and so on
        //      The built-in symbols will be defined in runtime.c and any user-defined ones will be added to the .cstring section of generated asm.
        //      E.g. for a user-defined symbol myvar, we'd generate a 'quad' data with name myvar_symbol_ptr, we'd strdup the char* symbol name so it's on the
        //      heap, we'd then store that pointer with the appropriate tagged bits in myvar_symbol_ptr.
        //      So for lookup, we can reference the pointer name by its well-defined name, get its value, check its type, remove the tag bits, dereference it, 
        //      and that's the symbol.  Its symbol table entry would use the same tagged pointer.

        // Generate the well-defined name of the runtime symbol holding the tagged pointer
        String symPointerName = "_" + symbolName + "_symbol_ptr";
        context.write("""
  adrp x0, %s@PAGE                     ; get page of tagged symbol pointer variable
  add x0, x0, %s@PAGEOFF               ; add offset of tagged symbol pointer variable so x0 contains its address
  ldr x0, [x0]                         ; dereference pointer so we return (in x0) the actual tagged (symbol) pointer
""".formatted(symPointerName, symPointerName));
    }

    public void generateSymbolLookup(String symbolName, Namespace namespace) {
        //      For each defined symbol we'll have a global with a well-defined name.  So far we're calling them t_symbol_ptr and nil_symbol_ptr and so on
        //      The built-in symbols will be defined in runtime.c and any user-defined ones will be added to the .cstring section of generated asm.
        //      E.g. for a user-defined symbol myvar, we'd generate a 'quad' data with name myvar_symbol_ptr, we'd strdup the char* symbol name so it's on the
        //      heap, we'd then store that pointer with the appropriate tagged bits in myvar_symbol_ptr.
        //      So for lookup, we can reference the pointer name by its well-defined name, get its value, check its type, remove the tag bits, dereference it, 
        //      and that's the symbol.  Its symbol table entry would use the same tagged pointer.

        // Generate the well-defined name of the runtime symbol holding the tagged pointer
        String symPointerName = "_" + symbolName + "_symbol_ptr";
        int namespaceId = namespace.getIdentifier();
        context.write("""
  adrp x0, %s@PAGE                     ; get page of tagged symbol pointer variable
  add x0, x0, %s@PAGEOFF               ; add offset of tagged symbol pointer variable so x0 contains its address
  ldr x0, [x0]                         ; dereference pointer so we return (in x0) the actual tagged (symbol) pointer
  mov x1, %d                           ; indicate which namespace
  bl _evaluate_symbol                  ; look up value of this symbol in variable namespace
""".formatted(symPointerName, symPointerName, namespaceId));
    }

    public void generateDataSectionQuadWordForSymbolPtr(String symbolName) {
        // Generate the well-defined name of the runtime symbol holding the tagged pointer
        String symPointerName = "_" + symbolName + "_symbol_ptr";

        switchToDataSegmentContext();
        context.write("""
%s:
    .quad 0
                """.formatted(symPointerName));
        switchFromDataSegmentContext();
    }

    // Returns name of pointer to symbol name cstring
    public String generateCStringForSymbol(String symbolName) {
        // Generate the well-defined name of the runtime symbol holding the tagged pointer
        String symPointerName = "_" + symbolName + "_symbol_ptr";

        switchToCStringContext();
        context.write(symPointerName + ":\n");
        context.write("  .asciz \"" + symbolName + "\"\n");
        switchFromCStringContext();
        return symPointerName;
    }

    public void generateSymbolExists() {
        // Similar to generateSymbolLookup() but allows for case where symbol is not in the table yet, e.g. for defvar to determine whether it's the first
        // time we've encountered defvar for this symbol (else no-op).
        context.write("""
  bl _symbol_exists                    ; look up value of this symbol in variable namespace; else NULL
""");
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

    public void write(String asm) {
        context.write(asm);
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
        String symbolPointerName = "_" + name + "_symbol_ptr";
        String functionLabel = "_" + name;
        context.write("""
  adrp x0, %s@PAGE
  add x0, x0, %s@PAGEOFF
  adrp x1, %s@PAGE
  add x1, x1, %s@PAGEOFF
  bl _put_function
""".formatted(symbolPointerName, symbolPointerName, functionLabel, functionLabel));
    }

    // Expects the tagged value ptr already in x1
    public void putSymbol(String symbolName) {
        // put value to symbol table into variable namespace
        // we use x8 for our own internal work to avoid clobbering x0 to x7 in which we assume our values may be
        String symbolPointerName = "_" + symbolName + "_symbol_ptr";
        context.write("""
  adrp x0, %s@PAGE
  add x0, x0, %s@PAGEOFF
  bl _put_symbol
""".formatted(symbolPointerName, symbolPointerName));
    }
}

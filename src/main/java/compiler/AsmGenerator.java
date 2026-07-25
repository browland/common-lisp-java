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
    private AsmContext context = new AsmContext();
    private Deque<AsmContext> asmContexts = new LinkedList<>();
    // TODO generated functions
//    private static Map<String,

    public AsmGenerator() {
        asmContexts.push(context);
    }

    public void generateGlobals() throws IOException {
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

    public void initMainFunction() throws IOException {
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

    public void printResultAndCleanUpMainFunction() throws IOException {
        context.write("""
  bl _printResult
  ldp x29, x30, [x29]
  add sp, sp, #16
  ret
""");
    }

    public void reserveSpaceOnStack(int stackBytes) throws IOException {
        context.write("""
    sub sp, sp, #%d           ;; reserve space for operands
  """.formatted(stackBytes));
    }

    /**
     * pos starts from 0 and is used to determine the stack offset
     */
    public void pushFixNumToStack(int pos, long fixNum) throws IOException {
        context.write("""
    mov x0, #%d          ;; move operand (fixnum) to x0
    str x0, [sp, #%d]  ;; store fixnum on stack to free x0 for further operand processing
  """.formatted(fixNum, pos*8));
    }

    public void storeResultToStack(int operandNum) throws IOException {
        context.write("""
  str x0, [sp, #%d]  ;; store fixnum on stack to free x0 for further operand processing
""".formatted(operandNum*8));
    }

    /**
     * operandNum starts from 0
     */
    public void loadOperandFromStackIntoRegister(int operandNum) throws IOException {
        context.write("""
      ldr x%d, [sp, #%d]   ;; load evaluated operand into register ready for operator call
    """.formatted(operandNum, operandNum*8));
    }

    public void callFunction(OperatorName operatorName)  throws  IOException {
        context.write("""
      bl %s                ;; call operator; this leaves the result in x0 for our caller
    """.formatted(operatorName.getAsmName()));
    }

    public void freeSpaceOnStack(int stackBytes) throws IOException {
        context.write("""
                  add sp, sp, #%d
                """.formatted(stackBytes));
    }

    public void generateLoadSymbolTaggedPtr(String symbolName) throws IOException {
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

    public void generateSymbolLookup(String symbolName) throws IOException {
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
  bl _evaluate_symbol                  ; look up value of this symbol in variable namespace
""".formatted(symPointerName, symPointerName));
    }

    public void generateTaggedSymbolName(String symbolName) throws IOException{
        // Generate the well-defined name of the runtime symbol holding the tagged pointer
        String symPointerName = "_" + symbolName + "_symbol_ptr";

        context.addTaggedSymbolName(symPointerName);
    }

    public void generateSymbolExists() throws IOException {
        // Similar to generateSymbolLookup() but allows for case where symbol is not in the table yet, e.g. for defvar to determine whether it's the first
        // time we've encountered defvar for this symbol (else no-op).
        context.write("""
  bl _symbol_exists                    ; look up value of this symbol in variable namespace; else NULL
""");
    }

    public void generateTypeCheckForSymbol() throws IOException {
        context.write("""
                bl _typecheck_symbol
                """);
    }

    public void generateCheckForT() throws IOException {
        context.write("""
                bl _is_t
                """);
    }

    public void generateJumpInstructionForNonZeroReturnValue(String jumpLabel) throws IOException {
        context.write("""
                cbnz x0, %s
                """.formatted(jumpLabel));
    }

    public void generateUnconditionalJump(String jumpLabel) throws IOException {
        context.write("""
                b %s
                """.formatted(jumpLabel));
    }

    public void generateLabel(String label) throws IOException {
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
    }
}

package compiler;

public class AddAsm implements FormAsm {
    @Override
    public String generate(Form form) {
        // Check type of operands; are they "fixnum"s, or regular integers which fit into a 64 bit register minus 3 bits
        // (for tagged pointer)
        // x0 and x1 will have our operands
        // TODO we're at the point where we need a better form of string templating than this
        String asmFxnName = form.getAsmFunctionName();
        String asm = """
                mov x2, #0x7    ; bit mask to select out the low 3 bits only
                and x3, x0, x2  ; store result of mask in x3
                sub x3, x3, #1  ; check that the masked bits are equal to 1
                cbnz x3, %s
            %s:
                add x0, x0, x1
                ret
            
            %s:
                adrp x0, _error_msg@PAGE
                add x0, x0, _error_msg@PAGEOFF
                bl _printf
                bl _exit
                """.formatted(asmFxnName + "_no_match", asmFxnName + "_match", asmFxnName + "_no_match");


        return asm + "add x0, x0, x1\n";
    }
}

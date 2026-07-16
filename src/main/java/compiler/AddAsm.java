package compiler;

public class AddAsm implements FormAsm {
    @Override
    public String generate(Form form) {
        // TODO we're at the point where we need a better form of string templating than this
        String asmFxnName = form.getAsmFunctionName();
        String noMatchLabel = asmFxnName + "_no_match";
        String matchLabel = asmFxnName + "_match";
        String exitLabel = asmFxnName + "_exit";

        return """
  mov x2, #0x7    ; bit mask to select out the low 3 bits only
  and x3, x0, x2  ; store result of masking x0 in x3
  sub x3, x3, #1  ; check that the masked bits are equal to 1
  cbnz x3, %s
            
  and x3, x1, x2  ; store result of masking x0 in x3
  sub x3, x3, #1  ; check that the masked bits are equal to 1
  cbnz x3, %s
%s:  ;; match
  lsr x0, x0, #3  ; logical shift right x0 to remove type tag bits
  lsr x1, x1, #3  ; logical shift right x1 to remove type tag bits
  add x0, x0, x1
                
  lsl x0, x0, #3
  orr x0, x0, #0x1
  b %s
%s:  ;; no match
  adrp x0, _error_msg@PAGE
  add x0, x0, _error_msg@PAGEOFF
  bl _printf
  bl _exit
""".formatted(noMatchLabel, noMatchLabel, matchLabel, exitLabel, noMatchLabel);
    }
}

; try to implement loop until done do ...
; we'll need splicing, as otherwise we have a list of forms and can't otherwise flatten the list to evaluate each one
; apart from this, we just need essentially an infinite loop around the body forms, but with checking to see if each
; form is the 'codeword' (in this case "done") and in that case we return-from the block.  Use tagbody to tag the top
; of the loop for jumping back to the start of the forms each time.
(defmacro myloop (arg1 arg2 arg3 &rest body_forms)
  (block loop_block
    (tagbody



  `,body_forms
)

; we just want this emitted: (format t "hello")

(macroexpand-1 '(myloop until done do (format t "hello") (format t "hello")))

(myloop until done do (format t "hello") (format t "hello"))

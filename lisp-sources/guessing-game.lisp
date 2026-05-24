; try to implement loop until done do ...
; we'll need splicing, as otherwise we have a list of forms and can't otherwise flatten the list to evaluate each one
; apart from this, we just need essentially an infinite loop around the body forms, but with checking to see if each
; form is the 'codeword' (in this case "done") and in that case we return-from the block.  Use tagbody to tag the top
; of the loop for jumping back to the start of the forms each time.
(defmacro myloop (arg1 done_var arg3 &rest body_forms)
  `(block loop_block
    (tagbody
      start
      ,@body_forms
      (if ,done_var (return-from loop_block))
      (go start)
    )
  )
)

(let ((done nil)
      (i 0))

    (myloop until done do
      (setq i (+ i 1))
      (format t i)
      (if (= i 10) (setq done t))))

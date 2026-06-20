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

; initialise our variables in a lexical scope
(let ((curr 1)
      (prev 0)
      (temp 0)
      (done nil))

  ; print the first two values before entering the loop
  (format t "~S" prev)
  (format t "~S" curr)

  ; loop through the fibonacci series ...
  (myloop until done do
    ; set our variables
    (setq temp prev)
    (setq prev curr)
    (setq curr (+ curr temp))

    ; print the new value for this loop iteration
    (format t "~S" curr)

    ; check if we've reached our upper bound; if so set our loop termination variable
    (if (> curr 100)
      (setq done t))))


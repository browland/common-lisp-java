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

(let ((curr 1)
      (prev 0)
      (temp 0)
      (done nil))
  (format t "~S" prev)
  (format t "~S" curr)
  (myloop until done do
    (setq temp prev)
    (setq prev curr)
    (setq curr (+ curr temp))
    (format t "~S" curr)
    (if (> curr 100)
      (setq done t))))


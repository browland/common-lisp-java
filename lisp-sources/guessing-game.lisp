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

(defun guess-game ()
  (let ((target (+ 1 (random 100)))
        (guesses 0)
        (done nil))
    (format t "Guess a number between 1 and 100")

    (myloop until done do
      (format t "Your guess: ")
      (let ((guess (read)))
        (incf guesses)

        (cond
          ((< guess target)
           (format t "Too low"))

          ((> guess target)
           (format t "Too high"))

          (t
           (format t "Correct!")
           (setq done t)))))))

(guess-game)
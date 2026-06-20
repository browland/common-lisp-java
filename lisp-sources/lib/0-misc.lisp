(defmacro incf (sym)
   `(setq ,sym (+ ,sym 1)))

(let ((foo 1))
    (incf foo)
    foo)

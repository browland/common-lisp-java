(defmacro incf (sym)
   `(setq ,sym (+ ,sym 1)))

(defvar *poo* 1)
*poo*
(incf *poo*)
*poo*

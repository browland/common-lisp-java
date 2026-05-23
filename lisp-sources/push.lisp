(defmacro push (place head)
  `(setq ,place (cons ,head ,place)))

(defvar *x* '(1 2))
(push *x* 0)
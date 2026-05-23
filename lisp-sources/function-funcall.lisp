(defvar x (function add))
(funcall x 1 1)

(defvar y (lambda (x) (+ x 1)))
(funcall y 5)

(defvar z #'add)
(funcall z 1 1)
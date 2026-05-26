; simple debug
(defvar mylist '(1 2 3))
(let ((fxn #'evenp)
      (resultingcons nil))
  (defvar predicate (lambda (x y)
    (if (funcall fxn x) (format t "true") (format t "false"))))
  (format t (funcall predicate 1 nil)))

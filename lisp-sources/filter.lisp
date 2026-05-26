; https://cs.stanford.edu/people/nick/compdocs/LISP_Examples.pdf
(defmacro mydolist (mylist func)
  `(let ((curr nil)
        (rest ,mylist)
        (result nil))
    (block myloop
      (tagbody
        start
        (setq curr (car rest))
        (setq result (funcall ,func curr result))
        (setq rest (cdr rest))
        (if (eq rest nil) (return-from myloop (funcall ,func curr result)))
        (go start)))))

; this is all very janky but working - needs improving
(defvar mylist '(1 2 3))
(let ((fxn #'evenp)
      (resultingcons nil))
  (defvar predicate (lambda (x y)
    (if (and x (funcall fxn x)) (setq resultingcons (cons x resultingcons)) (setq resultingcons resultingcons))))
  (mydolist mylist predicate)
  (format t resultingcons))


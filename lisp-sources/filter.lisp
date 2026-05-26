; https://cs.stanford.edu/people/nick/compdocs/LISP_Examples.pdf
(defmacro myfilter (mylist func)
  `(let ((curr nil)
        (rest ,mylist)
        (result nil))
    (block myloop
      (tagbody
        start
        (setq curr (car rest))
        (if (funcall ,func curr) (setq result (cons curr result)))
        (setq rest (cdr rest))
        (if (eq rest nil) (return-from myloop result))
        (go start)))))

(defvar mylist '(1 2 3))
(format t (myfilter mylist #'evenp))


(defmacro dolist (mylist, func)
  `(let ((curr 0)
        (rest ,mylist)
        (result 0))
    (block myloop
      (tagbody
        start
        (setq result (funcall ,func curr result))
        (setq curr (car rest))
        (setq rest (cdr rest))
        (if (eq rest nil) (return-from myloop (funcall ,func curr result)))
        (go start)))))

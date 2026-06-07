(defmacro dolist (mylist func)
  `(let ((curr (car ,mylist))
        (rest (cdr ,mylist)))
    (block myloop
      (tagbody
        start
        (funcall ,func curr)
        (setq curr (car rest))
        (setq rest (cdr rest))
        (if (eq rest nil) (return-from myloop (funcall ,func curr)))
        (go start)))))

(defmacro push (item place)
  `(setq ,place (cons ,item ,place)))

(defun reverse (list)
    (let ((newlist nil))
        (dolist list #'(lambda (x) (push x newlist)))))

(defmacro dotimes ((var num) &rest forms)
    `(let ((,var 0))
        (block myloop
            (tagbody
                start
                    (if (eq ,var (- ,num 1))
                        (return-from myloop ,@forms))
                    ,@forms
                    (incf ,var)
                    (go start)))))
(defvar mylist '(1 2))

; Example of how expanded list code could look
(let ((curr (car mylist))
      (rest mylist)
      (func #'+)
      (result 0))
  (block myloop
  (tagbody
    start
    (if (eq rest nil) (return-from myloop result))
    (setq result (funcall func curr result))
    (setq curr (car rest))
    (setq rest (cdr rest))
    (go start))))


; Convert to macro
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

; simpler one for make-cd.lisp which just evaluates the provided form for each element
(defmacro dolist ((loop_var list) &rest loop_form)
  `(let ((remaining ,list))
    (block myloop
      (tagbody
        start
        (setq ,loop_var (car remaining))
        (setq remaining (cdr remaining))
        ,(car loop_form)
        (if (eq remaining nil) (return-from myloop t))
        (go start)))))

(defvar *x* '(1 2 3))
(dolist (myvar *x*) (format t "~S" (add 1 myvar)))

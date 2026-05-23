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
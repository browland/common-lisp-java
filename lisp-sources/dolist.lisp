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
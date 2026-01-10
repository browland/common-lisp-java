(defun print (x)
  (format t x))

(let ((x 1))
  (print x)  ; should print 1
  (let ((x 2))
    (print x)  ; should print 2
    (setq x 3)
    (print x))  ; should print 3
  (print x))  ; should print 1

(defun rotate (start clockwise steps)
  (if clockwise (mod (+ start steps) 100)
    (mod (- start steps) 100)))

;(rotate 50 nil 68)
(mod -18 100)

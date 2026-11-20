(defvar *zeroes* 0)

(defun rotate (start direction steps)
  (let ((newPos 0))
    (if (eq direction :cw)
        (setq newPos (mod (+ start steps) 100))
        (setq newPos (mod (- start steps) 100)))
    (if (eq newPos 0) (incf *zeroes*) t)
    newPos))

#|
L68
L30
R48
L5
R60
L55
L1
L99
R14
L82
|#

(defvar *pos* 50)
(setq *pos* (rotate *pos* :anticw 68))
(setq *pos* (rotate *pos* :anticw 30))
(setq *pos* (rotate *pos* :cw 48))
(setq *pos* (rotate *pos* :anticw 5))
(setq *pos* (rotate *pos* :cw 60))
(setq *pos* (rotate *pos* :anticw 55))
(setq *pos* (rotate *pos* :anticw 1))
(setq *pos* (rotate *pos* :anticw 99))
(setq *pos* (rotate *pos* :cw 14))
(setq *pos* (rotate *pos* :anticw 82))

(format t "resulting position: ~S" *pos*)
(format t "no. of zeroes: ~S" *zeroes*)
(defvar *zeroes* 0)

(defun rotate (start clockwise steps)
  (let ((newPos 0))
    (if clockwise
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
(setq *pos* (rotate *pos* nil 68))
(setq *pos* (rotate *pos* nil 30))
(setq *pos* (rotate *pos* t 48))
(setq *pos* (rotate *pos* nil 5))
(setq *pos* (rotate *pos* t 60))
(setq *pos* (rotate *pos* nil 55))
(setq *pos* (rotate *pos* nil 1))
(setq *pos* (rotate *pos* nil 99))
(setq *pos* (rotate *pos* t 14))
(setq *pos* (rotate *pos* nil 82))

(format t "resulting position: ~S" *pos*)
(format t "no. of zeroes: ~S" *zeroes*)
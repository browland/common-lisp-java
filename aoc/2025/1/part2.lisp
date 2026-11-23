;; Any full rotations must cross through zero one time, regardless of where they started.
;; The remaining problem is the residue; in that case we must look at the start and residue alone; the difference between
;; them tells us if we had a zero crossing, depending on the direction.
;; Start = 50
;; L68
;; calc new pos same way using modulus, to get end index
(defun turn (startPos steps direction)
  (let ((newPos 0))
    (setq newPos
      (if (eq direction :cw)
        (mod (+ startPos steps) 100)
        (mod (- startPos steps) 100)))
    newPos))

;; returns 82; this is the residue
(turn 50 68 :acw)

;; direction nor startPos matter
(defun rotations (steps)
  (floor (/ steps 100)))

(defun zeroCrossing (start residue direction)
  (if (eq direction :cw)
    (< residue start)
    (> residue start)))

(defun consider (start steps direction)
  (let* ((residue (turn start steps direction))
        (fullTurns (rotations steps))
        (residualZeroCrossing (zeroCrossing start residue direction)))
    (if residualZeroCrossing (incf fullTurns))
    fullTurns))

;; also get no. of whole rotations = clicks
(defmacro with-open-file ((sref_var file) &rest forms)
    `(let ((,sref_var (open ,file)))
            ,@forms
            (close ,sref_var)))

(defvar *lines* '())
(defvar *zeroes* 0)
(defvar *pos* 50)

(with-open-file (infile "/Users/ben/git/lisp/aoc/2025/1/test.txt")
  (do ((line nil line) (done nil done))
    ((eq done t) *lines*)
    (setq line (read-line infile nil))
    (if (eq line nil) (setq done t) (push line *lines*))))

(defvar *reversedLines* '())

;; in proper CL:
;; (dolist (x *lines* *reversedLines*) (setq *reversedLines* (cons x *reversedLines*)))
(dolist *lines* #'(lambda (x) (setq *reversedLines* (cons x *reversedLines*))))

(defun extractParts (line)
  (list (subseq line 0 1) (subseq line 1))) 

(defvar *operationPairs* (mapcar #'extractParts *reversedLines*))

;; in CL:
;(dolist (pair *operationPairs* *zeroes*)
;  (let ((direction (if (string= (car pair) "R") :cw :acw))
;        (steps (parse-integer (car (cdr pair)))))
;    (incf *zeroes* (consider *pos* steps direction))
;    (setq *pos* (turn *pos* steps direction))))

(dolist *operationPairs* #'(lambda (pair)
  (let ((direction (if (string= (car pair) "R") :cw :acw))
        (steps (parse-integer (car (cdr pair)))))
    (incf *zeroes* (consider *pos* steps direction))
    (setq *pos* (turn *pos* steps direction)))))

*zeroes*
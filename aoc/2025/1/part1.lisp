(defmacro with-open-file ((sref_var file) &rest forms)
    `(let ((,sref_var (open ,file)))
            ,@forms
            (close ,sref_var)))

(defvar *lines* '())
(defvar *zeroes* 0)
(defvar *pos* 50)

(defun rotate (start direction steps)
  (let ((newPos 0))
    (if (eq direction :cw)
        (setq newPos (mod (+ start steps) 100))
        (setq newPos (mod (- start steps) 100)))
    (if (eq newPos 0) (incf *zeroes*) t)
    newPos))

(with-open-file (infile "/Users/ben/git/lisp/aoc/2025/1/input.txt")
  (do ((line nil line) (done nil done))
    ((eq done t) *lines*)
    (setq line (read-line infile nil))
    (if (eq line nil) (setq done t) (push line *lines*))))

(defvar *reversedLines* '())
(dolist *lines* #'(lambda (x) (setq *reversedLines* (cons x *reversedLines*))))

(defun extractParts (line)
  (list (subseq line 0 1) (subseq line 1))) 

(defvar *operationPairs* (mapcar #'extractParts *reversedLines*))

(dolist *operationPairs* #'(lambda (pair)
  (let ((direction (if (string= (car pair) "R") :cw :acw))
        (steps (parse-integer (car (cdr pair)))))
    (setq *pos* (rotate *pos* direction steps)))))

*zeroes*
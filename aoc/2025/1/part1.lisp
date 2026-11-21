(defmacro with-open-file ((sref_var file) &rest forms)
    `(let ((,sref_var (open ,file)))
            ,@forms
            (close ,sref_var)))

(defvar *lines* nil)

(defun rotate (start clockwise steps)
  (if clockwise (mod (+ start steps) 100)
    (mod (- start steps) 100)))

;(rotate 50 nil 68)
;(- 50 68)

(with-open-file (infile "/Users/ben/git/lisp/aoc/2025/1/test.txt")
  (do ((line nil line) (done nil done))
    ((eq done t) *lines*)
    (setq line (read-line infile nil))
    (if (eq line nil) (setq done t) (push line *lines*))))

;*lines*

(defun extractParts (line) 
  (list (subseq line 0 1) (subseq line 1))) 

(defvar *operationPairs* (mapcar #'extractParts *lines*))
;*operationPairs*

(defvar *test* '((a 1) (b 2)))

;(format t "hello ~S ~S" (car *test*) (car (cdr *test*)))

;(dolist *operationPairs* #'(lambda (pair)
;  (format t "here")
;  (format t "curr pair ~S ~S" (car pair) (car (cdr pair)))))

(dolist *operationPairs* #'(lambda (pair)
  (let ((direction (if (string= (car pair) "R") :cw :acw)))
    (format t "curr pair direction: ~S" direction))))

(defmacro with-open-file ((sref_var file) &rest forms)
    `(let ((,sref_var (open ,file)))
            ,@forms
            (close ,sref_var)))

(defvar *lines* nil)

(defun rotate (start clockwise steps)
  (if clockwise (mod (+ start steps) 100)
    (mod (- start steps) 100)))

;(rotate 50 nil 68)
(- 50 68)

(with-open-file (infile "/Users/ben/git/lisp/aoc/2025/1/test.txt")
  (do ((line nil line) (done nil done))
    ((eq done t) *lines*)
    (setq line (read-line infile nil))
    (if (eq line nil) (setq done t) (push line *lines*))))

*lines*

(defun extractParts (line) 
  (list (subseq line 0 1) (subseq line 1))) 

(defvar *operationPairs* (mapcar #'extractParts *lines*))
*operationPairs*

;(dolist *operationPairs* #'(lambda (pair) (format t "curr pair ~S" (car pair))))
;(dolist *operationPairs* #'(lambda (pair) (+ 1 1)))

(defvar *func* #'(lambda (elem) (format t "curr elem ~S" elem)))

;(dolist (myvar *x*) (format t "~S" (add 1 myvar)))


#|
(let ((curr (car *operationPairs*))
      (rest (cdr *operationPairs*)))
  (format t "top of let, curr: ~S, rest: ~S" curr rest)
  (block myloop
    (tagbody
      start
      (if (eq rest nil)
          (return-from myloop (funcall *func* curr))
          (funcall *func* curr))
      (setq curr (car rest))
      (setq rest (cdr rest))
      (go start))))
|#

; fine
;(format t "hello ~S ~S" 1 2)

; placeholder ix 2 is the fp for format
(let ((a 1) (b 2))
    (format t "hello ~S ~S" a b))

; placeholder ix 1 is something else but still not a tagged val
#|
(let ((a 1))
    (format t "hello ~S" a))
|#


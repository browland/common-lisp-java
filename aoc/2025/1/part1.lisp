(defmacro with-open-file ((sref_var file) &rest forms)
    `(let ((,sref_var (open ,file)))
            ,@forms
            (close ,sref_var)))

(defvar lines nil)

(with-open-file (infile "/Users/ben/git/lisp/aoc/2025/1/input.txt")
  (do ((line nil line) (done nil done))
    ((eq done t) lines)
    (format t "here")
    (setq line (read-line infile nil))
    (if (eq line nil) (setq done t) (push line lines))
    (format t "current line: ~S" line)))

lines

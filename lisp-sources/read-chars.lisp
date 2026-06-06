; assume file exists
(defvar *x* nil)

(with-open-file "/Users/ben/git/lisp/lisp-sources/testfile.txt" mystream
    (push (read-char mystream) *x*)
    (push (read-char mystream) *x*)
    (push (read-char mystream) *x*)
    (push (read-char mystream) *x*)
    (push (read-char mystream) *x*)
    (push (read-char mystream) *x*))

*x*
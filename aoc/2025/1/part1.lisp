(defvar line nil)
(defvar lines nil)

(with-open-file (infile "/Users/ben/git/lisp/aoc/2025/1/test.txt")
    (block file-loop
        (tagbody
            top
            (setq line (read-line infile nil))
            (if (eq line nil)
                (go end))
            (setq lines (cons line lines))
            (go top)
            end)))
lines
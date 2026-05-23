(defun make-cd (title artist rating ripped)
  (list :title title :artist artist :rating rating :ripped ripped))

(getf (make-cd "Roses" "Kathy Mattea" 7 t) :rating)

(defvar *db* nil)

(defmacro push (item place)
  `(setq ,place (cons ,item ,place)))

(defun add-record (cd)
  (push cd *db*))

(add-record (make-cd "Roses" "Kathy Mattea" 7 t))
(defun make-cd (title artist rating ripped)
  (list :title title :artist artist :rating rating :ripped ripped))

(getf (make-cd "Roses" "Kathy Mattea" 7 t) :rating)

(defvar *db* nil)
(defun add-record (cd) (setq *db* (cons cd *db*)))
(add-record (make-cd "Roses" "Kathy Mattea" 7 t))
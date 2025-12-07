(defun make-cd (title artist rating ripped)
  (list :title title :artist artist :rating rating :ripped ripped))

(getf (make-cd "Roses" "Kathy Mattea" 7 t) :rating)
(defvar test (make-cd "Roses" "Kathy Mattea" 7 t))
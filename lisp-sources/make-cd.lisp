(defun make-cd (title artist rating ripped)
  (list :title title :artist artist :rating rating :ripped ripped))

(getf (make-cd "Roses" "Kathy Mattea" 7 t) :rating)

(defvar *db* nil)

(defmacro push (item place)
  `(setq ,place (cons ,item ,place)))

(defun add-record (cd)
  (push cd *db*))

(add-record (make-cd "Roses" "Kathy Mattea" 7 t))

(defmacro dolist ((loop_var list) &rest loop_form)
  `(let ((remaining ,list))
    (block myloop
      (tagbody
        start
        (setq ,loop_var (car remaining))
        (setq remaining (cdr remaining))
        ,(car loop_form)
        (if (eq remaining nil) (return-from myloop t))
        (go start)))))

(defun dump-db ()
  (dolist (cd *db*)
    (format t cd)))

*db*

(dump-db)

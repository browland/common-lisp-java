(defun make-cd (title artist rating ripped)
  (list :title title :artist artist :rating rating :ripped ripped))

(getf (make-cd "Roses" "Kathy Mattea" 7 t) :rating)

(defvar *db* nil)

(defmacro mypush (item place)
  `(setq ,place (cons ,item ,place)))

(defun add-record (cd)
  (mypush cd *db*))

(add-record (make-cd "Roses" "Kathy Mattea" 7 t))

(defmacro mydolist (loop_var list loop_form)
  `(let ((remaining ,list))
    (block myloop
      (tagbody
        start
        (setq ,loop_var (car remaining))
        (setq remaining (cdr remaining))
        ,loop_form
        (if (eq remaining nil) (return-from myloop t))
        (go start)))))

(defun dump-db ()
  (mydolist cd *db*
    (format t cd)))

*db*

(dump-db)

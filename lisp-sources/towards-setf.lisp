(defmacro setf (place value)
  (cond
    ((symbolp place)
     `(setq ,place ,value))

    ((and (listp place)
          (eq (car place) 'car))
     `(rplaca ,(cadr place) ,value))))

(defvar *x* '(1 2))
(setf (car *x*) 2)
*x*
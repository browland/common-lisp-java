(defmacro when (cond &rest forms)
    `(if ,cond
        (progn ,@forms)))

(defmacro unless (cond &rest forms)
    `(if ,cond nil
        (progn ,@forms)))

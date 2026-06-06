(defmacro with-open-file (file sref_var &rest forms)
    `(let ((,sref_var (open ,file)))
            ,@forms
            (close ,sref_var)))

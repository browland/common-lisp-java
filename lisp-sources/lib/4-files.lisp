(defmacro with-open-file (file sref_var &rest forms)
    `(let ((,sref_var (open ,file)))
            ,@forms
            (close ,sref_var)))

; todo implement do, and then implement read-line
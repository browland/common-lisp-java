(defmacro with-open-file (file sref_var &rest forms)
    `(let ((,sref_var (open ,file)))
            ,@forms
            (close ,sref_var)))

; todo implement read-line
;      we need: a coerce function to turn cons list of chars into a string
;               (maybe a stream function to pass stream values around rather than assuming stdin, stdout or always working with files)
;               :eof symbol returned from readchar at eof
;               #\Newline char literal if we don't already have it
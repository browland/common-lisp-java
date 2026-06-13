(defmacro 1+ (sym)
    `(setq ,sym (+ ,sym 1)))

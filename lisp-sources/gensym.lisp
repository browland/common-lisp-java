;; example usages of gensym

(defmacro safe-do-twice (body)
  (let ((var (gensym)))
    `(dotimes (,var 2)
       ,body)))

(macroexpand-1 '(safe-do-twice (format t "hello")))
(safe-do-twice (format t "hello"))


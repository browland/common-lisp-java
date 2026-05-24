; try to implement loop until done do ...
(defmacro myloop (arg1 arg2 arg3 body)
  `,body
)

; we just want this emitted: (format t "hello")

(macroexpand-1 '(myloop until done do (format t "hello")))

(myloop until done do (format t "hello"))

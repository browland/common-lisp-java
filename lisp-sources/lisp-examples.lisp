; https://cs.stanford.edu/people/nick/compdocs/LISP_Examples.pdf

; filter macro (should be a function but my loops are still user-defined macros, so easier to create a filter macro all in one)
(defmacro myfilter (mylist func)
  `(let ((curr nil)
        (rest ,mylist)
        (result nil))
    (block myloop
      (tagbody
        start
        (setq curr (car rest))
        (if (funcall ,func curr) (setq result (cons curr result)))
        (setq rest (cdr rest))
        (if (eq rest nil) (return-from myloop result))
        (go start)))))

(defvar mylist '(1 2 3))
(format t "~S" (myfilter mylist #'evenp))

; Just playing with most for now
(defvar x #'(lambda (x y) (> (* x x) (* y y))))
(funcall x -72 20)
(funcall x 20 -72)
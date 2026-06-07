(defmacro dolist (mylist func)
  `(let ((curr (car ,mylist))
        (rest (cdr ,mylist)))
    (block myloop
      (tagbody
        start
        (funcall ,func curr)
        (setq curr (car rest))
        (setq rest (cdr rest))
        (if (eq rest nil) (return-from myloop (funcall ,func curr)))
        (go start)))))

(defmacro push (item place)
  `(setq ,place (cons ,item ,place)))

(defun reverse (list)
    (let ((newlist nil))
        (dolist list #'(lambda (x) (push x newlist)))))

(defmacro dotimes ((var num) &rest forms)
    `(let ((,var 0))
        (block myloop
            (tagbody
                start
                    (if (eq ,var (- ,num 1))
                        (return-from myloop ,@forms))
                    ,@forms
                    (incf ,var)
                    (go start)))))

; trying to do 'do' - not ready yet
#|
(defmacro do (var-defs (end-test-form result-forms) &rest body)
    `(let ,(mapcar #'(lambda (x) ((car x) (cadr x))) var-defs)
        (format t "~S" foo)))

; test
(macroexpand-1 '(do ((foo 0)) ((eq foo 10) t) t))
(do ((foo 0)) ((eq foo 10) t) t)
|#

(defmacro letter (var-forms)
    `(let (,@var-forms)
        (format t "~S" foo)))

(macroexpand-1 '(letter ((foo 0) (bar 1))))

(defun let-var-extractor (elem)
    (list (car elem) (cadr elem)))

(defvar *thing* '((foo 1 unused) (bar 2 unusedtoo)))
(mapcar #'let-var-extractor *thing*)

(defmacro letter2 (var-forms)
    `(let ,(mapcar #'let-var-extractor var-forms)
        (format t "~S" extracted-var-forms)))

(macroexpand-1 '(letter2 ((foo 1 unused) (bar 2 unusedtoo))))
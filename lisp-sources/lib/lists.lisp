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

; this extracts out the let form from the var forms, and ignores the step function
(defmacro letter2 (var-forms)
    `(let ,(mapcar #'let-var-extractor var-forms)
        (format t "~S" foo)))

(macroexpand-1 '(letter2 ((foo 1 unused) (bar 2 unusedtoo))))

; this goes one step further and adds tagbody/block for looping
(defmacro letter3 (var-forms)
    `(let ,(mapcar #'let-var-extractor var-forms)
        (block myloop
            (tagbody
                start
                (format t "~S" foo)))))

(letter3 ((foo 1 unused) (bar 2 unusedtoo)))

; this goes one step further and passes in the statements
(defmacro letter4 (var-forms end-test-and-result-form statements)
    `(let ,(mapcar #'let-var-extractor var-forms)
        (block myloop
            (tagbody
                start
                (,@statements)))))

(macroexpand-1 '(letter4
    ((foo 1 unused) (bar 2 unusedtoo))   ; var definitions
    ((eq foo 10))                        ; end test form with no result form provided
    (format t "~S" foo)))                 ; statement(s) for each loop

(letter4
    ((foo 1 unused) (bar 2 unusedtoo))   ; var definitions
    ((eq foo 10))                        ; end test form with no result form provided
    (format t "~S" foo))                 ; statement(s) for each loop


; this goes one step further and does looping as we don't have the step forms
(defmacro letter5 (var-forms end-test-and-result-form statements)
    `(let ,(mapcar #'let-var-extractor var-forms)
        (block myloop
            (tagbody
                start
                (if ,(car end-test-and-result-form)
                    (return-from myloop)
                    (go start))
                (,@statements)))))

(macroexpand-1 '(letter5
    ((foo 1 unused) (bar 2 unusedtoo))   ; var definitions
    ((eq foo 10))                        ; end test form with no result form provided
    (format t "~S" foo)))                 ; statement(s) for each loop

;(letter5
;    ((foo 1 unused) (bar 2 unusedtoo))   ; var definitions
;    ((eq foo 10))                        ; end test form with no result form provided
;    (format t "~S" foo))                 ; statement(s) for each loop

; this goes one step further and extracts the step forms
(defun step-forms-extractor (elem)
    (car (cdr (cdr elem))))

(defun step-forms-extractor-new (var-decls)
    (mapcar #'(lambda (decl) (list 'setq (car decl) (car (cdr (cdr decl))))) var-decls))

(defmacro letter6 (var-forms end-test-and-result-form statements)
    `(let (,@(mapcar #'let-var-extractor var-forms))
        (block myloop
            (tagbody
                start
                (if ,(car end-test-and-result-form)
                    (return-from myloop))
                (,@statements)
                ,@(step-forms-extractor-new var-forms)
                (go start)))))

(macroexpand-1 '(letter6
    ((foo 1 (+ 1 foo)) (bar 2 (- 1 bar)))   ; var definitions
    ((eq foo 10))                        ; end test form with no result form provided
    (format t "~S" foo)))                 ; statement(s) for each loop

(letter6
    ((foo 1 (+ 1 foo)) (bar 2 (- 1 bar)))   ; var definitions
    ((eq foo 10))                        ; end test form with no result form provided
    (format t "~S" foo))                 ; statement(s) for each loop

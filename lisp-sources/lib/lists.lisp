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

;;;;;;;;;;;;
;;; do macro
;;;;;;;;;;;;

(defun let-var-extractor (elem)
    (list (car elem) (cadr elem)))

(defun step-forms-extractor (var-decls)
    (mapcar #'(lambda (decl) (list 'setq (car decl) (car (cdr (cdr decl))))) var-decls))

(defmacro do (var-forms end-test-and-result-form &rest optional-statements)
    `(let (,@(mapcar #'let-var-extractor var-forms))
        (block myloop
            (tagbody
                start
                (if ,(car end-test-and-result-form)
                    (return-from myloop ,@(cadr end-test-and-result-form)))
                (if ,@optional-statements ,@(car optional-statements))
                ,@(step-forms-extractor var-forms)
                (go start)))))

; do invocation with all possible parameters
(do ((foo 1 (+ 1 foo)) (bar 2 (- 1 bar)))  ; var declarations
    ((eq foo 10) (t))                      ; end test form with no result form provided
    (format t "~S" foo))                   ; statement(s) for each loop

(defmacro 1+ (sym)
    `(setq ,sym (+ ,sym 1)))

; do invocation with no body (we just have a result form)
; TODO this prints 512 (not in fibonacci) but should print 55!
(do ((n 0 (1+ n))                          ; var declarations
     (cur 0 next)
     (next 1 (+ cur next)))
    ((= 10 n) cur))                        ; end form and result form

; expanded
#|
(let
    ((n 0) (cur 0) (next 1))
    (block myloop
        (tagbody
        start
        (if (= 10 n)
            (return-from myloop cur))
        (if nil nil)
        (setq n (1+ n))
        (setq cur next)
        (setq next (+ cur next))
        (go start))))
|#

(do ((n 0 (1+ n))                          ; var declarations
     (cur 0 next)
     (next 1 (+ cur next)))
    ((= 10 n) cur) (format t "~S ~S ~S" n cur next))                        ; end form and result form

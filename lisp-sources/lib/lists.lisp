(defmacro dolist (mylist func)
  `(let ((curr (car ,mylist))
        (rest (cdr ,mylist)))
    (block myloop
      (tagbody
        start
        (if (eq rest nil)
            (return-from myloop (funcall ,func curr))
            (funcall ,func curr))
        (setq curr (car rest))
        (setq rest (cdr rest))
        ;(funcall ,func curr)  ; only works on side effects
        (go start)))))

(defmacro push (item place)
  `(setq ,place (cons ,item ,place)))

(defun reverse (list)
    (let ((newlist '()))
        (dolist list #'(lambda (x)
            (if x (push x newlist))))
            newlist))

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
    (mapcar #'(lambda (decl)
        (list 'setq (car decl) (car (cdr (cdr decl))))) var-decls))

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

; trying to save step form results in a list and then update vars altogether

; 1. let's just start with a macro which takes an expression and evaluates to the result of it.

(defmacro resulter1 (expr)
    `,expr)

(resulter1 (+ 1 1))

; 2. now pass a list of expressions and return the list of results

(defmacro resulter2 (expr)
    `(list ,@expr))

(resulter2 ((+ 1 1) (- 1 1)))

; 3. now use the proper do signature and return the list of eval'd step forms

(defmacro resulter3 (var-forms end-test-and-result-form &rest optional-statements)
    `(let (,@(mapcar #'let-var-extractor var-forms))
        (let ((temp (list ,@(step-forms-extractor var-forms))))
            temp)))

(resulter3 ((n 0 (1+ n))                          ; var declarations
     (cur 0 next)
     (next 1 (+ cur next)))
    ((= 10 n) cur) (format t "~S ~S ~S" n cur next))                        ; end form and result form

; 4. now store the temp results into the vars

; 4.1 create pop macro
(defmacro pop (place)
    `(progn
        (defvar pop-temp (car ,place))
        (setq ,place (cdr ,place))
        pop-temp))

; quick test of pop
(defvar pop-test '(1 2 3))
(pop pop-test)
(pop pop-test)

(defmacro resulter3 (var-forms end-test-and-result-form &rest optional-statements)
    `(let (,@(mapcar #'let-var-extractor var-forms))
        (let ((temp (list ,@(step-forms-extractor var-forms))))
            (mapcar #'(lambda (var-form) (list 'setq (car var-form) (pop temp))) ',var-forms)
            (format t "~S ~S ~S" n cur next))))

(macroexpand-1 '(resulter3 ((n 0 (1+ n))                          ; var declarations
     (cur 0 next)
     (next 1 (+ cur next)))
    ((= 10 n) cur) (format t "~S ~S ~S" n cur next)))                        ; end form and result form

(resulter3 ((n 0 (1+ n))                          ; var declarations
     (cur 0 next)
     (next 1 (+ cur next)))
    ((= 10 n) cur) (format t "~S ~S ~S" n cur next))                        ; end form and result form

; print before and after
; this is wrong, I'm just doing what I was doing before instead of putting the results into temp

(defmacro resulter4 (var-forms end-test-and-result-form &rest optional-statements)
    `(let (,@(mapcar #'let-var-extractor var-forms))
        (let ((temp (list ,@(step-forms-extractor var-forms))))
            (format t "~S ~S ~S" n cur next)
            (mapcar #'(lambda (var-form) (list 'setq (car var-form) (pop temp))) ',var-forms)
            (format t "~S ~S ~S" n cur next))))

(macroexpand-1 '(resulter4 ((n 0 (1+ n))                          ; var declarations
     (cur 0 next)
     (next 1 (+ cur next)))
    ((= 10 n) cur) (format t "~S ~S ~S" n cur next)))                        ; end form and result form

(resulter4 ((n 0 (1+ n))                          ; var declarations
     (cur 0 next)
     (next 1 (+ cur next)))
    ((= 10 n) cur) (format t "~S ~S ~S" n cur next))                        ; end form and result form

;;;;;;;;
;; this one is correct (prints 55) but for now is just literal quoted code within the macro rather than using operands)
;;;;;;;;

(defmacro my-do (var-decls end-test-and-result-forms &rest statements)
    '(block loop-block
        (let ((i 0)
            (cur 0)
            (next 1)
            (temp-results '()))

            (tagbody
                start
                ; run statements if present

                ; evaluate step forms and push results into temp-results
                (push (1+ i) temp-results)
                (push next temp-results)
                (push (+ cur next) temp-results)

                ; now set vars to results
                (setq temp-results (reverse temp-results))
                (setq i (pop temp-results))
                (setq cur (pop temp-results))
                (setq next (pop temp-results))

                ; evaluate end-test-forms
                (if (= i 10)
                    (return-from loop-block cur)
                    (go start))))))

(my-do (
    (i 0 (+1 i))
    (cur 0 next)
    (next 1 (+ cur next)))
  ((= 10 i) cur))

;; Now let's try to substitute in var decls
;; Need to construct let bindings using first and second element from each list in var-decls
(defvar *res* (mapcar #'let-var-extractor '(
    (i 0 (+1 i))
    (cur 0 next)
    (next 1 (+ cur next)))))

*res*

;; This is close but I need to splice in temp-results into the constructed let bindings

(defmacro my-do (var-decls end-test-and-result-forms &rest statements)
    `(block loop-block
        (let ,(mapcar #'let-var-extractor var-decls)

            (tagbody
                start
                ; run statements if present

                ; evaluate step forms and push results into temp-results
                (push (1+ i) temp-results)
                (push next temp-results)
                (push (+ cur next) temp-results)

                ; now set vars to results
                (setq temp-results (reverse temp-results))
                (setq i (pop temp-results))
                (setq cur (pop temp-results))
                (setq next (pop temp-results))

                ; evaluate end-test-forms
                (if (= i 10)
                    (return-from loop-block cur)
                    (go start))))))

(my-do (
    (i 0 (+1 i))
    (cur 0 next)
    (next 1 (+ cur next)))
  ((= 10 i) cur))


;; Now let's try to substitute in var decls with also a temp-results
;; Need to construct let bindings using first and second element from each list in var-decls
(defvar *res* (mapcar #'let-var-extractor '(
    (i 0 (+1 i))
    (cur 0 next)
    (next 1 (+ cur next)))))

(push '(temp-results ()) *res*)

;; Now let's try that approach

(defmacro my-do (var-decls end-test-and-result-forms &rest statements)
    (defvar *temp-let-bindings* (mapcar #'let-var-extractor var-decls))
    (push '(temp-results ()) *temp-let-bindings*)
    `(let ,*temp-let-bindings*
        (block loop-block
            (tagbody
                start
                ; run statements if present

                ; evaluate step forms and push results into temp-results
                (push (1+ i) temp-results)
                (push next temp-results)
                (push (+ cur next) temp-results)

                ; now set vars to results
                (setq temp-results (reverse temp-results))
                (setq i (pop temp-results))
                (setq cur (pop temp-results))
                (setq next (pop temp-results))

                ; evaluate end-test-forms
                (if (= i 10)
                    (return-from loop-block cur)
                    (go start))))))

(macroexpand-1 '(my-do (
    (i 0 (+1 i))
    (cur 0 next)
    (next 1 (+ cur next)))
  ((= 10 i) cur)))

(my-do (
    (i 0 (+1 i))
    (cur 0 next)
    (next 1 (+ cur next)))
  ((= 10 i) cur))

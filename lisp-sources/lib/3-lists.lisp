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

(defmacro pop (place)
    `(progn
        (defvar pop-temp (car ,place))
        (setq ,place (cdr ,place))
        pop-temp))

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

(defun let-var-extractor (var-decls)
    (mapcar #'(lambda (decl)
        (list (car decl) (cadr decl))) var-decls))

(defun step-forms-extractor (var-decls)
    (mapcar #'(lambda (decl)
        (list 'setq (car decl) (car (cdr (cdr decl))))) var-decls))

(defmacro do (var-decls end-test-and-result-forms &rest statements)
    (let ((temp-let-bindings (let-var-extractor var-decls))
          (temp-step-forms (step-forms-extractor var-decls)))
        (push '(temp-results nil) temp-let-bindings)

        `(let ,temp-let-bindings
            (block loop-block
                (tagbody
                    start
                    ; run statements if present

                    ; evaluate step forms and push results into temp-results so there are no side effects between step forms
                    (push (1+ i) temp-results)
                    (push next temp-results)
                    (push (+ cur next) temp-results)

                    ; now set vars to temp-results
                    (setq temp-results (reverse temp-results))
                    (setq i (pop temp-results))
                    (setq cur (pop temp-results))
                    (setq next (pop temp-results))

                    ; evaluate end-test-forms
                    (if (= i 10)
                        (return-from loop-block cur)
                        (go start)))))))

(macroexpand-1 '(do (
    (i 0 (1+ i))
    (cur 0 next)
    (next 1 (+ cur next)))
  ((= 10 i) cur)))

(do (
    (i 0 (1+ i))
    (cur 0 next)
    (next 1 (+ cur next)))
  ((= 10 i) cur))

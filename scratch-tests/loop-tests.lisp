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

(defun mapcar (fxn the-list)
    (let ((res-list '())
          (acum-fxn #'(lambda (x) (push (funcall fxn x) res-list))))
       (dolist the-list acum-fxn)
       (reverse res-list)))

(defmacro do (var-decls end-test-and-result &rest body-forms)
  (let* ((var-decls-only (mapcar #'(lambda (v) `(,(car v) ,(cadr v))) var-decls))
         (end-test-form (car end-test-and-result))
         (result-form (cadr end-test-and-result))
         (step-forms (mapcar #'(lambda (v) `(,(car v) ,(nth 2 v))) var-decls))
         (var-to-temp-var (mapcar #'(lambda (v) `(,(car v) ,(gensym))) var-decls))
         (temp-var-decls (mapcar #'(lambda (vtv)
                                     (list (cadr vtv)
                                           (cadr (assoc (car vtv) var-decls-only)))) var-to-temp-var)))
    `(let (,@temp-var-decls ,@var-decls-only)
       (block my-loop
              (tagbody
                ;;; evaluate test-form and return result if true (first iteration)
                (if ,end-test-form (return-from my-loop ,result-form))

                ;;; execute body forms (first iteration)
                ,@body-forms

                ;;; start loop tag
                start

                ;;; This is do (not do*) so we update the variables in parallel.  So we generate setq
                ;;; forms to update our gensym'd temp variables first.
                ,@(mapcar #'(lambda (vtv)
                              `(setq ,(cadr vtv) ,(cadr (assoc (car vtv) step-forms)))) var-to-temp-var)

                ;;; now generate setq forms to set loop variables to temp variables
                ,@(mapcar #'(lambda (vtv)
                              `(setq ,(car vtv) ,(cadr vtv))) var-to-temp-var)

                ;;; evaluate test-form
                (if ,end-test-form (return-from my-loop ,result-form))

                ;;; execute body forms
                ,@body-forms

                ;;; jump to start tag for next loop
                (go start))))))

(do ((i 1 (1+ i)))       ; Bind 'i' to 1 initially, increment by 1 each step
    ((> i 5) 'done)      ; Stop when 'i' > 5, then return 'done'
  (format t "Current i: ~S" i))

'(:val1 1 :val2 2 :val3 3)
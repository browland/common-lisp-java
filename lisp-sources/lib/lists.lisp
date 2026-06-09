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

(defmacro do (var-forms end-test-and-result-form statements)
    `(let (,@(mapcar #'let-var-extractor var-forms))
        (block myloop
            (tagbody
                start
                (if ,(car end-test-and-result-form)
                    (return-from myloop ,@(cadr end-test-and-result-form)))
                (,@statements)
                ,@(step-forms-extractor var-forms)
                (go start)))))

(do ((foo 1 (+ 1 foo)) (bar 2 (- 1 bar)))  ; var definitions
    ((eq foo 10) (t))                      ; end test form with no result form provided
    (format t "~S" foo))                   ; statement(s) for each loop

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

(mapcar #'(lambda (x) (+ x 1)) '(1 2 3))

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


(defun nth (n the-list)
  (let ((result nil)
        (count 0))
    (dolist the-list #'(lambda (item) 
                          (if (eq count n) (setq result item)) (incf count)))
    result))

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

                ;;; start loop
                start
                ;;; update temp variables
                ,@(mapcar #'(lambda (vtv) 
                              `(setq ,(cadr vtv) ,(cadr (assoc (car vtv) step-forms)))) var-to-temp-var)

                ;;; set loop variables to temp variables
                ,@(mapcar #'(lambda (vtv) 
                              `(setq ,(car vtv) ,(cadr vtv))) var-to-temp-var)

                ;;; evaluate test-form (within loop)
                (if ,end-test-form (return-from my-loop ,result-form))

                ;;; execute body forms
                ,@body-forms

                ;;; test end-test-form and do next loop if false
                (go start))))))

;; Can do incremental testing in the repl, e.g:
;; (and (load "lib/3-lists.lisp") (test1))

(defun test1 ()
  (do ((i 0 (1+ i)) (cur 0 (1+ cur))) ((eq i 10) 'done) (format t "in body")))

(defun test2 () (do ((temp-one 1 (1+ temp-one))
                     (temp-two 0 (1- temp-two)))
                  ((> (- temp-one temp-two) 5) temp-one)))


(defun test3 () (do ((temp-one 1 (1+ temp-one))
                     (temp-two 0 (1+ temp-one)))     
                  ((= 3 temp-two) temp-one) (format t "In loop body: ~S ~S" temp-one temp-two)))


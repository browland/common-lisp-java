
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

;;; (nth 2 '(1 2 3))


;;;;;;;;;;;;
;;; do macro
;;;;;;;;;;;;

;; The template body we're looking to emit at expansion time - this is if we were to call:
;; (do ((i 0 (1+ i))
;;      (cur 0 (1+ cur)))
;;     ((> i 5) 'done)
;;   (print i))

;; temp print macro
(defmacro print (stuff)
   `(format t "~S" ,stuff))

#|
(let ((i 0)
      (cur 0))
   (block my-loop
      (tagbody
         start
            (if (> i 5)
               (return-from my-loop 'done))
            (print i)
            (setq temp-i (1+ i))
            (setq temp-cur (1+ cur))
            (setq i temp-i)
            (setq cur temp-cur)
         (go start))))
|#

;;; Step 1: build let form out of expansion where the vars are provided in list, and we init them to 0
(defmacro letter1 (vars)
  (let ((var-inits (mapcar #'(lambda (v) `(,v 0)) vars)))
    `(let ,var-inits
       (format t i))))


;;; Step 2: now provide step forms too, but ignore them
(defmacro letter2 (var-decls)
  (let ((var-decls-only (mapcar #'(lambda (v) `(,(car v) ,(cadr v))) var-decls)))
    `(let ,var-decls-only
       (format t i))))


;;; Step 3: now provide test-and-result, and body form, this means 3 args - one for bindings, one for test-and-result, and one for body-forms.
;;; For now we'll unquote-splice in the body forms as that's a direct replacement.  Still need to handle the looping and test-and-result etc
(defmacro letter3 (var-decls test-and-result &rest body-forms)
  ; trivially grabbing just the var decls from all-bindings for now
  (let ((var-decls-only (mapcar #'(lambda (v) `(,(car v) ,(cadr v))) var-decls))
        (test-form (car test-and-result))
        (result-form (cadr test-and-result)))
    `(let ,var-decls-only
       ,@body-forms)))

;;; Step 4 add block and tagbody
(defmacro letter4 (var-decls test-and-result &rest body-forms)
  (let ((var-decls-only (mapcar #'(lambda (v) `(,(car v) ,(cadr v))) var-decls))
        (test-form (car test-and-result))
        (result-form (cadr test-and-result)))
    `(let ,var-decls-only
       (block my-loop
              (tagbody
                start
                ,@body-forms)))))

;;; Step 5 add step forms let binding
(defmacro letter5 (var-decls test-and-result &rest body-forms)
  (let ((var-decls-only (mapcar #'(lambda (v) `(,(car v) ,(cadr v))) var-decls))
        (test-form (car test-and-result))
        (result-form (cadr test-and-result))
        (step-forms (mapcar #'(lambda (v) (nth 2 v) var-decls))))
    `(let ,var-decls-only
       (block my-loop
              (tagbody
                start
                ,@body-forms)))))

;;; Step 6 add in an assoc list var-to-temp-var (actual var to gensym'd var)
;;; just setq the gensym'd one to 1 for now
(defmacro letter6 (var-decls test-and-result &rest body-forms)
  (let* ((var-decls-only (mapcar #'(lambda (v) `(,(car v) ,(cadr v))) var-decls))
         (test-form (car test-and-result))
         (result-form (cadr test-and-result))
         (step-forms (mapcar #'(lambda (v) (nth 2 v)) var-decls))
         (var-to-temp-var (mapcar #'(lambda (v) `(,(car v) ,(gensym))) var-decls)))
         ;(temp-updates (mapcar #'(lambda (v-to-t) (cadr v-to-t)) var-to-temp-var)))
    `(let ,var-decls-only
       (block my-loop
              (tagbody
                start
                ,@(mapcar #'(lambda (vtv) `(setq ,(cadr vtv) 1)) var-to-temp-var))))))

;;; Step 7 set temp var to step result instead of 1; this involves using the assoc list 
;;; for the step forms
(defmacro letter7 (var-decls test-and-result &rest body-forms)
  (let* ((var-decls-only (mapcar #'(lambda (v) `(,(car v) ,(cadr v))) var-decls))
         (test-form (car test-and-result))
         (result-form (cadr test-and-result))
         (step-forms (mapcar #'(lambda (v) `(,(car v) ,(nth 2 v))) var-decls))
         (var-to-temp-var (mapcar #'(lambda (v) `(,(car v) ,(gensym))) var-decls)))
    `(let ,var-decls-only
       (block my-loop
              (tagbody
                start
                ,@(mapcar #'(lambda (vtv) 
                              `(setq ,(cadr vtv) ,(cadr (assoc (car vtv) step-forms)))) var-to-temp-var))))))

;;; Step 8 the gensyms should be added to the (let) lexical scope
(defmacro letter8 (var-decls test-and-result &rest body-forms)
  (let* ((var-decls-only (mapcar #'(lambda (v) `(,(car v) ,(cadr v))) var-decls))
         (test-form (car test-and-result))
         (result-form (cadr test-and-result))
         (step-forms (mapcar #'(lambda (v) `(,(car v) ,(nth 2 v))) var-decls))
         (var-to-temp-var (mapcar #'(lambda (v) `(,(car v) ,(gensym))) var-decls))
         (temp-var-decls (mapcar #'(lambda (vtv) 
                                     (list (cadr vtv) 
                                           (cadr (assoc (car vtv) var-decls-only)))) var-to-temp-var)))
    `(let (,@temp-var-decls ,@var-decls-only)
       (block my-loop
              (tagbody
                start
                ,@(mapcar #'(lambda (vtv) 
                              `(setq ,(cadr vtv) ,(cadr (assoc (car vtv) step-forms)))) var-to-temp-var))))))

;;; Step 9 evaluate the test-form and rename to end-test-form to make it clearer
(defmacro letter9 (var-decls end-test-and-result &rest body-forms)
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
                start
                ;;; evaluate test-form and return result if true
                (if ,end-test-form (return-from my-loop ,result-form))
                ;;; update temp variables
                ,@(mapcar #'(lambda (vtv) 
                              `(setq ,(cadr vtv) ,(cadr (assoc (car vtv) step-forms)))) var-to-temp-var)
                ;;; execute body forms
                ,@body-forms)))))


;;; Step 10 construct updates of loop vars from temp vars
(defmacro letter10 (var-decls end-test-and-result &rest body-forms)
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
                start
                ;;; evaluate test-form and return result if true
                (if ,end-test-form (return-from my-loop ,result-form))
                ;;; update temp variables
                ,@(mapcar #'(lambda (vtv) 
                              `(setq ,(cadr vtv) ,(cadr (assoc (car vtv) step-forms)))) var-to-temp-var)
                ;;; set loop variables to temp variables
                ,@(mapcar #'(lambda (vtv) 
                              `(setq ,(car vtv) ,(cadr vtv))) var-to-temp-var)

                ;;; execute body forms
                ,@body-forms)))))

;;; Step 11 goto start if end test false
(defmacro letter11 (var-decls end-test-and-result &rest body-forms)
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
                start
                ;;; evaluate test-form and return result if true
                (if ,end-test-form (return-from my-loop ,result-form))
                ;;; update temp variables
                ,@(mapcar #'(lambda (vtv) 
                              `(setq ,(cadr vtv) ,(cadr (assoc (car vtv) step-forms)))) var-to-temp-var)
                ;;; set loop variables to temp variables
                ,@(mapcar #'(lambda (vtv) 
                              `(setq ,(car vtv) ,(cadr vtv))) var-to-temp-var)

                ;;; execute body forms
                ,@body-forms

                ;;; test end-test-form and do next loop if false
                (if ,end-test-form t (go start))
                )))))

;;; Step 12 return correct value when end-test-form
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

                ;; debug temp vars
                ;(format t "var to temp var ~S" ',var-to-temp-var)
                ;(format t "temp vars (update each time!) ~S ~S" #:G92 #:G93)

                ;;; set loop variables to temp variables
                ,@(mapcar #'(lambda (vtv) 
                              `(setq ,(car vtv) ,(cadr vtv))) var-to-temp-var)

                ;;; evaluate test-form (within loop)
                (if ,end-test-form (return-from my-loop ,result-form))

                ;;; execute body forms
                ,@body-forms

                ;;; test end-test-form and do next loop if false
                (go start)
                )))))

;; allow incremental testing in the repl via:
;; (and (load "lib/3-lists.lisp") (test1))
(defun test1 ()
  ; trivial test-and-result which is just t and t
  (do ((i 0 (1+ i)) (cur 0 (1+ cur))) ((eq i 10) 'done) (format t "in body")))

(macroexpand-1 '(do ((i 0 (1+ i)) (cur 0 (1+ cur))) ((eq i 10) '(1)) (format t "in body")))

;;; do some more tests
(defun test2 () (do ((temp-one 1 (1+ temp-one))
                     (temp-two 0 (1- temp-two)))
                  ((> (- temp-one temp-two) 5) temp-one)))


(macroexpand-1 '(do ((temp-one 1 (1+ temp-one))
                     (temp-two 0 (1- temp-two)))
                  ((> (- temp-one temp-two) 5) temp-one)))

;;; should return 3 but returns 2; updates go like:
;;; there are no body forms
;;; (before loop): temp-one = 1, temp-two = 0
;;;                end-test (= 3 temp-two) is false so we continue with the loop
;;; (update temp vars): g1 (for temp-one) = (1+ temp-one) = 2
;;;                     g2 (for temp-two) = (1+ temp-one) = 2
;;; (update loop vars): temp-one = 2
;;;                     temp-one = 2
;;; (eval test form):   (=3 temp-two) = false
;;; (go start)
;;; (update temp vars): g1 (for temp-one) = (1+ temp-one) = 3
;;;                     g2 (for temp-two) = (1+ temp-one) = 3
;;; (update loop vars): temp-one = 3
;;;                     temp-one = 3
;;; (eval test form):   (=3 temp-two) = true
;;; 

(defun test3 () (do ((temp-one 1 (1+ temp-one))
                     (temp-two 0 (1+ temp-one)))     
                  ((= 3 temp-two) temp-one) (format t "In loop body: ~S ~S" temp-one temp-two)))

(test3)

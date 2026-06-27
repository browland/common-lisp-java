
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

  ;; allow incremental testing in the repl via:
  ;; (and (load "lib/3-lists.lisp") (test))
  (defun test ()
    ; trivial test-and-result which is just t and t
    (letter4 ((i 0 (1+ i)) (cur 0 (1+ cur))) (t t) (format t "in body")))


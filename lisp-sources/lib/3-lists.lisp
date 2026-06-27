
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


;;; Step 3: now provide body form
(defmacro letter3 (var-decls-with-body-form)
  (let ((var-decls-only (mapcar #'(lambda (v) `(,(car v) ,(cadr v))) var-decls)))
    `(let ,var-decls-only
       (format t i))))

;; allow incremental testing in the repl via:
;; (and (load "lib/3-lists.lisp") (test))
(defun test ()
  (letter2 ((i 0 (1+ i)) (cur 0 (1+ cur)))))


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

;; The template body we're looking to emit at expansion time - this is if we were to call:
;; (do ((i 0 (1+ i))
;;      (cur 0 (1+ cur)))
;;     ((> i 5) 'done)
;;   (print i))

;; temp print macro
(defmacro print (stuff)
   `(format t "~S" ,stuff))

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
(defvar *nodes*
  (list
   (cons 'start
         '(:text "You wake up in a room. Left or right?"
           :choices (("left" . monster)
                      ("right" . door))))

   (cons 'monster
         '(:text "A monster kills you."
           :choices nil))

   (cons 'door
         '(:text "A steel door blocks your way. Key? (yes/no)"
           :choices (("yes" . escape)
                      ("no" . trapped))))

   (cons 'escape
         '(:text "You escape. You win."
           :choices nil))

   (cons 'trapped
         '(:text "You are trapped. The monster finds you."
           :choices nil))))

(defun get-node (name)
  (cdr (assoc name *nodes*)))

(setq node (get-node 'start))
(getf node :choices)


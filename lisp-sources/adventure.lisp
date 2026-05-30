(defvar *nodes*
  '((start :text "You wake up in a room. Left or right?"
           :choices (("left" . monster)
                     ("right" . door)))

    (monster :text "A monster kills you."
             :choices nil)

    (door :text "A steel door blocks your way. Key? (yes/no)"
          :choices (("yes" . escape)
                    ("no" . trapped)))

    (escape :text "You escape. You win."
            :choices nil)

    (trapped :text "You are trapped. The monster finds you."
             :choices nil)))

(defun get-node (name)
  (cdr (assoc name *nodes*)))

(setq node (get-node 'start))
(getf node :choices)

(defun play (node-name)
  (let ((node (get-node node-name)))
    (format t (getf node :text))

    (let ((choices (getf node :choices)))
      (if (null choices)
          (format t "End")
          (progn
            (mapcar #'car choices)

            (let ((input (read)))
              (let ((next (assoc input choices :test #'string=)))
                (if next
                    (play (cdr next))
                    (progn
                      (format t "Invalid choice")
                      (play node-name))))))))))

(play 'start)

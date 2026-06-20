# Lisp Interpreter

*Very* simple Common Lisp interpreter with a REPL.  Far from complete, but the small subset implemented works well enough for a learning exercise.

## Quick Example

This interpreter can run Lisp code like the following, which prints the first 100 terms of the Fibonacci Series:

```
; initialise our variables in a lexical scope
(let ((curr 1)
      (prev 0)
      (temp 0)
      (done nil))
      
  ; print the first two values before entering the loop
  (format t "~S" prev)
  (format t "~S" curr)
  
  ; loop through the fibonacci series ...
  (myloop until done do
    ; set our variables
    (setq temp prev)
    (setq prev curr)
    (setq curr (+ curr temp))
    
    ; print the new value for this loop iteration
    (format t "~S" curr)
    
    ; check if we've reached our upper bound; if so set our loop termination variable
    (if (> curr 100)
      (setq done t))))

```

Looping is implemented using a simple macro, defined in this project:

```
(defmacro myloop (arg1 done_var arg3 &rest body_forms)
  `(block loop_block
    (tagbody
      start
      ,@body_forms
      (if ,done_var (return-from loop_block))
      (go start)
    )
  )
)
```

## A bit more info ...

Things which basically work: user-defined functions, macros, lambdas, basic data types.  There's also support for many
special forms like `if`, `defvar`, `cond`, `funcall` and things like that.  Basic I/O support is starting to be added
so we can do useful and interesting stuff like Advent of Code or whatever.  Support for character literals was recently
added, which forced (another) rewrite of the parsing layer to allow lookahead.

Implementation has reached the point where we can start defining functions and macros in lisp itself, rather than always
calling back to the host language.  E.g. some loop macros have been implemented, but not `do` and not the true `loop` 
macro as it implements its own little domain-specific language - maybe one for later.

Speaking of macros, support for *hygienic macros* doesn't yet exist.  Once `gensym` is implemented, then that will
become possible.

## Running

```
export LISP_SOURCES_DIR=<path to the lisp-sources directory in this project>
export LISP_SOURCES_LIB=<path to the lisp-sources/lib directory in this project>

```
TODO: have a decent way to run more portably from a shell script, as I'm currently running the Repl class directly 
from IntelliJ.

## Issues
So I don't lose track ...

### And
and should be a macro or special operator, otherwise it can't short circuit

### Environment stuff and defvar, defparameter

Need to tidy this up - e.g. `defvar` and `defparameter` do the same thing as we've only just started to separate out the 
concept of declaring vars and setting existing ones.

`defvar` should only introduce a new variable if not already bound.  Shouldn't allow a constant (keyword) symbol to be assigned.
Nor allow assignments to special constants like `t`.  Calling `defvar` again on an existing symbol should have no effect -
works but will not update it.

### Macros

These have started to be implemented, but are being provided as I become able to write them :-/  The `do` macro is in 
progress, and requires `gensym` to be implemented before it'll be ready.

We don't support `&body` yet, but we do support `&rest` which still works to bind the body forms for macro definitions.

### Setf

`setf` should be implemented by a macro which can do matching on the place operand.  Need to implement things like `cond` (done) 
and `consp`, `and` (done), `rplaca` (done), `rplacd`, `cadr` (done), `symbolp` (done) etc first!

`setf` should be able to update an existing variable, and can't update one which is not yet declared.  `setf` also can't assign a new
value to special constants e.g. `t`.


# Reading

Background reading:

Program examples
https://cs.stanford.edu/people/nick/compdocs/LISP_Examples.pdf

Lisp By Example
https://github.com/ashok-khanna/common-lisp-by-example/raw/main/Common%20Lisp%20by%20Example.pdf

Practical Common Lisp
https://gigamonkeys.com/book/

# Using sbcl

sbcl is a real Common Lisp compiler and runtime which is very handy when learning Lisp.
I use sbcl a lot to learn Lisp and cross-check to make sure the interpreter is behaving correctly.

On mac, with readline support for command-line history etc:
```
% brew install sbcl rlwrap
% rlwrap sbcl
```

# Lisp Interpreter

Simple Common Lisp (tree-walking) interpreter with a REPL.  Far from complete coverage of the CL language, but support exists for lambda functions, hygienic macros, lexical scopes etc.

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

Hygienic macros work, too:

```
(defmacro safe-do-twice (body)
  (let ((var (gensym)))
    `(dotimes (,var 2)
      ,body)))

(safe-do-twice (format t "hello world"))
```


## A bit more info ...

Things which basically work: user-defined functions, hygienic macros (using `gensym`), lambdas with closures, basic data types.  There's also support for many
special forms like `if`, `defvar`, `cond`, `funcall` and things like that.  Basic I/O support is starting to be added
so we can do useful and interesting stuff like Advent of Code or whatever.  Support for character literals was recently
added, which forced (another) rewrite of the parsing layer to allow lookahead.

Implementation has reached the point where we can start defining functions and macros in lisp itself, rather than always
calling back to the host language.  E.g. some loop macros have been implemented, including `dotimes`, `dolist` and `do`.

## Running

```
export LISP_SOURCES_DIR=<path to the lisp-sources directory in this project>
export LISP_SOURCES_LIB=<path to the lisp-sources/lib directory in this project>

./build.sh
./repl.sh

```

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
progress, and now that `gensym` is implemented (hygienic macros) we're a bit closer to implementing it.

We don't support `&body` yet, but we do support `&rest` which still works to bind the body forms for macro definitions.

### Setf

`setf` should be implemented by a macro which can do matching on the place operand.  Need to implement things like `cond` (done) 
and `consp`, `and` (done), `rplaca` (done), `rplacd`, `cadr` (done), `symbolp` (done) etc first!

`setf` should be able to update an existing variable, and can't update one which is not yet declared.  `setf` also can't assign a new
value to special constants e.g. `t`.

### Load

The `load` special form is really handy for testing lisp sources in the repl.  But it only prints the result of the last form in the loaded file.  It would be handy to be able to execute a 'script' in the repl as if we were typing it in, e.g. for testing some work in progress.  I'm not sure if this should be the responsibility of `load` though.  Maybe another special form, or maybe `load` indeed can be re-purposed?

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
# Compiler

This is under the very early stages of development.  We have a rough outline of a tree walking compiler, supporting 
ARM64 on Mac only, which has code for primitive evaluation of an add function call, and the startings of a symbol table 
containing "t".  We are using pointer tagging to allow determination of type of things at runtime.

Next steps:
1. Implementation of symbol table lookup: use "compact index" project?  Would need to switch to doing a lexicographical
   ordering of the array elements rather than a 'hash' of sorts.  This would involve a predicate for determining position
   (i.e. considering each character from first onwards) to determine whether one string is larger than the other.  This
   is different from the current approach where we determine position in the array using a deterministic hash.
2. Implement special forms.  Need reference back to TreeWalker when calling the specialised compilation handler for each
   one?  Since e.g. for `if` we'd need to call into walkTree() from within the special form handling code.
3. Clean up old code if no longer needed (we've moved to a simpler, less abstracted approach).
4. Look into TBI (built-in pointer tagging for ARM).  Lose portability but interesting to look into?
   Also I lose 3 bits of precision from numeric primitives (so-called fixnums in Lisp) due to using tagging for literal 
   values (not memory addresses).  Alternative approaches?  E.g. always store numeric literals in memory (e.g. using a
   pool generated by the compiler, rather than allocating each occurence).  They could be either in the .rodata section
   or allocated in heap on a resizable array.  
   Heap allocation would certainly slow down and complicate things.  The compiler would need to generate instructions to 
   look up the address of numeric literals each time.  And if we used the .rodata section it could be massive.
   Reminds me of Java's integer pool.

Issues:
1. When we use a non-existent symbol e.g. + instead of add when we have only stored `add` in the symbol table, then we
   get an error from clang (as the symbol name string isn't there) but we should have an error out of the compiler as
   we should know we have no mapping for it.
2. Bindings for defun
3. Handle user-defined functions in operator position
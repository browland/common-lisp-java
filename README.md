# Design

## Issues

### Loops

How to impl the simplest loop behaviour (not the 'loop' macro)?  So things like do, dolist, dotimes.
E.g. what's the simplest building block - is it tagbody etc?

### Defvar/setf

Setf should be implemented by a macro which can do matching on the place operand.  Need to implement things like cond 
and consp, 'and', rplaca, rplacd, cadr, symbolp etc first!

Setf can update an existing variable, and can't update one which is not yet declared.  Setf also can't assign a new
value to special constants e.g. t.

Defvar only introduces a new variable if not already bound.  Shouldn't allow a constant (keyword) symbol to be assigned.
Nor allow assignments to special constants like t.  Calling defvar again on an existing symbol will have no effect -
works but will not update it.

# Program Examples

https://cs.stanford.edu/people/nick/compdocs/LISP_Examples.pdf

Lisp By Example
https://github.com/ashok-khanna/common-lisp-by-example/raw/main/Common%20Lisp%20by%20Example.pdf

# Using sbcl

On mac:
```
% brew install sbcl
% sbcl
```

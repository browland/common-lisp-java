# Design

## Issues

### Environment stuff

Keep variable and function namespaces separate by having e.g. setVariable() setFunction() getVariable() etc and 
setVariableInScope() etc.  You know which one to call from the context.

### Macro expansion

What problem was I trying to solve? 

I need to stick to evaluation returning a Value.  But when a macro is expanded, a ConsCellValue is (usually) returned.
This is then translated back to an RList and evaluated as normal.
This approach is because macro expansion can execute arbitrary code to conditionally return one code 
structure or another.  So we need to use the evaluator, but just making sure that we don't look up binding values 
from the environment (we just pass them in as bindings as-is).

### Macro environment capture
At defmacro time, captured environment needs to be set on the Macro.  This can be used during expansion and not during 
evaluation of the expanded macro.  So need to ensure we honour this.  Perhaps add the bindings to the captured env as a new scope.

Also consider, it might be easier to keep parse results as a ConsCellValue to avoid starting to have
to convert back and forth between RList and ConsCellValue as I go further down this path.
Might even be worth having a branch where I try that approach.

### BindingEvaluator

Why can't you just pass parameters through the regular Evaluator?  I think this is bc of special handling for &rest etc.

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

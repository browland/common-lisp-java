# Design

## Issues

### Assertions on cons cell values

It's really awful as we keep having to unwrap values all the way.  Additionally it's confusing to think about what type 
we're dealing with at each point.  And hard to come up with consistent names for each intermediate variable and I'm sometimes
not sure if I'm asserting the right thing.  
Is there a way to unwrap everything into a nicer structure for testing/printing/debugging?
E.g. convert to what would be printed in the repl.
Should getValue() be unwrap()?  As we start with a Value, and calling getValue() returns something which is not a Value
but some real type.

### Environment stuff

Keep variable and function namespaces separate by having e.g. setVariable() setFunction() getVariable() etc and 
setVariableInScope() etc.  You know which one to call from the context.

### Macros and destructuring

* We should be able to pass arbitrarily deep nested structures - we'd then need to walk the tree matching bindings up, rather than
just expecting atoms in a single list.
* Should also have a test for destructuring followed by an &rest binding.

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

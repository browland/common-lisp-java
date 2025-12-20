# Design

## Issues

* Globals defined during closure evaluation: For now just manually copying globals back in to the captured environment 
  after application.  Should have a separate single shared global environment.
  
* Globals need handling properly (rather than manually handling * prefix and suffix)

* format only works with strings - should work with cons cells for eg.

* closure toString() causing StackOverflowException so have hacked in a minimal toString() for now

## List parsing (ListParser)

The first layer (ListParser) turns the program into a tree of nodes.  Each node is an element in the list.  We don't know
yet what each node is - it's just a string.

We need to do a little more at this level - we need to understand quoting otherwise we'll get an invalid tree.
A simple use of a quoted function looks like this:

```
(funcall #'+ 1 2 3)
```

In this case, the second element of the list is the quoted '+' function.  In this case we want to treat this as the '+'
symbol, but also set some state on this symbol to indicate it's quoted rather than just ingesting the #'+ as one element.  

This might make more sense in this example:

```
(funcall #'(lambda (x) (+ x 1)) 1)
```

The top-level list contains three elements, and the second element is the quoted function.  Without special handling of
quotes at this level, this would break list parsing which otherwise might just look for text elements separated by a space,
and nested within parentheses. Bearing in mind we don't understand what a lambda is yet, we need to ensure we pick up the 
sharp-quote pair of characters correctly and attach some state to the inner list so we remember the inner list is quoted.  
We track each type of quote (e.g. function quote, quoted string etc) as we need to understand what they are at this level 
anyway, in order to parse correctly.

The next layer up will transform quoted forms into the (quote ...) form.  It's easier to do this once we already have a
tree form.

So the result of ListParser.parse() is a ParsedList.  This contains a List<ListNode> and a QuoteType which is an enum
type which captures the quote type mentioned above.

A ListNode contains either:
* a plain value (as a String), or 
* a ParsedList (where this element is itself a list)

The ListNode also contains a QuoteType, which is only set when the value is present.  It's not set when a ParsedList is
present because the QuoteType would be set on the ParsedList itself.

TODO For this reason we should refactor ListNode so it doesn't have these two flavours.

The output of this level should be a valid ParsedList when any program is passed into parse().


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

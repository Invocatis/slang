# slang

## Design Assumptions

1. If the stack is empty at the end of a function, nil is returned
2. Pops and peeks on the stack at any other point is a fatal exception
3. Symbols with multiple trailing + marks results in a syntax error
     - This is due to ambiguity in later accesses of the same variable. Given the assignment `!v++`, is `!v+` an assignment or a reference?

## Design Notes

Slang implements the stack language by converting the list of statements into Clojure code using macros. Beginning with the last statement, the interpreter recursively generates the code. Each interpreted statement returns a stack, represented by a Clojure vector, with an environment attached via metadata. A set of formal parameters (specifically the symbols used to represent the parameters to the function) is attached to a global variable dynamically with `binding`. Finally, a context is also represented in a global variable. Currently, the context only represents which statement the interpreter is on for use in error logging.

Since we are generated Clojure code, it is fairly trivial to achieve recursion in our stack functions. By allowing an optional name to be added to our function definition, recursion arrises in the very same way that it does in Clojure.

## Further Improvements

1. One might want to dynamically invoke functions, that is, push a function and an arity on the stack and apply invoke> to them. This will be left for another day.

## Test Cases

A number of tests cases may be found in the `slang.core-test` namespace. Including the given test case, an array of other common functions have been implemented in slang to prove its abilities.

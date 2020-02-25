# slang

## Usage

Slang implements two api points, `stackfn` and `defstackfn`. These can be thought of as analogous to `fn` and `defn`, respectively. Each takes as arguments an name (optionally in the case of stackfn), a formal parameter definition (each parameter prefixed with a '!'), and finally a body of statements that define stack operations.

```clojure
(defstackfn inc
  [!n]
  !n
  1
  (invoke> + 2))
```

## Operations

For each of the following statment types, the given operation is executed:


### constant

Constants are pushed to the stackfn

```Clojure
1 ; [1]
:a ; [1 :a]
"asdf" ; [1 :a "asdf"]
```

### variables / assignments

Variables are denoted with the prefix `!`. Assignments are variables marked with the postfix `+`. When assigned, the top value of the stack is stored in the variable, and left on the stack.
```clojure
1 ; [1]
!a+ ; [1]
!a ; [1 1]
```
The previous snippet of code pushes the value 1 to the stack, assigns the variable `!a` to the value at the top of the stack (`1`), and pushes the value of `!a` to the stack. Thus, we end with a stack of `[1 1]`, the first from our constant `1` and the latter from our variable `!a`.

##### Note!
Variables with multiple assignment postfixes, such as `!a+++` are considered syntax errors. This is due to the ambiguity created when trying to use them later (is `!a++` the assignment of `!a+` or the reference to the variable assigned by `!a+++`).

### <pop>

The `<pop>` operator pops the top value of the stack off the stack. The value is not used in any way, and is removed for the rest of the programs execution.

```clojure
1 ; [1]
<pop> ; []
<pop> ; ERROR! Empty stack!
```

Attempting to pop an empty stack is an error.

### invoke>

`invoke>` takes two arguments, a function, and an number representing the number of arguments needed. It then applys that function to the top `n` values of the stack, in the order they were pushed.

```clojure
"hello" ; => ["hello"]
\space ; => ["hello" \space]
"world!" ; => ["hello" \space "world!"]
(invoke> str 3) ; => ["hello world!"]
```

The result of the function is then pushed back to the stack, with all arguments removed from the stack.

If the stack size is smaller than the number of parameters requested, this results in an ArityException.

### if> / else>

Branching in slang is done via if/else structures. When encountered, the top value of the stack is popped, checked for truthiness, and then discarded. Given it's truthiness, the `if` branch (given true) or the `else` branch (given false) will be executed.

```clojure
true
(if>
  "True!"
  (invoke> println 1)
 else>
  "False!"
  (invoke> println 1))
```

## Design Notes

Slang implements the stack language by converting the list of statements into Clojure code using macros. Beginning with the last statement, the interpreter recursively generates the code. Each interpreted statement returns a stack, represented by a Clojure vector, with an environment attached via metadata. A set of formal parameters (specifically the symbols used to represent the parameters to the function) is attached to a global variable dynamically with `binding`. Finally, a context is also represented in a global variable. Currently, the context only represents which statement the interpreter is on for use in error logging.

Since we are generated Clojure code, it is fairly trivial to achieve recursion in our stack functions. By allowing an optional name to be added to our function definition, recursion arrises in the very same way that it does in Clojure.

## Further Improvements

1. One might want to dynamically invoke functions, that is, push a function and an arity on the stack and apply invoke> to them. This will be left for another day.
2. `defstackfn` could be implemented to have similar function as `defn`.
3. Better stack traces could be generated within the exception errors. As of now, only the top level of the stackfn is referenced along with a surplus of other needless information.

## Test Cases

A number of tests cases may be found in the `slang.core-test` namespace. Including the given test case, an array of other common functions have been implemented in slang to prove its abilities.

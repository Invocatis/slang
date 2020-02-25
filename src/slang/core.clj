(ns slang.core
  (:refer-clojure :exclude [pop peek]))

(defn- strip-assign
  [assign]
  (apply (comp symbol str) (butlast (name assign))))

(declare interpret)

(def ^:private ^:dynamic formals #{})

(def ^:private ^:dynamic context {})

(defmacro pop
  [stack]
  `(let [stack# ~stack]
    (if (empty? stack#)
      (throw (Exception. "Slang Error @ <pop> was called on empty stack!"))
      (clojure.core/pop stack#))))

(defmacro peek
  [stack]
  `(let [stack# ~stack]
    (if (empty? stack#)
      (throw (Exception. "Slang Error @ <peek> was called on empty stack!"))
      (clojure.core/peek stack#))))

(defn- constant?
  [any]
  true)

(defn ^:private eval|constant
  [body stack]
  `(conj ~(interpret (rest body) stack) ~(first body)))

(defn- variable?
  [any]
  (and (symbol? any) (= (first (name any)) \!)))

(defn ^:private eval|variable
  [[var & rest] stack]
  (if (contains? formals var)
    `(let [stack# ~(interpret rest stack)]
       (conj stack# (or (get (meta stack#) '~var) ~var)))
    `(let [stack# ~(interpret rest stack)]
       (when-not (contains? (meta stack#) '~var)
         (throw (Exception. (format "Slang Syntax Error @ %s (%s) :: Unable to resolve variable" '~var ~(get context :n)))))
       (conj stack# (get (meta stack#) '~var)))))

(defn- assign?
  [any]
  (and (symbol? any) (boolean (re-matches #".*?\+{1}" (name any)))))

(defn ^:private eval|assign
  [body stack]
  (let [assign (first body)]
    (when (re-matches #".*?\+{2,}" (name assign))
      (throw (Exception. (format "Slang Syntax Error @ %s (%s):: Symbol has more than 1 trailing + marks" (name assign) (get context :n)))))
    `(let [stack# ~(interpret (rest body) stack)]
       (with-meta stack#
         (assoc-in (meta stack#) ['~(strip-assign assign)] (peek stack#))))))

(defn- pop?
  [any]
  (= any '<pop>))

(defn ^:private eval|pop
  [body stack]
  `(pop ~(interpret (rest body) stack)))

(defn- invoke?
  [any]
  (and (list? any)
       (= 'invoke> (first any))))

(defn- verify|invoke
  [[_ f arity]]
 (or (nil? arity) (integer? arity)))

(defn ^:private eval|invoke
  [body stack]
  (let [[_ f arity :as invoke] (first body)]
    (when-not (verify|invoke invoke)
      (throw (Exception. (format "Slang Syntax Error @ %s (%s) :: Invoke expects a function, and an integer arity" invoke (get context :n)))))
    `(let [stack# ~(interpret (rest body) stack)]
       (if (< (count stack#) ~arity)
         (throw (Exception. (format "Arity mismatch @ %s (%s) :: Function needed %s args, but stack only had %s values"
                                    (quote ~(first body))
                                    ~(get context :n)
                                    ~arity
                                    (count stack#))))
         (conj (with-meta (subvec stack# 0 (- (count stack#) ~arity)) (meta stack#))
             (apply ~f (subvec stack# (- (count stack#) ~arity))))))))

(defn- if?
  [stmt]
  (and (list? stmt) (= 'if> (first stmt))))

(defn- else?
  [stmt]
  (= 'else> stmt))

(defn  ^:private eval|if
  [body stack]
  (let [[truthy falsy] (split-with (complement else?) (rest (first body)))
        stack (interpret (rest body) stack)
        form `(let [stack# ~stack]
                (if (peek stack#)
                  (interpret '~(reverse truthy) (quote (pop stack#)))
                  (interpret '~(reverse (rest falsy)) (quote (pop stack#)))))]
    (clojure.walk/postwalk
      (fn [f]
        (if (and (seq? f) (symbol? (first f)) (= "interpret" (name (first f))))
          (eval f)
          f))
      form)))

(defn interpret
  [body stack]
  (binding [context (when context (update context :n #(when % (dec %))))]
    (if (empty? body)
      stack
      (let [stmt (first body)]
        (cond
          (assign? stmt)   (eval|assign body stack)
          (variable? stmt) (eval|variable body stack)
          (pop? stmt)      (eval|pop body stack)
          (invoke? stmt)   (eval|invoke body stack)
          (if? stmt)       (eval|if body stack)
          (constant? stmt) (eval|constant body stack)
          :else           (throw
                            (Exception.
                             (format "Slang Syntax Error @ %s (%s)" stmt (get context :n)))))))))

(defn num-statements
  [body]
  (apply +
    1
    (map
     (fn [statement]
       (if (if? statement)
         (num-statements statement)
         1))
     body)))

(defmacro stackfn
  [& sigs]
  (let [name (when (symbol? (first sigs)) (list (first sigs)))
        formals (if (vector? (first sigs)) (first sigs) (second sigs))
        body (if (nil? name) (rest sigs) (rest (rest sigs)))]
    (when-not (every? variable? formals)
      (throw (Exception. "All formal arguments must be symbols prefixed with !")))
    (let [f (into #{} formals)]
     `(fn ~@name ~formals
        (clojure.core/peek
         ~(binding [formals (into #{} f)
                    context {:n (num-statements body)}]
           (interpret (reverse body) [])))))))

(defmacro defstackfn
  [n formals & body]
  `(def ~n (stackfn ~n ~formals ~@body)))

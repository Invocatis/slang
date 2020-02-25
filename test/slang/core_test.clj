(ns slang.core-test
  (:require [clojure.test :refer :all]
            [slang.core :as slang :refer [defstackfn]]
            [clojure.set]))

(defstackfn f
  [!a !b !c]
  !a
  !b
  (invoke> + 2)
  !v1+
  !c
  !c
  <pop>
  2
  (invoke> * 2)
  !v2+
  (invoke> = 2)
  (if>
    !v1
    !v2
    (invoke> - 2)
   else>
    "false!!"
    (invoke> println 1)
    <pop>
    !v1
    !v2
    (invoke> * 2)))

(defstackfn !
  [!n]
  !n
  (invoke> zero? 1)
  (if>
    1
   else>
    !n
    (invoke> dec 1)
    (invoke> ! 1)
    !n
    (invoke> * 2)))

(defstackfn -fib
  [!n !a !b]
  !n
  (invoke> zero? 1)
  (if>
    !a
   else>
    !n
    (invoke> dec 1)
    !b
    !a
    !b
    (invoke> +' 2)
    (invoke> -fib 3)))

(defstackfn fib
  [!n]
  !n
  1
  1
  (invoke> -fib 3))

(defstackfn gcd
  [!x !y]
  !y
  (invoke> zero? 1)

  (if>
    !x
   else>
    !x
    !y
    (invoke> mod 2)
    !remainder+
    <pop>
    !y
    !remainder

    (invoke> gcd 2)))

(defn other
  [from to]
  (first (clojure.set/difference #{0 1 2} #{from to})))

(defstackfn -hanoi
  [!state !from !to !n]

  !n
  1
  (invoke> = 2)

  (if>
    !state
    (invoke> println 1)
    <pop>

    !state
    !from
    (invoke> get 2)
    (invoke> clojure.core/peek 1)

    !value+
    <pop>

    !state
    !from
    clojure.core/pop
    (invoke> update 3)

    !to
    conj
    !value
    (invoke> update 4)
   else>

    #{0 1 2}

    !from
    !to
    (invoke> #(into #{} %&) 2)

    (invoke> clojure.set/difference 2)
    (invoke> first 1)
    !other+
    <pop>

    !state
    !from
    !other
    !n
    (invoke> dec 1)
    !oneless+

    (invoke> -hanoi 4)

    !from
    !to
    1
    (invoke> -hanoi 4)

    !other
    !to
    !oneless
    (invoke> -hanoi 4)))

(defn hanoi
  [n]
  (-hanoi
    [(into [] (map #(apply (comp symbol str) (repeat % 'x)) (reverse (range 1 (inc n))))) [] []]
    0 2 n))

(defstackfn merge*
  [!l0 !l1]
  !l0

  (invoke> empty? 1)
  (if>
    !l1
   else>
    !l1
    (invoke> empty? 1)
    (if>
      !l0
     else>
      !l0
      (invoke> first 1)
      !v0+

      !l1
      (invoke> first 1)
      !v1+

      !v0
      !v1
      (invoke> < 2)
      (if>
        !l0
        (invoke> rest 1)
        !l1
        (invoke> merge* 2)
        !result+
        <pop>
        !v0
        !result
        (invoke> cons 2)
       else>
        !l0
        !l1
        (invoke> rest 1)
        (invoke> merge* 2)
        !result+
        <pop>
        !v1
        !result
        (invoke> cons 2)))))

(defstackfn split
  [!l]
  !l
  (invoke> count 1)
  2
  (invoke> / 2)
  (invoke> int 1)
  !l
  (invoke> (juxt take drop) 2))

(defstackfn mergesort
  [!l]
  !l
  (invoke> count 1)
  1
  (invoke> = 2)
  (if>
    !l
   else>
    !l
    (invoke> split 1)
    !s+
    (invoke> first 1)
    (invoke> mergesort 1)

    !s
    (invoke> second 1)
    (invoke> mergesort 1)

    (invoke> merge* 2)))



(deftest given-function-test
  (testing "Testing Given function"
    (is (= (f 1 2 4) 24))
    (is (= (with-out-str (f 1 2 4)) "false!!\n"))
    (is (= (f 4 4 4) 0))))

(deftest fibonacci-test
  (testing "Testing Fibonacci Sequence"
    (is (= 1 (fib 1)))
    (is (= 89 (fib 10)))
    (is (= 956722026041 (fib 58)))))

(deftest gcd-test
  (testing "Testing GCD"
    (is (= 37 (gcd 111 259)))
    (is (= 9 (gcd 9 27)))))

(deftest hanoi-test
  (testing "Testing Hanoi"
    (is (= '[[] [] [xxxxx xxxx xxx xx x]] (hanoi 5)))))

(deftest mergesort-test
  (testing "Testing Mergesort"
    (is (= '(1 2 3 4 5 6 7 8 9) (mergesort (shuffle (range 1 10)))))))

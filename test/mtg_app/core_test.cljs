(ns mtg-app.core-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [mtg-app.core :as core]))

(deftest fake-test
  (testing "fake description"
    (is (= 1 2))))

(ns mtg-app.subs
  (:require
   [re-frame.core :as rf]))

(rf/reg-sub
 ::card
 (fn [db]
   (:card db)))

(rf/reg-sub
 ::card-list
 (fn [db]
   (sort-by :name (take 12 (:card-list db)))))

(rf/reg-sub
 ::search-form-value
 (fn [db]
   (get db :search-form-value "")))

(rf/reg-sub
 ::my-deck
 (fn [db]
   (sort-by :name (:my-deck db))))
(rf/subscribe [::my-deck])

(rf/reg-sub
 ::mana-count
 (fn [db [_ qtt]]
   (let [deck (:my-deck db)]
     (reduce + (map #(:amount %) (filter #(= (:mana %) qtt) deck))))))

(rf/subscribe [::mana-count 2])

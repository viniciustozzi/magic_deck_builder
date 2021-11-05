(ns mtg-app.events
  (:require
   [re-frame.core :as rf]
   [mtg-app.db :as db]
   [day8.re-frame.http-fx]
   [ajax.core :as ajax]
   [day8.re-frame.tracing :refer-macros [fn-traced]]))

(rf/reg-event-db
 ::initialize-db
 (fn-traced [_ _]
            db/default-db))

(rf/reg-event-fx                             ;; note the trailing -fx
 :load-random                      ;; usage:  (dispatch [:handler-with-http])
 (fn [{:keys [db]} _]                    ;; the first param will be "world"
   {:db   (assoc db :show-twirly true)   ;; causes the twirly-waiting-dialog to show??
    :http-xhrio {:method          :get
                 :uri             "https://api.scryfall.com/cards/random"
                 :timeout         8000                                           ;; optional see API docs
                 :response-format (ajax/json-response-format {:keywords? true})  ;; IMPORTANT!: You must provide this.
                 :on-success      [:on-random-card-success]
                 :on-failure      [:on-http-error]}}))

(rf/reg-event-db
 :on-random-card-success
 (fn [db [_ result]]
   (assoc db :card result)))

(rf/reg-event-db
 :on-http-error
 (fn [db [_ result]]
    ;; result is a map containing details of the failure
   (assoc db :http-error result)))

(rf/reg-event-fx                             ;; note the trailing -fx
 :search-cards                      ;; usage:  (dispatch [:handler-with-http])
 (fn [{:keys [db]} [_ query]]                    ;; the first param will be "world"
   {:db   (assoc db :show-twirly true)   ;; causes the twirly-waiting-dialog to show??
    :http-xhrio {:method          :get
                 :uri             "https://api.scryfall.com/cards/search"
                 :params          {:q query}
                 :timeout         8000                                           ;; optional see API docs
                 :response-format (ajax/json-response-format {:keywords? true})  ;; IMPORTANT!: You must provide this.
                 :on-success      [:on-cards-loaded]
                 :on-failure      [:on-http-error]}}))

(defn build-cards-map [data]
  {:name (-> data :name)
   :img (-> data :image_uris :normal)
   :mana (-> data :cmc)})

(rf/reg-event-db
 :on-cards-loaded
 (fn [db [_ result]]
   (let [cards (map build-cards-map (:data result))]
     (assoc db :card-list cards))))

(rf/reg-event-db
 :update-search-form-value
 (fn [db [_ value]]
   (assoc db :search-form-value value)))

(rf/reg-event-db
 :on-card-click
 (fn [db [_ value]]
   nil))

(defn is-card-in-deck? [card my-deck]
  (let [name (:name card)]
    (seq (filter #(= (:name %) name) my-deck))))

(defn is-last-card? [card]
  (let [amount (:amound card)]
    (= amount 1)))

(defn subtract-card-amount [c]
  (update c :amount dec))

(defn find-card-in-deck [c deck]
  (into {} (filter #(= (:name c) (:name %)) deck)))

(defn can-add-to-deck [card card-in-deck]
  (and (< (:amount card-in-deck) 4) (= (:name card-in-deck) (:name card))))

(rf/reg-event-db
 :events/add-to-deck
 (fn [db [_ card]]
   (let [my-deck (get db :my-deck [])]
     (if (is-card-in-deck? card my-deck)
       (assoc db :my-deck (map (fn [deck-card]
                                 (if (can-add-to-deck card deck-card)
                                   (assoc card :amount (inc (:amount deck-card)))
                                   deck-card)) my-deck))
       (assoc db :my-deck (conj my-deck (assoc-in card [:amount] 1)))))))

(rf/reg-event-db
 :events/remove-from-deck
 (fn [db [_ card]]
   (let [my-deck (get db :my-deck [])]
     (assoc db :my-deck (->> my-deck
                             (map (fn [c] (if (= (:name c) (:name card))
                                            (subtract-card-amount c)
                                            c)))
                             (filter (#(> (:amount %) 0))))))))

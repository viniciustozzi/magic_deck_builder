(ns mtg-app.views
  (:require
   [re-frame.core :as rf]
   [stylefy.core :as styles]
   [mtg-app.subs :as subs]
   [cljs.pprint :as pprint]))

(def grid-style {; Default style uses Flexbox as fallback
                 :display "flex"
                 :flex-direction "row"
                 :flex-wrap "wrap"
                 ::styles/media {{:max-width 500}
                                 {:display "block"}}
                 ; Use CSS Grid style if it is supported by the browser.
                 ; If the browser does not support CSS Grid or feature queries at all, this
                 ; block is simply ignored.
                 ::styles/supports {"display: grid"
                                    {:display "grid"
                                     :grid-template-columns "350px 350px 350px 350px"
                                     :justify-content "center"
                                     :margin-left "auto"
                                     :margin-right "auto"
                                      ; Make CSS Grid responsive
                                     ::styles/media {{:max-width 500}
                                                     {:grid-template-columns "1fr"}}}}})

(defn bar-size-style [size]
  {:--size size
   :--color "blue"})

(defn debug-info [info]
  [:pre (with-out-str (cljs.pprint/pprint info))])

(defn search-form [placeholder value]
  [:div {:class "field has-addons container"}
   [:div.control
    [:input.input {:on-blur #(rf/dispatch [:update-search-form-value (-> % .-target .-value)])
                   :type "text"
                   :placeholder placeholder}]]
   [:div.contro
    [:a {:class "button is-info"
         :on-click #(rf/dispatch [:search-cards value])} "Search"]]])

(defn wide-button [text on-click]
  [:button.button.is-info.is-fullwidth.is-1
   {:on-click on-click} text])

(defn card-component [card]
  (let [name (:name card)
        img (:img card)]
    [:div
     [:div.image.p-2
      [:h2.subtitle.center name]
      [:img {:src img}]
      (wide-button "Add To Deck" #(rf/dispatch [:events/add-to-deck card]))]]))

(defn card-list-component [card-list]
  [:div (styles/use-style grid-style)
   (map card-component card-list)])

(defn my-deck-component [card-list]
  [:div (styles/use-style grid-style)
   (map (fn [card]
          [:div.block
           [:div.image.p-2
            [:h2.subtitle.center (str (:name card) " (" (:amount card) ")")]
            [:img {:src (:img card)}]
            (wide-button "Remove from Deck" #(rf/dispatch [:events/remove-from-deck card]))]])
        card-list)])

(defn random-card-component [card-img]
  [:div
   [:button.button.is-primary.mb-4 {:on-click #(rf/dispatch [:load-random])} "Load Random Card"]
   [:img {:src card-img}]])

(defn single-bar [amount]
  [:tr
   [:td (styles/use-style (bar-size-style (str (/ amount 10)))) "1"]])

(defn bar-chart []
  (let
   [mana-1-amount (rf/subscribe [::subs/mana-count 1])
    mana-2-amount (rf/subscribe [::subs/mana-count 2])
    mana-3-amount (rf/subscribe [::subs/mana-count 3])
    mana-4-amount (rf/subscribe [::subs/mana-count 4])
    mana-5-amount (rf/subscribe [::subs/mana-count 5])
    mana-6-amount (rf/subscribe [::subs/mana-count 6])
    mana-7-amount (rf/subscribe [::subs/mana-count 7])]
    [:div.box
     [:h1.title "Mana Curve"]
     [:table {:id "mana-chart" :class "charts-css column data-spacing-3"}
      [:tbody
       (single-bar @mana-1-amount)
       (single-bar @mana-2-amount)
       (single-bar @mana-3-amount)
       (single-bar @mana-4-amount)
       (single-bar @mana-5-amount)
       (single-bar @mana-6-amount)
       (single-bar @mana-7-amount)]]]))

(defn main-panel []
  (let [card-list (rf/subscribe [::subs/card-list])
        search-value (rf/subscribe [::subs/search-form-value])
        my-deck (rf/subscribe [::subs/my-deck])]
    [:div
     [:div.block.container
      [:h1.title "MTG Deck Builder"]
      (search-form "Find a card" @search-value)]
     (card-list-component @card-list)
     [:div.block.container
      [:h1.title "My Deck"]
      (my-deck-component @my-deck)
      (bar-chart)]]))
(def x 5)

(* 3 (+ 1 2))

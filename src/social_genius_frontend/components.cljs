(ns social_genius_frontend.components
  (:require [reagent.core :as reagent]))



(defn meetup-input [get-group]
  (let [meetup-name (reagent/atom "")]
    (fn []
      [:div {:class "col-md-4"}
       [:div {:class "input-group"}
        [:input {:type "text"
                 :class "form-control"
                 :placeholder "Meetup group name"
                 :on-change #(reset! meetup-name (-> % .-target .-value))}]
        [:span {:class "input-group-btn"}
         [:button {:class "btn btn-default"
                   :on-click (fn []
                               (get-group @meetup-name))} "Get group"]]]])))
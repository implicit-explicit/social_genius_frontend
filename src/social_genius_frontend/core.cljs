(ns social_genius_frontend.core
  (:require [reagent.core :as reagent]
            [reagent.session :as session]
            [social_genius_frontend.components :as components]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [secretary.core :as secretary :include-macros true]
            [ajax.core :refer [GET POST]]
            [cljs-time.periodic :as periodic]
            [cljs-time.core :as time]
            [cljs-time.format :as tf]
            [cljs-time.coerce :as tc])
  (:import goog.History))

(enable-console-print!)
(println "Executing frontend code")

;; App data
(defonce app-state (reagent/atom {:text "Social Genius"}))

(def date-formatter (tf/formatters :date))

;;--------------------------
;; Backend communication

(defn response-handler [response]
  (println "Response payload" response)
  (doseq [event-date (get response "ocamsterdam")]
    (println (tc/from-long event-date))))

(defn error-handler [{:keys [status status-text]}]
  (println (str "something bad happened: " status " " status-text)))

(defn get-group [group]
  (println (str "Retrieving members for group: " group))
  (GET (str "/city")
       {:handler response-handler
        :error-handler error-handler
        :response-format :json
        :params {:meetup_group group}}))


;;--------------------------
;; Components

(defn menu []
  [:nav {:class "navbar navbar-default navbar-static-top"}
    [:div {:class "container"}
      [:div {:class "navbar-header"}
        [:button {:type "button" :class "navbar-toggle collapsed" :data-toggle "collapse" :data-target "#navbar" :aria-expanded "false" :aria-controls "navbar"}
          [:span {:class "sr-only"} "Toggle navigation"]
          [:span {:class="icon-bar"}]
          [:span {:class="icon-bar"}]
          [:span {:class="icon-bar"}]]]
      [:div {:id "navbar" :class "collapse navbar-collapse"}
        [:ul {:class "nav navbar-nav"}
          [:li [:a {:href "#/"} "Home"]]
          [:li [:a {:href "#/about"} "About page"]]]]
    ]])

;;--------------------------
;; Pages

(defn home-page []
  [:div
   (menu)
   [:div {:class "container-fluid"}
    [:div {:class "row" :style {:padding "3em"}}
     [components/meetup-input get-group]]
    [:div {:class "row" :style {:padding "3em"}}
     [:div {:class "col-md-8"}
      [:table {:class "table table-hover table-condensed"}
       [:thead
       [:tr
        [:th "Date"]
        [:th "Docker-Randstad"]
        [:th "Elasticsearch"]]]
       [:tbody
       (for [date (take 30 (periodic/periodic-seq (time/now) (time/hours 24)))]
           [:tr {:key (tf/unparse date-formatter date)}
            [:td (tf/unparse date-formatter date)]
            [:td "x"]
            [:td "x"]])]]]]]])

(defn about-page[]
  [:div
   (menu)
   [:p "This is the about text"]])


(defn current-page []
  [:div [(session/get :current-page)]])

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
                    (session/put! :current-page #'home-page))

(secretary/defroute "/about" []
                    (session/put! :current-page #'about-page))

;; -------------------------
;; History
;; must be called after routes have been defined

(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
      EventType/NAVIGATE
      (fn [event]
        (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

;; -------------------------
;; Initialize app

(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (hook-browser-navigation!)
  (mount-root))

(init!)

;(defn on-js-reload []
;  ;; optionally touch your app-state to force rerendering depending on
;  ;; your application
;  ;; (swap! app-state update-in [:__figwheel_counter] inc)
;)

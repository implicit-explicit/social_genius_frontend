(ns social_genius_frontend.core
  (:require [reagent.core :as reagent]
            [reagent.session :as session]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [secretary.core :as secretary :include-macros true]
            [ajax.core :refer [GET POST]])
  (:import goog.History))

(enable-console-print!)
(println "Executing frontend code")

;; App data
(defonce app-state (reagent/atom {:text "Social Genius"}))


;;--------------------------
;; Backend communication

(defn response-handler [response]
  (println "Received response from backend")
  (swap! app-state assoc :apps (get response "Apps"))
  (println "Response payload" (get @app-state :apps)))

(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text)))

(defn get-group [group]
  (println (str "Retrieving group: " (str "/groups/" group)))
  (GET (str "/groups/" group)
       {:handler response-handler
        :error-handler error-handler
        :response-format :json}))


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
   [:button {:on-click (fn [e] (.preventDefault e)
                         (get-group "Docker%20Randstad"))} "Get group"]])

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

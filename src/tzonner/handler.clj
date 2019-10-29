(ns tzonner.handler
  (:require [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer :all]
            [clojure.spec.alpha :as s]
            [ring.middleware.cors :refer [wrap-cors]]))

(def state (atom []))

(s/def ::id uuid?)
(s/def ::name string?)
(s/def ::city
  #(contains? #{"Alba Iulia" "Cluj Napoca" "Munich" "London"} %))
(s/def ::offset int?)
(s/def ::dst boolean?)
(s/def ::timezone (s/keys
  :req-un [::name ::city ::offset ::dst] :opt-un [::id]))

(s/def ::error string?)
(s/def ::error-map (s/keys :req-un [::error]))

(defn cors-md [hnd]
  (wrap-cors hnd :access-control-allow-origin [#"http://localhost:3449"]
    :access-control-allow-methods [:options :get :put :post :delete]))

(def app
  (api
    {:swagger
     {:ui "/"
      :spec "/swagger.json"
      :data {:info {:title "TimeZonner"
                    :description "TimeZonner CRUD via Swagger"}
             :tags [{:name "api", :description "tz apis because why not"}]}}}

    (context "/api" []
      :tags ["api"]
      :coercion :spec

      (GET "/timezones" []
        :return (s/coll-of ::timezone :into []) 
        :middleware [cors-md]
        :summary "List of timezones"
        (ok @state))

      (GET "/timezones/:id" []
        :path-params [id :- ::id]
        :middleware [cors-md]
        :return (s/or :tz ::timezone :err ::error-map) 
        :summary "Get in the timeZone"
        (if-let [found (->> @state
          (filterv #(= id (:id %)))
          first)]
          (ok found)
          (not-found {:error "No such timezone"})))
      
      (PUT "/timezones/:id" []
        :path-params [id :- ::id]
        :body [dstobj (s/keys :req-un [::dst])]
        :return (s/or :tz ::timezone :err ::error-map) 
        :middleware [cors-md]
        :summary "Change DST status"
        (if-let [found (->> @state
            (filterv #(= id (:id %)))
            first)]
          (let [modified (some->
              found
              (assoc :dst (-> dstobj :dst)))]
            (reset!
              state
              (map 
                #(if (= (:id %) id) modified %)
                @state))
            (println @state modified)
            (ok modified))
          (not-found {:error "No such timezone"})))
      
      (DELETE "/timezones/:id" []
        :path-params [id :- ::id]
        :return (s/or :tz ::timezone :err ::error-map) 
        :middleware [cors-md]
        :summary "Delete a timezone"
        (if-let [found (->> @state
            (filterv #(= id (:id %)))
            first)]
          (do
            (reset!
              state
              (filter #(not= id (:id %)) @state))
            (ok found))
          (not-found {:error "No such timezone"})))

      (POST "/timezones" []
        :return ::timezone
        :body [tz ::timezone]
        :middleware [cors-md]
        :summary "Create a timezone"
        (let [id (java.util.UUID/randomUUID)
          marked (assoc tz :id id)]
          (swap! state conj marked)
          (ok tz))))))

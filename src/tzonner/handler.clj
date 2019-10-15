(ns tzonner.handler
  (:require [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer :all]
            [clojure.spec.alpha :as s]))

(def state (atom []))

(s/def ::id uuid?)
(s/def ::name string?)
(s/def ::city
  #(contains? #{"Alba Iulia" "Cluj Napoca" "Munich" "London"} %))
(s/def ::offset int?)
(s/def ::timezone (s/keys
  :req-un [::name ::city ::offset] :opt-un [::id]))

(s/def ::error string?)
(s/def ::error-map (s/keys :req [::error]))

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
        :summary "List of timezones"
        (ok @state))

      (GET "/timezones/:id" []
        :path-params [id :- ::id]
        :return (s/or :tz ::timezone :err ::error-map) 
        :summary "Get in the timeZone"
        (if-let [found (->> @state
          (filterv #(= id (:id %)))
          first)]
          (ok found)
          (not-found {:error "No such timezone"})))

      (POST "/timezones" []
        :return ::timezone
        :body [tz ::timezone]
        :summary "Create a timezone"
        (let [id (java.util.UUID/randomUUID)
          marked (assoc tz :id id)]
          (swap! state conj marked)
          (ok tz))))))

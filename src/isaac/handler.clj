(ns isaac.handler
  (:require
   [compojure.core :refer :all]
   [isaac.github :as github]
   [compojure.handler :as handler]
   [compojure.route :as route]
   [clojure.data.json :as json])
  (:gen-class))

(defroutes app-routes
  (POST "/" {headers :headers body :body} (do
                                           (github/handle-post headers (json/read-str (slurp body) :key-fn keyword))
                                            "OK"))
  (route/not-found ""))

(def app
  (handler/site app-routes))

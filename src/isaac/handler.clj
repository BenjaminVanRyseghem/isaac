(ns isaac.handler
  (:require
   [clojure.data.json :as json]
   [clojure.java.io :as io]
   [compojure.core :refer :all]
   [compojure.handler :as handler]
   [compojure.route :as route]
   [isaac.github :as github]
   [isaac.hipchat :as hipchat])
  (:gen-class))

(def ^{:private true} info-path (io/file (-> (java.io.File. "info.json") .getAbsolutePath)))

(def ^{:private true} info (atom (json/read-str
                                  (slurp info-path)
                                  :key-fn keyword)))

;;
;; CORE
;;

(defroutes app-routes
  (GET "/" {} (hipchat/capabilities-descriptor @info))
  (POST "/" {headers :headers body :body}
        (do
          (if (= (get headers "user-agent") "HipChat.com")
            (let [new-info (hipchat/retrieve-token headers (json/read-str (slurp body) :key-fn keyword))
                  token (:token new-info)
                  room-id (:room-id new-info)]
              (swap! info assoc :token token :room-id room-id)
              (spit info-path (json/write-str @info))
              "200")
            (github/handle-post headers (json/read-str (slurp body) :key-fn keyword) @info))
          "200"))
  (route/not-found ""))

(def app
  (handler/site app-routes))

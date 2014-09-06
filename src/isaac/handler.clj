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

(declare info)
(def ^{:private true} basic-info (json/read-str
                                  (slurp (io/file (-> (java.io.File. "info.json") .getAbsolutePath)))
                                  :key-fn keyword))

;;
;; CORE
;;

(defroutes app-routes
  (GET "/" {} (hipchat/capabilities-descriptor basic-info))
  (POST "/" {headers :headers body :body}
        (do
          (if (= (get headers "user-agent") "HipChat.com")
            (let [new-info (hipchat/retrieve-token headers (json/read-str (slurp body) :key-fn keyword))
                  token (:token new-info)
                  room-id (:room-id new-info)]
              (def ^{:private true} info
                {:token token
                 :room-id room-id
                 :url (:url basic-info)})
              "200")
            (github/handle-post headers (json/read-str (slurp body) :key-fn keyword) info))
          "200"))
  (route/not-found ""))

(def app
  (handler/site app-routes))

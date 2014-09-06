(ns isaac.hipchat
  (:require [clj-http.client :as client]
            [clojure.data.json :as json])
  (:gen-class))

;;
;; PUBLIC
;;

(defn send-room-message
  [url room message color token]
  (let [body {"color" color
              "message" message}]
    (client/post (str url "/v2/room/" room "/notification?auth_token=" token)
                 {:body (json/write-str body)
                  :headers {"X-Api-Version" "2"}
                  :content-type :json
                  :socket-timeout 1000  ;; in milliseconds
                  :conn-timeout 1000    ;; in milliseconds
                  :accept :json})))

(defn send-private-message
  [url user message token]
  (let [body {"message" message}]
    (client/post (str url "/v2/user/" user "/message?auth_token=" token)
                 {:body (json/write-str body)
                  :headers {"X-Api-Version" "2"}
                  :content-type :json
                  :socket-timeout 1000  ;; in milliseconds
                  :conn-timeout 1000    ;; in milliseconds
                  :accept :json})))

(ns isaac.hipchat
  (:require [clj-http.client :as client]
            [clojure.data.json :as json])
  (:gen-class))


;;
;; PUBLIC
;;

(defn send-message
  [url message color room]
  (let [body {"color" color
              "message" message
              }]
  (client/post "https://foretagsplatsen.hipchat.com/v2/room/795864/notification?auth_token=HSCBTyrkZHdmWeJLopOd524oIOWWjWLSg4oaZgAg"
               {:basic-auth ["user" "pass"]
                :body (json/write-str body)
                :headers {"X-Api-Version" "2"}
                :content-type :json
                :socket-timeout 1000  ;; in milliseconds
                :conn-timeout 1000    ;; in milliseconds
                :accept :json})))

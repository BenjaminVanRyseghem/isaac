(ns isaac.hipchat
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]
            [ring.util.codec :as codec])
  (:gen-class))

;;
;; PUBLIC
;;

(defn retrieve-token [headers body]
  (let [data {:grant_type "client_credentials"
              :scope "send_notification"}
        json (:body (client/post "https://api.hipchat.com/v2/oauth/token"
                               {:basic-auth (str (:oauthId body) ":" (:oauthSecret body))
                                :form-params data}))
        response (json/read-str json :key-fn keyword)]
    {:token (:access_token response)
     :room-id (:roomId body)}))

(defn capabilities-descriptor
  [info]
  (json/write-str
   {:vendor {:url "benjamin.vanryseghem.com"
             :name "Benjamin Van Ryseghem"}
    :name "Isaac"
    :description "A HipChat dedicated robot."
    :key "com.benjaminvanryseghem.isaac"
    :links {:homepage "https://github.com/BenjaminVanRyseghem/isaac"
            :self (:callback-url info)}
    :capabilities {:hipchatApiConsumer {:scopes ["send_notification"]
                                        :name "Isaac"}
                   :installable {:allowGlobal false
                                 :callbackUrl (:callback-url info)}}}))

(defn get-rooms
  [url token]
  (let [full-url (str url "/v2/room?auth_token=" token)
        json (:body (client/get full-url))
        body (json/read-str json :key-fn keyword)]
    body))

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

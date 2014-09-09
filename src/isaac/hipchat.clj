(ns isaac.hipchat
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]
            [ring.util.codec :as codec])
  (:gen-class))

(def ^{:private true} token (atom 0))
(def ^{:private true} ttl (atom 0))
(def ^{:private true} oauth-id (atom 0))
(def ^{:private true} oauth-secret (atom 0))

(def ^{:private true} time-to-run 3590) ;; 10 minutes (60 * 10)

;;
;; PUBLIC
;;

(defn refresh-token []
  (println "Refresh token -- sleep for" @ttl "seconds")
  (Thread/sleep (* (- @ttl time-to-run) 1000))
  (println "Refresh token -- awakening" @token)
  (let [data {:grant_type "refresh_token"
              :refresh_token @token}
        json (:body (client/post (str "https://api.hipchat.com/v2/oauth/token")
                               {:basic-auth (str @oauth-id ":" @oauth-secret)
                                :form-params data}))
        response (json/read-str json :key-fn keyword)]
    (reset! token (:access_token response))
    (reset! ttl (:expires_in response)))
  (recur))


(defn retrieve-token [headers body]
  (reset! oauth-id (:oauthId body))
  (reset! oauth-secret (:oauthSecret body))
  (let [data {:grant_type "client_credentials"
              :scope "send_notification"}
        json (:body (client/post "https://api.hipchat.com/v2/oauth/token"
                               {:basic-auth (str @oauth-id ":" @oauth-secret)
                                :form-params data}))
        response (json/read-str json :key-fn keyword)]

    (reset! token (:access_token response))
    (reset! ttl (:expires_in response))
    (.start (Thread. refresh-token))
    (:roomId body)))

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
                                        :name "GitHub"}
                   :installable {:allowGlobal false
                                 :callbackUrl (:callback-url info)}}}))

(defn send-room-message
  [url room message color]
  (let [body {"color" color
              "message" message}]
    (client/post (str url "/v2/room/" room "/notification?auth_token=" @token)
                 {:body (json/write-str body)
                  :headers {"X-Api-Version" "2"}
                  :content-type :json
                  :socket-timeout 1000  ;; in milliseconds
                  :conn-timeout 1000    ;; in milliseconds
                  :accept :json})))

(defn send-private-message
  [url user message]
  (let [body {"message" message}]
    (client/post (str url "/v2/user/" user "/message?auth_token=" @token)
                 {:body (json/write-str body)
                  :headers {"X-Api-Version" "2"}
                  :content-type :json
                  :socket-timeout 1000  ;; in milliseconds
                  :conn-timeout 1000    ;; in milliseconds
                  :accept :json})))

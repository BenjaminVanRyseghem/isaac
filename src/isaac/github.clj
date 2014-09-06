(ns isaac.github
  (:require [isaac.hipchat :as hipchat])
  (:gen-class))


(def ^{:private true} image-regex #"[\s|\w]*!\[.*\]\((.*)\)[\s|\w]*")
;;
;; PRIVATE
;;

(defn- closed-pull-request-color [body]
  (if (get-in body [:pull_request :merged])
    "gray"
    "red"))

;; build messages

(defn- contains-image? [body]
  (not (nil? (re-matches image-regex (get-in body [:pull_request :body])))))

(defn- retrieve-image [body]
  (second (re-matches image-regex (get-in body [:pull_request :body]))))

(defn- build-user-link [body]
  (str "<a href=\"" (get-in body [:sender :html_url]) "\">" (get-in body [:sender :login]) "</a>"))

(defn- build-branch-name [body]
  (str "<b>" (:ref body) "</b>"))

(defn- build-branch-link [body]
  (str "<a href=\""
       (get-in body [:repository :html_url])
       "\">"
       (build-branch-name body) "</a>"))

(defn- build-repository-link [body]
  (str "<a href=\"" (get-in body [:repository :html_url]) "\">" (get-in body [:repository :full_name]) "</a>"))

(defn- build-pull-request-link [body]
  (str
   "<a href=\""
   (get-in body [:pull_request :html_url])
   "\">#"
   (:number body)
   "</a>"))

(defn- build-pull-request-repository-link [body]
  (str "<a href=\"" (get-in body [:pull_request :head :repo :html_url]) "\">" (get-in body [:pull_request :head :repo :full_name]) "</a>"))

(defn- build-pull-request-title-link [body]
  (str "<a href=\"" (get-in body [:pull_request :html_url]) "\"><b>" (get-in body [:pull_request :title]) "</b></a>"))


(defn- build-pull-request-desc [body]
  (str "<pre>" (get-in body [:pull_request :body]) "</pre>"))

(defn- build-pull-request-picture [body]
  (str "<img src=\"" (retrieve-image body) "height=\"300\">"))

(defn- build-head-link [body]
  (str "<a href=\""
       (get-in body [:pull_request :head :repo :html_url])
       "/commit/"
       (get-in body [:pull_request :head :sha])
       "\"><em>"
       (subs (get-in body [:pull_request :head :sha]) 0 8)
       "</em></a>"))

(defn- build-refs-desc [body]
  (str "<br>[base:<code> " (get-in body [:pull_request :base :ref]) "</code>"
       " head:<code> " (get-in body [:pull_request :head :ref]) "</code>]"))

(defn- build-closed-pull-request-message [body]
  (if (get-in body [:pull_request :merged])
    (str (build-user-link body)
         "<b> merged</b> the pull request "
         (build-pull-request-link body)
         " "
         (build-pull-request-repository-link body)
         ":"
         (build-pull-request-title-link body)
         " merging <code> "
         (get-in body [:pull_request :head :ref])
         "</code> into <code> "
         (get-in body [:pull_request :base :ref])
         "</code>")
    (str (build-user-link body)
         "<b> closed</b> the pull request "
         (build-pull-request-link body)
         " "
         (build-pull-request-repository-link body)
         ":"
         (build-pull-request-title-link body))))

;;
;; PUBLIC
;;

(defmulti handle-post (fn [x y z] (get x "x-github-event")))
(defmulti handle-delete (fn [x y z] (:ref_type y)))
(defmulti handle-create (fn [x y z] (:ref_type y)))
(defmulti handle-pull-request (fn [x y z] (:action y)))

;; DELETE

(defmethod handle-delete "branch" [headers body info]
  (hipchat/send-room-message (:url info)
                             (:room-id info)
                             (str (build-user-link body)
                                  " deleted the branch "
                                  (build-branch-name body)
                                  " of "
                                  (build-repository-link body))
                             "red"
                             (:token info)))

(defmethod handle-delete :default [headers body info]
  "IGNORED")

;; CREATE

(defmethod handle-create "branch" [headers body info]
  (hipchat/send-room-message (:url info)
                             (:room-id info)
                             (str (build-user-link body)
                                  " created the branch "
                                  (build-branch-link body)
                                  " of "
                                  (build-repository-link body))
                             "green"
                             (:token info)))

(defmethod handle-create :default [headers body info]
  "IGNORED")

;; PULL REQUEST

(defmethod handle-pull-request "opened" [headers body info]
  (hipchat/send-room-message (:url info)
                             (:room-id info)
                             (str (build-user-link body)
                                  " opened the pull request "
                                  (build-pull-request-link body)
                                  " on "
                                  (build-pull-request-repository-link body)
                                  ":"
                                  (build-pull-request-title-link body)
                                  (build-refs-desc body)
                                  (build-pull-request-desc body)
                                  (if (contains-image? body)
                                    (build-pull-request-picture body)))
                             "green"
                             (:token info)))

(defmethod handle-pull-request "synchronize" [headers body info]
  (hipchat/send-room-message (:url info)
                             (:room-id info)
                             (str (build-user-link body)
                                  " updated the pull request "
                                  (build-pull-request-link body)
                                  " on "
                                  (build-pull-request-repository-link body)
                                  ":"
                                  (build-pull-request-title-link body)
                                  " moving <code>HEAD</code> to "
                                  (build-head-link body))
                             "yellow"
                             (:token info)))

(defmethod handle-pull-request "closed" [headers body info]
  (hipchat/send-room-message (:url info)
                             (:room-id info)
                             (build-closed-pull-request-message body)
                             (closed-pull-request-color body)
                             (:token info)))

(defmethod handle-pull-request :default [headers body info]
  "IGNORED")

;;
;; CORE
;;

(defmethod handle-post "delete" [headers body info]
  (handle-delete headers body info))

(defmethod handle-post "create" [headers body info]
  (handle-create headers body info))

(defmethod handle-post "pull_request" [headers body info]
  (handle-pull-request headers body info))


(defmethod handle-post :default [headers body info]
  (println (get headers "x-github-event") "NOT YET SUPPORTED"))

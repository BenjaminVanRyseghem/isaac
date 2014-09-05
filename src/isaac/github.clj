(ns isaac.github
  (:require [clojure.data.json :as json]
            [isaac.hipchat :as hipchat])
  (:gen-class))


;;
;; PRIVATE
;;

(defn- build-user-link [body]
  (str "<a href=\"" (get-in body [:sender :html_url]) "\">" (get-in body [:sender :login]) "</a>"))

(defn- build-branch-name [body]
  (str "<b>" (:ref body) "</b>"))

(defn- build-repository-link [body]
  (str "<a href=\"" (get-in body [:repository :html_url]) "\">" (get-in body [:repository :full_name]) "</a>"))

;;
;; PUBLIC
;;

(defmulti handle-post #(str (get %1 "x-github-event") "-" (:ref_type %2)))

(defmethod handle-post "delete-branch" [headers body]
  (hipchat/send-message ""
                        (str (build-user-link body) " deleted the branch " (build-branch-name body) " of " (build-repository-link body))
                        "red"
                        ""))

(defmethod handle-post :default [h b]
  (println (str (get h "x-github-event") "-" (:ref_type b)) "NOT YET SUPPORTED"))

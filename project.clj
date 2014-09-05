(defproject hipchat-github-bot "0.1.0-SNAPSHOT"
  :description "HipChat dedicated robot"
  :url "https://github.com/BenjaminVanRyseghem/isaac"
  :license {:name "General Public License version 3"
            :url "http://www.gnu.org/copyleft/gpl.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [compojure "1.1.8"]
                 [clj-http "1.0.0"]
                 [org.clojure/data.json "0.2.5"]
                 [ring/ring-codec "1.0.0"]]
  :plugins [[lein-ring "0.8.11"]]
  :ring {:handler isaac.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-codec "1.0.0"]
                        [ring-mock "0.1.5"]]}})

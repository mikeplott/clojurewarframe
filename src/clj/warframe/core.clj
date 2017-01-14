(ns warframe.core
  (:require [clojure.string :as str]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.file :refer [wrap-file]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.util.response :refer [redirect]]
            [clojure.java.io :as io])
  (:gen-class))

(defonce web-server (atom nil))

(defn start-web-server! [handler]
  (reset! web-server (run-jetty handler {:port 3000 :join? false})))

(defn web-handler [request]
  (when (= (:uri request) "/")
    (redirect "/index.html"))
  (when (= (:uri request "/dashboard"))
    (redirect "/dashboard.html")))

(defn dev-main []
  (when-not @web-server
    (.mkdirs (io/file "target" "public"))
    (start-web-server! (wrap-file web-handler "target/public"))))

(def file-name "items.txt")

(defn read-items []
  (let [items (str/split-lines (slurp file-name))
        items (map (fn [line]
                     (str/split line #"\|"))
                items)
        header (first items)
        items (rest items)
        items (map (fn [line]
                     (zipmap header line))
                items)]
    items))

(def db-spec
  {:classname "org.h2.Driver"
   :subprotocol "h2:file"
   :subname "db/warframe"})

(def the-db
  {:classname "org.postgresql.Driver"
   :subprotocol "postgresql"
   :subname "//localhost:5432/warframe"
   :user "michaelplott"
   :password "dubose"})

(defn create-ext-tables []
   (sql/db-do-commands the-db
      (sql/create-table-ddl
        :item_list [[:id "SERIAL"]
                    [:item_name "TEXT"]
                    [:category "TEXT"]
                    [:void_relic "TEXT"]])))

(defn insert-ext []
    (let [items (read-items)]
      (map (fn [item]
             (sql/insert! the-db
               :item_list
               {:item_name (get item "item_name")
                :category (get item "category")
                :void_relic (get item "void_relic")}))
        items)))

(defn create-tables []
   (sql/db-do-commands db-spec
      (sql/create-table-ddl
        :item_list [[:id "IDENTITY"]
                    [:item_name "VARCHAR"]
                    [:category "VARCHAR"]
                    [:void_relic "VARCHAR"]]))
   (let [items (read-items)]
        (map (fn [item]
               (sql/insert! db-spec
                 :item_list
                 {:item_name (get item "item_name")
                  :category (get item "category")
                  :void_relic (get item "void_relic")}))
          items)))

(defn test-import []
  (let [test-data
        (sql/query db-spec ["SELECT * FROM item_list"])]
    (println test-data)
    test-data))

(defn -main [& args]
  (start-web-server! (wrap-resource web-handler "public")))











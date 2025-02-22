#!/usr/bin/env bb
(require '[clojure.string :as str])

(defn load-data [filename]
  (->> (slurp filename)
       (str/split-lines)
       (map #(vec (str/split % #",")))))

(defn prepend [& args]
  (concat (butlast args) (last args)))

(defn cut-f [columns row]
  (map #(nth row %) columns))

(def shopping (load-data "shopping_trends.csv"))
(def items (rest (load-data "item.csv")))
(def item-map (into {} (map (juxt rest first) items)))

(def columns [5 9 10 12 13 14 15])
(def item-columns [3 4 7 8])

(println (str/join "," (prepend "customer_id" "item_id" (cut-f columns (first shopping)))))
(doseq [row (rest shopping)]
  (let [customer-id (first row)
        item-id (get item-map (cut-f item-columns row))
        purchase (cut-f columns row)]
    (println (str/join "," (prepend customer-id item-id purchase)))))

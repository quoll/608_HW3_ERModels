#!/usr/bin/env bb
(require '[clojure.string :as str])
(require '[clojure.java.io :as io])

(def input-file "shopping_trends.csv")
(def item-file "item.csv")
(def purchase-file "purchase.csv")
(def customer-file "customer.csv")
(def payment-file "payment_method.csv")

(def purchase-columns [5 9 10 12 13 14 15])
(def item-columns [3 4 7 8])
(def customer-columns [0 1 2 6 11 16 17 18])
(def payment-columns [12 17])

(defn index-of [v coll]
  (first (keep-indexed #(when (= v %2) %1) coll)))

(def preferred-payment-method (index-of 17 customer-columns))
(def payment-method (index-of 12 purchase-columns))

(defn load-data [filename]
  (->> (slurp filename)
       (str/split-lines)
       (map #(vec (str/split % #",")))))

(defn prepend [& args]
  (concat (butlast args) (last args)))

(defn select [columns row]
  (mapv #(nth row %) columns))

(defn cut-f [columns data]
  (map #(select columns %) data))

;; Load the data
(def shopping (load-data "shopping_trends.csv"))
(def shopping-headers (first shopping))
(def shopping-data (rest shopping))

;; Extract the Payment Methods
(def payment-methods (->> (cut-f payment-columns shopping-data) flatten set sort (map-indexed (fn [i x] [(inc i) x]))))
(with-open [w (io/writer payment-file)]
  (binding [*out* w]
    (println (str/join "," (prepend "id" (select (take 1 payment-columns) shopping-headers))))
    (doseq [method payment-methods]
      (println (str/join "," method)))))
(def payment-map (into {} (map (juxt second first) payment-methods)))

;; Extract the Items
(def items (->> (cut-f item-columns shopping-data) set sort (map-indexed (fn [i data] (into [(inc i)] data)))))
(with-open [w (io/writer item-file)]
  (binding [*out* w]
    (println (str/join "," (prepend "id" (select item-columns shopping-headers))))
    (doseq [item items]
      (println (str/join "," item)))))
(def item-map (into {} (map (juxt rest first) items)))

;; Extract the Customers
(with-open [w (io/writer customer-file)]
  (binding [*out* w]
    (println (str/join "," (select customer-columns shopping-headers)))
    (doseq [row shopping-data]
      (let [customer (select customer-columns row)
            ;; update the string for the preferred payment method with the index of the method
            pcustomer (update customer preferred-payment-method payment-map)]
        (println (str/join "," pcustomer))))))

;; Extract the Purchases
(with-open [w (io/writer purchase-file)]
  (binding [*out* w]
    (println (str/join "," (prepend "customerId" "itemId" (select purchase-columns shopping-headers))))
    (doseq [row shopping-data]
      (let [customer-id (first row)
            item-id (get item-map (select item-columns row))
            purchase (select purchase-columns row)
            ;; update the string for the payment method with the index of the method
            ppurchase (update purchase payment-method payment-map)]
        (println (str/join "," (prepend customer-id item-id ppurchase)))))))


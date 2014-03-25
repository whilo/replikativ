(ns geschichte.platform
  (:require goog.net.WebSocket
            [cljs.reader :refer [read-string]]
            [cljs.core.async :as async :refer (take! put! close! chan)]))

(defn log [& s]
  (.log js/console (apply str s)))

;; taken from https://github.com/whodidthis/cljs-uuid-utils/blob/master/src/cljs_uuid_utils.cljs
;; TODO check: might not have enough randomness (?)
(defn make-random-uuid
  "(make-random-uuid) => new-uuid
   Arguments and Values:
   new-uuid --- new type 4 (pseudo randomly generated) cljs.core/UUID instance.
   Description:
   Returns pseudo randomly generated UUID,
   like: xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx as per http://www.ietf.org/rfc/rfc4122.txt.
   Examples:
   (make-random-uuid) => #uuid \"305e764d-b451-47ae-a90d-5db782ac1f2e\"
   (type (make-random-uuid)) => cljs.core/UUID"
  []
  (letfn [(f [] (.toString (rand-int 16) 16))
          (g [] (.toString (bit-or 0x8 (bit-and 0x3 (rand-int 15))) 16))]
    (UUID. (.toString (.append (goog.string.StringBuffer.)
       (f) (f) (f) (f) (f) (f) (f) (f) "-" (f) (f) (f) (f)
       "-4" (f) (f) (f) "-" (g) (f) (f) (f) "-"
       (f) (f) (f) (f) (f) (f) (f) (f) (f) (f) (f) (f))))))


(defn uuid
  ([] (make-random-uuid))
  ([val] :TODO-UUID))


(defn now []
  (js/Date.))


(defn client-connect! [ip port in out]
  (let [channel (goog.net.WebSocket.)
        opener (chan)]
    (doto channel
      (.listen goog.net.WebSocket.EventType.MESSAGE
               (fn [evt]
                 (log "receiving: " (-> evt .-message))
                 (put! in (-> evt .-message read-string))))
      (.listen goog.net.WebSocket.EventType.OPENED
               (fn [evt] (close! opener)))
      (.listen goog.net.WebSocket.EventType.ERROR
               (fn [evt] (log "ERROR:" evt)))
      (.open (str "ws://" (str ip ":" port))))
    ((fn sender [] (take! out
                         (fn [m]
                           (log "sending: " m)
                           (.send channel (str m))
                           (sender)))))
    opener))

(defn start-server! [ip port]
  (throw "No server functionality in js yet. Node port welcome."))

(comment
  (require '[clojure.browser.repl]))

(ns re-pressed.core
  (:require
   [re-frame.core :as rf]
   [re-pressed.impl]))

(rf/reg-fx
 ::panic!
 (fn [error]
   (js/console.error error)))

(rf/reg-event-fx
 ::add-keyboard-event-listener
 ;; event-type can be:
 ;; "keydown",
 ;; "keypress", or
 ;; "keyup"
 (fn add-keyboard-event-listener
   [_ [_ event-type & {:as args-map}]]
   (let [{:keys [prefix]} args-map]
     (if prefix
       {::keyboard-event {:event-type event-type :arguments args-map}}
       {::panic! (new js/Error ":prefix key must be provided while initializing Keyboard Event Listener!")}))))

(rf/reg-event-fx
    ::set-keydown-rules
    (fn [{:keys [db]}
         [_ {:keys [prefix
                    event-keys
                    clear-keys
                    always-listen-keys
                    prevent-default-keys]
             :as   opts}]]
      (if prefix
        {:db (-> db
                 (assoc-in [prefix ::keydown :keys] nil)
                 (assoc-in [prefix ::keydown :event-keys] event-keys)
                 (assoc-in [prefix ::keydown :clear-keys] clear-keys)
                 (assoc-in [prefix ::keydown :always-listen-keys] always-listen-keys)
                 (assoc-in [prefix ::keydown :prevent-default-keys] prevent-default-keys))}
        {::panic! (new js/Error ":prefix key must be provided while defining Keyboard Rules!")})))


   (rf/reg-event-fx
    ::set-keypress-rules
    (fn [{:keys [db]}
         [_ {:keys [prefix
                    event-keys
                    clear-keys
                    always-listen-keys]
             :as   opts}]]
      (if prefix
        {:db (-> db
                 (assoc-in [prefix ::keypress :keys] nil)
                 (assoc-in [prefix ::keypress :event-keys] event-keys)
                 (assoc-in [prefix ::keypress :clear-keys] clear-keys)
                 (assoc-in [prefix ::keypress :always-listen-keys] always-listen-keys))}
        {::panic! (new js/Error ":prefix key must be provided while defining Keyboard Rules!")})))


   (rf/reg-event-fx
    ::set-keyup-rules
    (fn [{:keys [db]}
         [_ {:keys [prefix
                    event-keys
                    clear-keys
                    always-listen-keys]
             :as   opts}]]
      (if prefix
        {:db (-> db
                 (assoc-in [prefix ::keyup :keys] nil)
                 (assoc-in [prefix ::keyup :event-keys] event-keys)
                 (assoc-in [prefix ::keyup :clear-keys] clear-keys)
                 (assoc-in [prefix ::keyup :always-listen-keys] always-listen-keys))}
        {::panic! (new js/Error ":prefix key must be provided while defining Keyboard Rules!")})))

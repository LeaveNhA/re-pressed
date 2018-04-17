# re-pressed

> **Arthur**: Shut up, will you, SHUT UP!
> **Man**:	Aha!  Now we see the violence inherent in the system!
> **Arthur**: SHUT UP!
> **Man**:	Come and see the violence inherent in the system!	HELP, HELP, I'M BEING REPRESSED!
> **Arthur**: Bloody PEASANT!
> **Man**:	Oh, what a giveaway!  Did'j'hear that, did'j'hear that, eh?  That's what I'm all about!  Did you see 'im repressing me?  You saw it, didn't you?!
> - Monty Python and the Holy Grail

Re-pressed is a library that handles keyboard events
for [re-frame](https://github.com/Day8/re-frame) applications.

```clojure
[re-pressed "0.2.0"]
```

**Note**: For now, this library should be considered *alpha quality* and the api is still settling.

![re-pressed gif not found](re-pressed.gif)

# The Problem

If you aren't careful, it is easy to add a bunch of keyboard event
listeners scattered throughout your application. When these listeners
collide, this can lead to unexpected and hard to debug behavior.

In addition, the current state of how to identify a keyboard event in
a cross-browser compatible way can be quite cumbersome ... you will
likely be asking yourself, "Should I use keyCode, key, which, etc?".

# Re-pressed's Solution

With re-pressed, you only set up one keyboard event listener when your
application starts, with `::rp/add-keyboard-event-listener`. However,
that does not mean that you are locked in to one set of rules for how
to handle keyboard events. By dispatching `::rp/set-keydown-rules`,
`::rp/set-keypress-rules`, or `::rp/set-keyup-rules`, you can update
the rules dynamically.

In addition, jQuery is able to ensure cross-browser compatibility with
their `which` attribute. Re-pressed trusts that jQuery will do a good
job at keeping this current and uses it under the hood.

# API

### `::rp/add-keyboard-event-listener`

`::rp/add-keyboard-event-listener` adds the keyboard event listener to
your application. Needs to be dispatched **only once**, when the
application *first* loads.

There are three options, and you can use more than one if you'd like:

```clojure
(rf/dispatch-sync [::rp/add-keyboard-event-listener "keydown"])

;; or
(rf/dispatch-sync [::rp/add-keyboard-event-listener "keypress"])

;; or
(rf/dispatch-sync [::rp/add-keyboard-event-listener "keyup"])
```

### `::rp/set-keydown-rules`

`::rp/set-keydown-rules` takes a hash-map of `:event-keys`,
`:clear-keys`, and `:prevent-default-keys` and listens for *keydown*
events.

- For `:event-keys`, there is a vector of *event + key combo* vectors.
  If any of the key combos are true, then the event will get
  dispatched.
- For `:clear-keys`, there is a vector of *key combo* vectors. If any
  of the key combos are true, then the recently recorded keys will be
  cleared.
- For `:always-listen-keys`, there is a vector of just *keys*. If any
  of the keys are pressed, then that key will always be recorded by
  re-pressed. *By default, keys are ignored when pressed inside of an input, select, or textarea.*
- For `:prevent-default-keys` there is a vector of just *keys*. If any
  of the keys are pressed, then the default browser action for that
  key will be prevented.

This is a description of the shape:

```clojure
(rf/dispatch
 [::rp/set-keydown-rules
  {:event-keys [
                [<event vector>
                 <key-combo vector>
                 ...
                 <key-combo vectorN>]
                ]

   :clear-keys [<key-combo vector>
                ...
                <key-combo vectorN>]

   :always-listen-keys [<key>
                        ...
                        <keyN>]

   :prevent-default-keys [<key>
                          ...
                          <keyN>]

   }])
```

Here is an example:

```clojure
(re-frame/dispatch
 [::rp/set-keydown-rules
  {;; takes a collection of events followed by key combos that can trigger the event
   :event-keys [
                ;; Event & key combos 1
                [;; this event
                 [:some-event-id1]
                 ;; will be triggered if
                 ;; enter
                 [{:which 13}]
                 ;; or delete
                 [{:which 46}]]
                ;; is pressed

                ;; Event & key combos 2
                [;; this event
                 [:some-event-id2]
                 ;; will be triggered if
                 ;; tab is pressed twice in a row
                 [{:which 9} {:which 9}]
                 ]]

   ;; takes a collection of key combos that, if pressed, will clear
   ;; the recorded keys
   :clear-keys
   ;; will clear the previously recorded keys if
   [;; escape
    [{:which 27}]
    ;; or Ctrl+g
    [{:which   71
      :ctrlKey true}]]
   ;; is pressed

   ;; takes a collection of keys that will always be recorded
   ;; (regardless if the user is typing in an input, select, or textarea)
   :always-listen-keys
   ;; will always record if
   [;; enter
    {:which 13}]
   ;; is pressed

   ;; takes a collection of keys that will prevent the default browser
   ;; action when any of those keys are pressed
   ;; (note: this is only available to keydown)
   :prevent-default-keys
   ;; will prevent the browser default action if
   [;; Ctrl+g
    {:which   71
      :ctrlKey true}]
    ;; is pressed
   }])
```


For `:event-keys`, `:clear-keys`, `:always-listen-keys`, and
`:prevent-default-keys`, the keys take the following shape:

```clojure
{:which    <int>
 :altKey   <boolean>
 :ctrlKey  <boolean>
 :metaKey  <boolean>
 :shiftKey <boolean>
 }
```

For `:event-keys`, the *event* will be called with a few things *conj*ed
on to the end of the event vector. For example:

```
;; this event
[:some-event-id1]

;; will be dispatched as
[:some-event-id1 js-event keyboard-keys]
```

Where:

- `js-event` is the javascript event (i.e. jQuery event) of the most recently pressed key
- `keyboard-keys` is a collection of the recently pressed keys taking
  the shape of the clojurescript hash-map described above.


### `::rp/set-keypress-rules`

Listens to *keypress* events, otherwise it is the same as `::rp/set-keydown-rules` (except `:prevent-default-keys` is not supported).

### `::rp/set-keyup-rules`

Listens to *keyup* events, otherwise it is the same as `::rp/set-keydown-rules` (except `:prevent-default-keys` is not supported).

# Usage

Create a new re-frame application.

```
lein new re-frame foo
```

Add the following to the `:dependencies` vector of your *project.clj*
file.

```clojure
[re-pressed "0.2.0"]
```

Then require re-pressed in the core namespace, and add the
`::rp/add-keyboard-event-listener` event.

```clojure
(ns foo.core
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]

            ;; Add this (1 of 2)
            [re-pressed.core :as rp]

            [foo.events :as events]
            [foo.views :as views]
            [foo.config :as config]
            ))

(defn dev-setup []
  (when config/debug?
    (enable-console-print!)
    (println "dev mode")))

(defn mount-root []
  (re-frame/clear-subscription-cache!)
  (reagent/render [views/main-panel]
                  (.getElementById js/document "app")))

(defn ^:export init []
  (re-frame/dispatch-sync [::events/initialize-db])

  ;; And this (2 of 2)
  (re-frame/dispatch-sync [::rp/add-keyboard-event-listener "keydown"])

  (dev-setup)
  (mount-root))
```

Notes:

- You can pass `"keydown"`, `"keypress"`, or `"keyup"` to
  `::rp/add-keyboard-event-listener`.
- You need to dispatch `::rp/add-keyboard-event-listener` from the
  `init` function instead of `mount-root`. `init` is ran **once** when
  the application loads, and `mount-root` runs **everytime** figwheel
  updates the application. This is significant, because you only want
  to add one event listener!

Next, you will need to dispatch a `::rp/set-keydown-rules`,
`::rp/set-keypress-rules`, or `::rp/set-keyup-rules` event somewhere.
Personally, I like dispatching this in my routes file (because I may
want to handle keyboard events differently on each page).

## Gotchas

- For keypress events, you only have access to things like letters and
  numbers. This is unlike keydown and keyup events, where you have
  access to more things like the Escape key.
- Using `:prevent-default-keys` only works with
  `::rp/set-keydown-rules`. This is because the default action will
  happen before keypress and keyup events happen.
- Certain browser default actions cannot be overwritten, like `ctrl+n`
  in chrome.

## Questions

If you have questions, I can usually be found hanging out in
the [clojurians](http://clojurians.net/) #reagent slack channel (my
handle is [@gadfly361](https://twitter.com/gadfly361)).

## License

Copyright © 2018 Matthew Jaoudi

Distributed under the The MIT License (MIT).

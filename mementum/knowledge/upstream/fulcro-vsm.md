---
type: Reference
title: Fulcro — VSM Lambda Document
status: active
tags: [fulcro, clojure, clojurescript, vsm, lambda, generated, upstream]
---

# Fulcro — VSM Lambda Document

Extracted from 75 source files, 23,433 lines.
Root namespace: `com.fulcrologic.fulcro`

Fulcro is a full-stack web framework in Clojure(Script). It unifies client and server through:
- **declarative component model** (CLJC protocols & lifecycles)
- **normalized state** with EQL queries (Clojure graph queries)
- **transaction processing** (mutations + network remotes)
- **routing & UISM** (UI state machines)

## S5 — Identity (Protocols & Public API)

```clojure
;; Component protocol — the system IS this
PROTOCOL IOptions
  has-feature?(this, query) → component-options(this, query)(this)
  component-options(this, key) → fn? | map?

PROTOCOL Ident
  ident(this, props) → [table-kw, id]

PROTOCOL Query
  query(this) → eql-query-vector | nil

PROTOCOL InitialAppState
  initial-state(clz, params) → tree-structure

PROTOCOL PreMerge
  pre-merge(this, {state-map, data-tree, query}) → data

;; Computed values — pass data through hierarchy
λ computed(props, computed-map). computed? → assoc(meta, :computed, computed-map)
λ get-computed(props). props → meta → :computed | {}

;; Application identity
λ runtime-atom(app). app → :com.fulcrologic.fulcro.application/runtime-atom
λ state-atom(app). app → :com.fulcrologic.fulcro.application/state-atom
λ any->app(x). component-instance? → :props/:fulcro$app | fulcro-app? → x | deref?

;; Props & query access
λ props(component). isoget(component, [:props, :fulcro$value])
λ get-query(clz, state-map). has-feature?(clz, :query) → clz/query(state-map)
λ get-ident(clz, props). has-feature?(clz, :ident) → clz/ident(props)
```

## S4 — Intelligence (Algorithms & Adaptation)

### Normalization & Denormalization

```clojure
;; EQL tree ↔ normalized DB
λ tree->db(query, tree, &, transform). 
  eql/query->ast(query) → normalize*(query, tree, {}, {}, transform)
  
λ db->tree(query, starting-entity, state-map).
  ast←eql/query->ast(query)
  denormalize(ast, starting-entity, state-map, {})

;; Join topology — shared across all normalization
λ join?(expr). map?(expr)
λ lookup-ref?(v). vector?(v) ∧ 2=|v| ∧ keyword?(first(v))
λ follow-ref(state-map, [table, id]). state-map[[table, id]]
```

### Transaction Processing Pipeline

```clojure
;; Transact! → queue → dispatch → network → merge → render
λ transact!(app-or-component, tx, &, {:keys [component, ref, synchronous?]}).
  optimistic? ∧ sync? → mutations run immediately
  | ¬optimistic? → queue until remotes respond
  mutations → action(env) + remote(env) dispatches

;; Mutation environment
λ build-env(app, tx-node, {:keys [ast, state-before-action]}). 
  {app, state, state-atom, dispatch, transact!, result, ref, 
   com.fulcrologic.fulcro.algorithms.tx-processing/options}

;; Dispatch: mutate is the multimethod
MULTI mutate(env). dispatch on (:dispatch-key (:ast env))
  mutate returns: {action: (fn [env]) | nil,
                   result-action: (fn [env]) | nil,
                   remote: (fn [env] → false | true | ast) | false}

;; Action queue processing
λ process-queue!({state-atom, runtime-atom}).
  active-queue ← app/config/active-queue
  ∀ tx-node: run-actions! → queue-sends! → distribute-results! → render!

λ fully-complete?(app, tx-node). ∀ element ∈ elements: 
  action ✓ ∧ (desired-remotes ✓) → element complete
```

### Merging & State Updates

```clojure
;; Merge result into state
λ merge-component!(app, component, object-data, &, named-parameters).
  ∃ident(component) → merge-component(state, component, data, {...})
  targeted-refs ← named-parameters
  render!(app)

λ merge-component(state, comp, data, &, {:keys [remove-missing?]}).
  query ← get-query(comp, state)
  normalized ← tree->db(query, data)
  state ← merge*(state, [{query}], {query: data})
  integrate targeting params → state

;; Deep merge aware of tempids
λ sweep-merge(target, source). 
  ∀[k, v] ∈ source:
    tempid?(v) → dissoc(target, k)
    | leaf?(v) → sweep-one(v) into target[k]
    | map?(v) → recursive sweep-merge
```

### Form State Machines

```clojure
;; Form configuration in query: [{:form-config-join [...fields]}]
λ derive-form-info(class, {state-map}).
  fields ← get-form-fields(class) ∩ prop-keys
  subforms ← get-form-fields(class) ∩ join-keys
  ∀ field: default validity = unchecked

;; Form updates drive validity states
λ toggle!(component, field). transact!(component, [{:toggle ({:field})}])
λ set-value!(comp, field, v). transact!(comp, [{:set-value ({:field, :value})}])
```

### Data Fetch (Lazy Load Pattern)

```clojure
;; Async load with loading markers & caching
λ load!(app, {:keys [query target ident on-load on-error simulate-latency?]}).
  remote ← :default
  set-load-marker! → loading
  network → result → on-load(app, result, tx-result) → merge
  | error → on-error(app, error)

;; Result handler
λ load-action-data([result-target], response).
  ∃ response → merge-with-target
  | ¬ → set-target to {} (nil-like)
```

### Tempid Resolution

```clojure
;; Mutations return tempid→realid mapping
λ tempid(seed). unique-id (symbol) with seed metadata
λ tempid?(v). symbol?(v) ∧ some->(meta, :tempid)
λ resolve-tempids(data-structure, tid→rid). 
  prewalk-replace(tid→rid, data-structure)
```

## S3 — Control (Lifecycle & Initialization)

### Application Creation

```clojure
;; Create app with root component, initial state, remotes
λ fulcro-app({:keys [root-component, initial-db, remotes, 
                      render-root!, hydrate-root!, query-processor]}).
  state-atom ← atom(initial-db)
  runtime-atom ← atom({root-class, render-listeners, etc})
  config ← {active-queue, send-queues, submission-queue}
  ∀ remote: {transmit!, abort!} required
  → app (map with atoms)

;; Mount to DOM
λ mount!({app}, dom-node, &, {:keys [force-root?]}).
  root-factory ← computed-factory(root-class)
  root-element ← root-factory({})
  render!(app)
  react.dom/render(root-element, dom-node)

;; Hydrate (server-rendered HTML)
λ set-root!({app}, root-component, &, {:keys [hydrate?]}).
  if hydrate? → ah/app-algorithm(app, :hydrate-root!)
  | ¬ → ah/app-algorithm(app, :render-root!)
  force-refresh? → enable-forced-refresh!(1000)
  render!(app)
```

### State Initialization

```clojure
;; Build initial state tree from components
λ initialize-state!({app}, root-component).
  initial-db ← @state-atom
  root-query ← get-query(root-component, initial-db)
  initial-tree ← get-initial-state(root-component)
  
  db-from-ui ← tree->db(root-query, initial-tree, true, 
                        pre-merge-transform(initial-tree))
  db-from-ui ← merge-alternate-union-elements(db-from-ui, root-component)
  
  db ← deep-merge(initial-db, db-from-ui)
  reset!(@state-atom, db)

;; Get component initial state (compose child states)
λ get-initial-state(clz, &, params).
  has-initial-app-state?(clz) 
    → clz/initial-state(params)
    | ¬ → clz has query with idents
      → {:?default-table [:uuid]}
```

### Rendering & Refresh

```clojure
;; Render schedule — batched/debounced
λ render!({app}, &, {:keys [force-root?, hydrate?]}).
  root-class ← @runtime-atom :root-class
  root-factory ← computed-factory(root-class)
  
  r! ← hydrate? → hydrate-root! | render-root!
  
  app-root ← r!(root-factory({}), mount-node)

;; Mark for re-render (schedule, don't render immediately)
λ schedule-render!({app}). 
  swap!(runtime-atom, assoc, :schedule-render?, true)
  after-frame → render!(app)

λ add-render-listener!({app}, nm, listener-fn).
  listener-fn called after every render with: {app, timestamp}
```

### Routing & UISM

```clojure
;; Dynamic routing state machine
MULTI get-dynamic-router-target(screen-kw). 
  returns Fulcro component class for screen

;; Current route query
λ current-route(state-map, router-id). 
  state-map[[ROUTERS-TABLE, router-id, :current-route]]

;; Navigation
λ route!(app-or-component, target-ident). 
  transact!(app, [{:com.fulcrologic.fulcro.routing.dynamic-routing/route-to ({target})}])

;; UI State Machine (UISM) — statechart integration
PROTOCOL FulcroSM (= UISM events)
  trigger!(app, sm-id, event-key) → transact mutation
  state(app, sm-id) → current-state-kw | nil
```

## S2 — Coordination (Component & DSL Composition)

### Components as Data

```clojure
;; Defui is syntactic sugar for component + defsc
λ defsc(name, & {:keys [query, ident, initial-state, render, pre-merge, 
                         will-enter, will-leave, allow-route-change?]}).
  generates: 
    - name (component class)
    - computed-factory(name, {qualifier, keyfn}) → fn
    - ui-name (factory function)

;; Query composition — join children in parent query
λ comp(query(ComponentChild, params)) → in-parent-query
  ∨ ComponentChild/query (static: symbol) 
  ∨ ∃ get-query(ComponentChild, state-map)

;; Props threading — parent passes to child
λ computed-factory(component-class, &, {:keys [qualifier, keyfn]}).
  qualifier → disambiguate query IDs
  keyfn({props}) → unique :react-key
  → factory-fn(props, &, children)

;; Higher-order components
λ hoc-factory(fulcro-class, hoc-fn). 
  hoc-fn wraps react component in wrapped component
  → wrapped factory fn
```

### Element Construction (DOM)

```clojure
;; Server & client DOM — isomorphic
λ div([&, classes | {:keys [classes, id, style, ...] :as props}], 
      & children).
  creates hiccup: [:div {...props} ...children]

;; DOM helpers
λ element?(x). react/isValidElement(x) | instance?(IReactDOMElement, x) 
              | vector?(x) ∧ keyword?(first(x))

λ element-attrs(elem). second(elem) if map? else {}
λ element-children(elem). subvec(elem, if-map? 2 else 1)

;; Conditional rendering
λ element-with-attrs(elem, attrs). element?(elem) 
  → second-item ← second(elem)
  → map?(second-item) → assoc(second-item, attrs)
  | ¬ → {attrs}
```

### Query Building & Manipulation

```clojure
;; EQL manipulation
λ eql/query->ast(query) → ast-map{type, children, dispatch-key, key, ...}
λ eql/ast->query(ast) → query-vector

;; Query composition helpers
λ join-key(expr). ffirst(expr) if join else expr | first(expr)
λ union?(expr). map?(expr) ∧ keys are keywords
λ recursive?(query). integer?(query) | quote(...) = query

;; Mark missing data (for result validation)
λ mark-missing(result, query) → result with :not-found markers
```

## S1 — Operations (Concrete Functions & Utilities)

### State Access & Update

```clojure
λ current-state({app}). @state-atom
λ app->remote-names({app}). remotes → keys → set
λ app->remotes({app}). @runtime-atom :remotes

λ swap-state!({app}, f, &, args). 
  swap!(state-atom, f, args)

λ update-caller!({state, ref}, &, args). 
  apply(swap!, state, update-in, ref, args)
```

### Mutation Utilities

```clojure
;; Mutation declaration
RECORD Mutation {:symbol}
λ mutation-declaration?(expr). = Mutation (type expr)
λ mutation-symbol(expr). expr :symbol

;; Common mutations
λ toggle!({comp}, field). transact! [{:toggle ({:field})}]
λ set-value!({comp}, field, v). transact! [{:set-value ({:field, :value})}]
λ returning({mutation-ast}, query). 
  assoc mutation :result-action-return-value-keys
```

### Component Navigation

```clojure
λ get-parent({component}). isoget(component, [:props, :fulcro$parent])
λ get-root({component}). 
  loop through parents until none remains

λ component-name(class). isoget(class, :displayName)
λ react-type(x). or(gobj/get(x, "type"), type(x))
λ component-type(x). if class? → x else :fulcro$class
```

### Inspection & Debugging

```clojure
λ transact!!(app-or-comp, tx). 
  like transact! but waits (blocking in headless; 
  uses post-processing in browser)

λ tx-status!({app}). 
  print submission-queue, active-queue, send-queues

λ captured-transactions({app}). 
  @runtime-atom :captured-transactions → list (dev mode)
```

### Network Remotes

```clojure
;; Remote abstraction
PROTOCOL Remote
  transmit!(remote, send-node) → callback({status-code, body})
  abort!(remote, send-node-id) → void

;; HTTP remote (common)
λ make-request-impl(remote-name, send-node, xhrio, &, opts).
  GET/POST AST as EQL → server → transit response → callback

;; Sync remote (test)
λ sync-remote(handler-fn, &, {:keys [simulate-latency-ms]}).
  calls handler-fn immediately (or with delay)

;; Ring (full-stack testing)
λ ring-remote(ring-handler, &, 
              {:keys [uri, method, encode-fn, decode-fn]}).
  encode tx as EQL → ring-handler → decode response
```

### Server-Side Rendering

```clojure
λ render-to-str(root-component). 
  render component tree to HTML string (server)

λ render-to-string(component, state).
  component render(props) → hiccup → HTML

λ render-unescaped-html!(sb, {:keys [__html]}). 
  append dangerouslySetInnerHTML content
```

### Utilities

```clojure
λ deep-merge(x, y). 
  ∀map? → merge-with(deep-merge, x, y) | last(y)

λ now(). inst-ms(java.util.Date.) | js/Date.
λ uuid(). tempid/uuid() → unique symbol

λ has-class?(elem, class-name). 
  split className, check contains

λ find-by-id(tree, id). 
  recursive search for element with :id matching
```

---

## Usage Patterns

### Pattern 1: Define a Component with Query & Ident

```clojure
(ns app.ui
  (:require [com.fulcrologic.fulcro.components :as comp]
            [com.fulcrologic.fulcro.dom :as dom]))

(comp/defsc Person [this {id :person/id name :person/name}]
  {:query [:person/id :person/name]
   :ident [:person/id :person/id]}
  (dom/div "Name: " name))

(comp/defsc Root [this {:keys [person]}]
  {:query [{:root/person (comp/get-query Person)}]
   :initial-state {:root/person (comp/get-initial-state Person {})}}
  (dom/div (comp/factory Person person)))
```

### Pattern 2: Create App, Mount, Transact

```clojure
(ns app.core
  (:require [com.fulcrologic.fulcro.application :as app]
            [com.fulcrologic.fulcro.mutations :as m]
            [app.ui :as ui]))

;; Create app with HTTP remote
(def fulcro-app
  (app/fulcro-app
    {:root-component ui/Root
     :remotes {:default (net/make-request-impl "http://api")}}))

;; Mount to DOM
(app/mount! fulcro-app (js/document.getElementById "app"))

;; Transact mutation
(comp/transact! fulcro-app
  [{(m/mutate {:action (fn [env] ...)})}])
```

### Pattern 3: Load Data via Network

```clojure
(ns app.mutations
  (:require [com.fulcrologic.fulcro.data-fetch :as df]))

;; Async load with result handler
(df/load! app
  :root/person
  Person
  {:target [:ui/current-person]
   :on-load (fn [app result] ...)
   :on-error (fn [app error] ...)})
```

### Pattern 4: Dynamic Routing

```clojure
(comp/defsc PageA [this props]
  {:query [:page/id]
   :ident [:page/id :page/id]
   :route-segment ["a"]}
  (dom/div "Page A"))

;; Install in router
(dr/install-route-impl! app :page-a PageA)

;; Navigate
(dr/route-to! app [:page-a])
```

---

## Dependency Graph

**Core dependencies:**
- `edn-query-language/eql` — EQL parser/builder
- `com.cognitect/transit-clj(s)` — serialization format
- `com.fulcrologic/guardrails` — spec assertions (clj only)
- Clojure 1.12+, ClojureScript 1.12+ (provided)

**Upstream VSMs:**
- ✅ EQL — query language for normalization
- ✅ Transit — serialization (network layer)


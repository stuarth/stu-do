(ns todo.core.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [ring.util.response :as resp]
            [hiccup.page :refer [html5 include-css]]))

(def todos [{:title "first todo" :complete? false :id 1}
            {:title "second" :complete? false :id 2}])

(defn todos-index
  [{:keys [flash] :as req}]
  (html5
   [:head
    [:title "Hello world"]
    (include-css "//maxcdn.bootstrapcdn.com/bootstrap/3.3.1/css/bootstrap.min.css"
                 "/css/site.css")]
   [:body
    [:div.container
     (when flash
       [:div#flash.alert.alert-info
        flash])
     [:h1 "All this work!"]
     [:ul.list-unstyled
      (for [{:keys [id title complete?] :as todo} todos]
        [:li (when complete?
               {:class "completed"})
         title
         (when-not complete?
           [:form {:action (str "/todos/" id) :method "POST"}
            (anti-forgery-field)
            [:input {:type "hidden" :name "_method" :value "PUT"}]
            [:button.btn.btn-xs.btn-default {:type "submit"} "finish"]])])]
     [:form.form-inline {:action "/todos/create" :method "POST"}
      (anti-forgery-field)
      [:input.form-control
       {:type "text" :placeholder "your new task" :name "new-todo"}]
      [:button.btn.btn-primary {:type "submit"} "Add todo"]]]]))

(defn complete-todo
  [id]
  (assoc (resp/redirect-after-post "/")
    :flash (str "updated " id)))

(defn create-todo
  [new-todo]
  (assoc (resp/redirect-after-post "/")
    :flash (format "Added %s" new-todo)))

(defroutes app-routes
  (route/resources "/")
  (GET "/" req (todos-index req))
  (POST "/todos/create" {{:keys [new-todo]} :params} (create-todo new-todo))
  (PUT "/todos/:id" [id] (complete-todo id))
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes site-defaults))

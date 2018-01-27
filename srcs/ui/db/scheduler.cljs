(ns ui.db.scheduler)

(def shift-types [{:type "active"
                   :pred #(= "active" (:state %))
                   :label "Current"}
                  {:type "coverage_completed"
                   :pred #(= "coverage_completed" (:state %))
                   :label "Active"}
                  {:type "require_approval"
                   :pred #(= "require_approval" (:state %))
                   :label [:span "Active" [:br] "(require approval)"]}
                  {:type "without_applies"
                   :pred #(= "without_applies" (:state %))
                   :label [:span "Active" [:br] "(need coverage)"]}
                  {:type "completed"
                   :pred #(#{"completed" "failed"} (:state %))
                   :label "Past"}])


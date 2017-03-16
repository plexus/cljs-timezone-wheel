(ns timezone-wheel.test-runner
  (:require
   [doo.runner :refer-macros [doo-tests]]
   [timezone-wheel.core-test]
   [timezone-wheel.common-test]))

(enable-console-print!)

(doo-tests 'timezone-wheel.core-test
           'timezone-wheel.common-test)

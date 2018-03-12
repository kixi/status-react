(ns status-im.ui.screens.wallet.views
  (:require-macros [status-im.utils.views :as views])
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.screens.wallet.styles :as styles]
            [status-im.ui.screens.wallet.utils :as wallet.utils]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.ethereum.tokens :as tokens]))

(defn toolbar-view []
  [toolbar/toolbar {:style styles/toolbar :flat? true}
   nil
   [toolbar/content-wrapper]
   [toolbar/actions
    [{:icon      :icons/options
      :icon-opts {:color :white}
      :options   [{:label  (i18n/label :t/wallet-manage-assets)
                   :action #(re-frame/dispatch [:navigate-to-modal :wallet-settings-assets])}]}]]])

(defn- total-section [usd-value]
  [react/view styles/section
   [react/view {:style styles/total-balance-container}
    [react/view {:style styles/total-balance}
     [react/text {:style styles/total-balance-value} usd-value]
     [react/text {:style styles/total-balance-currency} (i18n/label :t/usd-currency)]]
    [react/text {:style styles/total-value} (i18n/label :t/wallet-total-value)]]])

(def actions
  [{:label  (i18n/label :t/send-transaction)
    :icon   :icons/arrow-right
    :action #(re-frame/dispatch [:navigate-to :wallet-send-transaction])}
   {:label  (i18n/label :t/receive-transaction)
    :icon   :icons/arrow-left
    :action #(re-frame/dispatch [:navigate-to :wallet-request-transaction])}
   {:label  (i18n/label :t/transaction-history)
    :icon   :icons/transaction-history
    :action #(re-frame/dispatch [:navigate-to :transactions-history])}])

(defn- render-asset [{:keys [symbol icon decimals amount]}]
  [react/view
   [list/item
    [list/item-image icon]
    [react/view {:style styles/asset-item-value-container}
     [react/text {:style           styles/asset-item-value
                  :number-of-lines 1
                  :ellipsize-mode  :tail}
      (wallet.utils/format-amount amount decimals)]
     [react/text {:style           styles/asset-item-currency
                  :uppercase?      true
                  :number-of-lines 1}
      (clojure.core/name symbol)]]]])

(defn current-tokens [visible-tokens network]
  (filter #(contains? visible-tokens (:symbol %)) (tokens/tokens-for (ethereum/network->chain-keyword network))))

(defn- asset-section [network balance visible-tokens]
  (let [tokens (current-tokens visible-tokens network)
        assets (map #(assoc % :amount (get balance (:symbol %))) (concat [tokens/ethereum] tokens))]
    [react/view styles/asset-section
     [react/text {:style styles/asset-section-title} (i18n/label :t/wallet-assets)]
     [list/flat-list
      {:default-separator? true
       :scroll-enabled     false
       :data               assets
       :render-fn          render-asset}]]))

(defn- refresh-control [tokens]
  (reagent/as-element
    [react/refresh-control {:on-refresh #(re-frame/dispatch [:update-wallet (map :symbol tokens)])
                            :tint-color :white
                            :refreshing false}]))

(views/defview wallet []
  (views/letsubs [network          [:network]
                  balance          [:balance]
                  visible-tokens   [:wallet.settings/visible-tokens]
                  portfolio-value  [:portfolio-value]]
    [react/view styles/main-section
     [toolbar-view]
     [react/scroll-view {:content-container-style styles/scrollable-section
                         :refresh-control         (refresh-control visible-tokens)}
      [total-section portfolio-value]
      [list/action-list actions
       {:container-style styles/action-section}]
      [asset-section network balance visible-tokens]]]))

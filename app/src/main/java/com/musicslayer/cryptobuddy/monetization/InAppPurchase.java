package com.musicslayer.cryptobuddy.monetization;

import android.app.Activity;

import androidx.annotation.NonNull;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.musicslayer.cryptobuddy.app.App;
import com.musicslayer.cryptobuddy.data.persistent.app.PersistentAppDataStore;
import com.musicslayer.cryptobuddy.data.persistent.app.Purchases;
import com.musicslayer.cryptobuddy.util.ToastUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

// Note: This class does not support buying more than one item in a single purchase, even if it's multiple copies of the same thing.

public class InAppPurchase {
    public static BillingClient billingClient;

    public static InAppPurchaseListener inAppPurchaseListener; // Used to handle activity UI updates.

    public static void setInAppPurchaseListener(InAppPurchase.InAppPurchaseListener inAppPurchaseListener) {
        InAppPurchase.inAppPurchaseListener = inAppPurchaseListener;
    }

    public static void initialize() {
        // The application context should be passed in here to avoid a memory leak.
        if(billingClient == null || !billingClient.isReady()) {
            PurchasesUpdatedListener purchasesUpdatedListener = new PurchasesUpdatedListener() {
                @Override
                public void onPurchasesUpdated(@NonNull BillingResult billingResult, List<Purchase> purchases) {
                    if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        for(Purchase purchase : purchases) {
                            if(purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                                for(String id : purchase.getSkus()) {
                                    grantPurchase(id);
                                }

                                InAppPurchase.acknowledgeOrConsume(purchase);
                            }
                        }
                    }
                }
            };

            // Use application context here to avoid a memory leak.
            billingClient = BillingClient.newBuilder(App.applicationContext)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build();

            billingClient.startConnection(new BillingClientStateListener() {
                @Override
                public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                    if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        InAppPurchase.updateAllPurchases();
                    }
                }

                @Override
                public void onBillingServiceDisconnected() {
                }
            });
        }
    }

    public static void purchaseRemoveAds(Activity activity) {
        List<String> skuList = new ArrayList<>();
        skuList.add("remove_ads");
        InAppPurchase.purchase(activity, skuList);
    }

    public static void purchaseUnlockPremiumFeatures(Activity activity) {
        List<String> skuList = new ArrayList<>();
        skuList.add("premium");
        InAppPurchase.purchase(activity, skuList);
    }

    public static void purchaseSupportDeveloper1(Activity activity) {
        List<String> skuList = new ArrayList<>();
        skuList.add("support_developer_1");
        InAppPurchase.purchase(activity, skuList);
    }

    public static void purchaseSupportDeveloper2(Activity activity) {
        List<String> skuList = new ArrayList<>();
        skuList.add("support_developer_2");
        InAppPurchase.purchase(activity, skuList);
    }

    public static void purchaseSupportDeveloper3(Activity activity) {
        List<String> skuList = new ArrayList<>();
        skuList.add("support_developer_3");
        InAppPurchase.purchase(activity, skuList);
    }

    private static void purchase(Activity activity, List<String> skuList) {
        if(!billingClient.isReady()) {
            ToastUtil.showToast("billing_connection_not_finished");
            return;
        }

        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);

        billingClient.querySkuDetailsAsync(params.build(), new SkuDetailsResponseListener() {
            @Override
            public void onSkuDetailsResponse(@NonNull BillingResult billingResult, List<SkuDetails> skuDetailsList) {
                if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && skuDetailsList != null && skuDetailsList.size() > 0) {
                    // We do not support a user purchasing more than one thing at once, so we know that there is only one sku.
                    BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder().setSkuDetails(skuDetailsList.get(0)).build();
                    billingClient.launchBillingFlow(activity, billingFlowParams);
                }
                else {
                    ToastUtil.showToast("billing_problem");
                }
            }
        });
    }

    public static void acknowledgeOrConsume(Purchase purchase) {
        // One-time items are just acknowledged.
        // Consumable items will be both consumed and acknowledged so they can be purchased again.
        if(!purchase.isAcknowledged() && purchase.getSkus().size() > 0) {
            // There is only one sku, so just check the first element to figure out which category it falls in.
            String id = purchase.getSkus().get(0);
            if("remove_ads".equals(id) || "premium".equals(id)) {
                acknowledge(purchase);
            }
            else if("support_developer_1".equals(id) || "support_developer_2".equals(id) || "support_developer_3".equals(id)) {
                consume(purchase);
            }
        }
    }

    public static void acknowledge(Purchase purchase) {
        AcknowledgePurchaseParams acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.getPurchaseToken())
                .build();

        final String toastKey = purchase.getSkus().get(0);

        billingClient.acknowledgePurchase(acknowledgePurchaseParams, new AcknowledgePurchaseResponseListener() {
            @Override
            public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {
                if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    ToastUtil.showToast(toastKey + "_purchase");
                }
                else {
                    ToastUtil.showToast("acknowledge_problem");
                }
            }
        });
    }

    public static void consume(Purchase purchase) {
        ConsumeParams consumeParams =
            ConsumeParams.newBuilder()
            .setPurchaseToken(purchase.getPurchaseToken())
            .build();

        // This will also do an implicit acknowledgement.
        billingClient.consumeAsync(consumeParams, new ConsumeResponseListener() {
            @Override
            public void onConsumeResponse(@NonNull BillingResult billingResult, @NonNull String purchaseToken) {
                if(billingResult.getResponseCode() ==  BillingClient.BillingResponseCode.OK) {
                    ToastUtil.showToast("support_developers_purchase");
                }
            }
        });
    }

    // Internal use only!
    public static void refund() {
        if(!billingClient.isReady()) {
            ToastUtil.showToast("billing_connection_not_finished");
            return;
        }

        ToastUtil.showToast("refund_purchases");

        billingClient.queryPurchasesAsync(BillingClient.SkuType.INAPP, new PurchasesResponseListener() {
            @Override
            public void onQueryPurchasesResponse(@NonNull BillingResult billingResult, @NonNull List<Purchase> purchases) {
                if (billingResult.getResponseCode() ==  BillingClient.BillingResponseCode.OK) {
                    for(Purchase purchase : purchases) {
                        for(String id : purchase.getSkus()) {
                            revokePurchase(id);
                        }

                        ConsumeParams.Builder params = ConsumeParams.newBuilder();
                        params.setPurchaseToken(purchase.getPurchaseToken());
                        billingClient.consumeAsync(params.build(), new ConsumeResponseListener() {
                            @Override
                            public void onConsumeResponse(@NonNull BillingResult billingResult, @NonNull String purchaseToken) {
                                ToastUtil.showToast("refund_purchases_complete");
                            }
                        });
                    }
                }
            }
        });
    }

    // Internal use only!
    public static void lock() {
        // Just lock one-time items.
        revokePurchase("remove_ads");
        revokePurchase("premium");
    }

    // Internal use only!
    public static void unlock() {
        // Just unlock one-time items.
        grantPurchase("remove_ads");
        grantPurchase("premium");
    }

    private static void grantPurchase(String id) {
        PersistentAppDataStore.getInstance(Purchases.class).updatePurchase(id, true);

        if(InAppPurchase.inAppPurchaseListener != null) {
            InAppPurchase.inAppPurchaseListener.onInAppPurchase();
        }
    }

    private static void revokePurchase(String id) {
        PersistentAppDataStore.getInstance(Purchases.class).updatePurchase(id, false);

        if(InAppPurchase.inAppPurchaseListener != null) {
            InAppPurchase.inAppPurchaseListener.onInAppPurchase();
        }
    }

    public static void updateAllPurchases() {
        if(!billingClient.isReady()) {
            ToastUtil.showToast("billing_connection_not_finished");
            return;
        }

        billingClient.queryPurchasesAsync(BillingClient.SkuType.INAPP, new PurchasesResponseListener() {
            @Override
            public void onQueryPurchasesResponse(@NonNull BillingResult billingResult, @NonNull List<Purchase> purchases) {
                if (billingResult.getResponseCode() ==  BillingClient.BillingResponseCode.OK) {
                    ArrayList<Purchase> toAcknowledgeList = new ArrayList<>();

                    // The default null value means revoke.
                    // Place "NO-OP" to do nothing, or "GRANT" to grant.
                    HashMap<String, String> productMap = new HashMap<>();

                    // Any Purchase not present means it was never purchased, or is refunded and not currently active.
                    for(Purchase purchase : purchases) {
                        if(purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                            // Grant all purchases, and add to list of things we need to acknowledge if we haven't already.
                            for(String id : purchase.getSkus()) {
                                productMap.put(id, "GRANT");
                            }
                            toAcknowledgeList.add(purchase);
                        }
                        else {
                            // Do nothing for any transient or indeterminate purchase state.
                            for(String id : purchase.getSkus()) {
                                productMap.put(id, "NO-OP");
                            }
                        }
                    }

                    for(String id : productMap.keySet()) {
                        if(productMap.get(id) == null) {
                            revokePurchase(id);
                        }
                        else if("GRANT".equals(productMap.get(id))) {
                            grantPurchase(id);
                        }
                    }

                    for(Purchase purchase : toAcknowledgeList) {
                        InAppPurchase.acknowledgeOrConsume(purchase);
                    }
                }
            }
        });
    }

    abstract public static class InAppPurchaseListener {
        abstract public void onInAppPurchase();
    }
}

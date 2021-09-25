package com.musicslayer.cryptobuddy.monetization;

import android.app.Activity;
import android.content.Context;

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
import com.musicslayer.cryptobuddy.persistence.Purchases;
import com.musicslayer.cryptobuddy.util.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

// For now, it is not possible to buy more than one sku in a single purchase, but later on it will be.

public class InAppPurchase {
    public static BillingClient billingClient;
    private static InAppPurchase.InAppPurchaseListener inAppPurchaseListener;

    public static void setInAppPurchaseListener(InAppPurchase.InAppPurchaseListener inAppPurchaseListener) {
        InAppPurchase.inAppPurchaseListener = inAppPurchaseListener;
    }

    public static void initialize(Context context) {
        if(billingClient == null || !billingClient.isReady()) {
            PurchasesUpdatedListener purchasesUpdatedListener = new PurchasesUpdatedListener() {
                @Override
                public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> purchases) {
                    if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        for(Purchase purchase : purchases) {
                            if(purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                                for(String id : purchase.getSkus()) {
                                    grantPurchase(context, id);
                                }

                                InAppPurchase.acknowledgeOrConsume(purchase);
                            }
                        }
                    }
                }
            };

            billingClient = BillingClient.newBuilder(context)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build();

            billingClient.startConnection(new BillingClientStateListener() {
                @Override
                public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                    if(billingResult.getResponseCode() ==  BillingClient.BillingResponseCode.OK) {
                        InAppPurchase.updateAllPurchases(context);
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

    public static void purchaseUnlockTokens(Activity activity) {
        List<String> skuList = new ArrayList<>();
        skuList.add("unlock_tokens");
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

    public static void purchase(Activity activity, List<String> skuList) {
        if(!billingClient.isReady()) {
            Toast.showToast("billing_connection_retry");
            InAppPurchase.initialize(activity);
            return;
        }

        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);

        billingClient.querySkuDetailsAsync(params.build(), new SkuDetailsResponseListener() {
            @Override
            public void onSkuDetailsResponse(@NonNull BillingResult billingResult, List<SkuDetails> skuDetailsList) {
                if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && skuDetailsList != null && skuDetailsList.size() > 0) {
                    // In the future, we could offer a bundle of skus to the user to purchase all at once, but for now we know that there is only one sku.
                    BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder().setSkuDetails(skuDetailsList.get(0)).build();
                    billingClient.launchBillingFlow(activity, billingFlowParams);
                }
                else {
                    Toast.showToast("billing_problem");
                }
            }
        });
    }

    public static void acknowledgeOrConsume(Purchase purchase) {
        // One-time items are just acknowledged.
        // Consumable items will be both consumed and acknowledged so they can be purchased again.
        if(!purchase.isAcknowledged() && purchase.getSkus().size() > 0) {
            // We assume that either the skus are all consumable, or they are all one-time.
            // We just need to check the first one to figure out which category it falls in.
            String id = purchase.getSkus().get(0);
            if("remove_ads".equals(id) || "unlock_tokens".equals(id)) {
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
                    Toast.showToast(toastKey + "_purchase");
                }
                else {
                    Toast.showToast("acknowledge_problem");
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
                    Toast.showToast("support_developers_purchase");
                }
            }
        });
    }

    // Internal use only!
    public static void refund(Context context) {
        if(!billingClient.isReady()) {
            Toast.showToast("billing_connection_retry");
            InAppPurchase.initialize(context);
            return;
        }

        Toast.showToast("refund_purchases");

        billingClient.queryPurchasesAsync(BillingClient.SkuType.INAPP, new PurchasesResponseListener() {
            @Override
            public void onQueryPurchasesResponse(@NonNull BillingResult billingResult, @NonNull List<Purchase> purchases) {
                if (billingResult.getResponseCode() ==  BillingClient.BillingResponseCode.OK) {
                    for(Purchase purchase : purchases) {
                        for(String id : purchase.getSkus()) {
                            revokePurchase(context, id);
                        }

                        ConsumeParams.Builder params = ConsumeParams.newBuilder();
                        params.setPurchaseToken(purchase.getPurchaseToken());
                        billingClient.consumeAsync(params.build(), new ConsumeResponseListener() {
                            @Override
                            public void onConsumeResponse(@NonNull BillingResult billingResult, @NonNull String purchaseToken) {
                                Toast.showToast("refund_purchase_complete");
                            }
                        });
                    }
                }
            }
        });
    }

    private static void grantPurchase(Context context, String id) {
        Purchases.updatePurchase(context, id, true);

        if(InAppPurchase.inAppPurchaseListener != null) {
            InAppPurchase.inAppPurchaseListener.onInAppPurchase();
        }
    }

    private static void revokePurchase(Context context, String id) {
        Purchases.updatePurchase(context, id, false);

        if(InAppPurchase.inAppPurchaseListener != null) {
            InAppPurchase.inAppPurchaseListener.onInAppPurchase();
        }
    }

    public static HashMap<String, Integer> getProductMap() {
        HashMap<String, Integer> productMap = new HashMap<>();

        // -1 = revoke, 0 = do nothing, 1 = grant.
        productMap.put("remove_ads", -1);
        productMap.put("unlock_tokens", -1);
        productMap.put("support_developer_1", -1);
        productMap.put("support_developer_2", -1);
        productMap.put("support_developer_3", -1);

        return productMap;
    }

    private static void updateAllPurchases(Context context) {
        if(!billingClient.isReady()) {
            InAppPurchase.initialize(context);
            return;
        }

        billingClient.queryPurchasesAsync(BillingClient.SkuType.INAPP, new PurchasesResponseListener() {
            @Override
            public void onQueryPurchasesResponse(@NonNull BillingResult billingResult, @NonNull List<Purchase> purchases) {
                if (billingResult.getResponseCode() ==  BillingClient.BillingResponseCode.OK) {
                    HashMap<String, Integer> productMap = getProductMap();
                    ArrayList<Purchase> toAcknowledgeList = new ArrayList<>();

                    // Any Purchase not present means it was never purchased, or is refunded and not currently active.
                    for(Purchase purchase : purchases) {
                        if(purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                            // Grant all purchases, and add to list of things we need to acknowledge if we haven't already.
                            for(String id : purchase.getSkus()) {
                                productMap.put(id, 1);
                            }
                            toAcknowledgeList.add(purchase);
                        }
                        else {
                            // Do nothing for any transient or indeterminate purchase state.
                            for(String id : purchase.getSkus()) {
                                productMap.put(id, 0);
                            }
                        }
                    }

                    for(String id : productMap.keySet()) {
                        if(productMap.get(id) == -1) {
                            revokePurchase(context, id);
                        }
                        else if(productMap.get(id) == 1) {
                            grantPurchase(context, id);
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

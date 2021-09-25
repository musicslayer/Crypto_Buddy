package com.musicslayer.cryptobuddy.persistence;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;

public class Purchases {
    public static boolean isRemoveAdsPurchased;
    public static boolean isUnlockTokensPurchased;
    public static int totalSupportAmount; // In Cents

    public static void updatePurchase(Context context, String sku, boolean isPurchased) {
        SharedPreferences settings = context.getSharedPreferences("purchases_data", MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        switch(sku) {
            case "remove_ads":
                isRemoveAdsPurchased = isPurchased;
                editor.putBoolean("purchases_remove_ads", isPurchased);
                break;
            case "unlock_tokens":
                boolean hasChanged = isUnlockTokensPurchased != isPurchased;

                isUnlockTokensPurchased = isPurchased;
                editor.putBoolean("purchases_unlock_tokens", isPurchased);

                // If we changed the purchase status, update TokenManagers and TokenList data right now.
                if(hasChanged) {
                    TokenManager.initialize(context);
                    if(!isPurchased) {
                        TokenList.resetAllData(context);
                    }
                }

                break;
            case "support_developer_1":
                if(isPurchased) {
                    totalSupportAmount += 99;
                    editor.putInt("purchases_total_support_amount", totalSupportAmount);
                }
                break;
            case "support_developer_2":
                if(isPurchased) {
                    totalSupportAmount += 499;
                    editor.putInt("purchases_total_support_amount", totalSupportAmount);
                }
                break;
            case "support_developer_3":
                if(isPurchased) {
                    totalSupportAmount += 2499;
                    editor.putInt("purchases_total_support_amount", totalSupportAmount);
                }
                break;
            default:
                return;
        }

        editor.apply();
    }

    public static void loadAllPurchases(Context context) {
        SharedPreferences settings = context.getSharedPreferences("purchases_data", MODE_PRIVATE);
        isRemoveAdsPurchased = settings.getBoolean("purchases_remove_ads", false);
        isUnlockTokensPurchased = settings.getBoolean("purchases_unlock_tokens", false);
        totalSupportAmount = settings.getInt("purchases_total_support_amount", 0);
    }
}

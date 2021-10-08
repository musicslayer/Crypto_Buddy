package com.musicslayer.cryptobuddy.persistence;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;

import java.util.HashMap;

public class Purchases {
    public final static boolean DEFAULT_isRemoveAdsPurchased = false;
    public static boolean isRemoveAdsPurchased;

    public final static boolean DEFAULT_isUnlockTokensPurchased = false;
    public static boolean isUnlockTokensPurchased;

    public final static int DEFAULT_totalSupportAmount = 0;
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

                // If we changed the purchase status, update TokenManagers right now.
                if(hasChanged) {
                    TokenManager.initialize(context);
                    if(!isPurchased) {
                        TokenManagerList.resetAllData(context);
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
        isRemoveAdsPurchased = settings.getBoolean("purchases_remove_ads", DEFAULT_isRemoveAdsPurchased);
        isUnlockTokensPurchased = settings.getBoolean("purchases_unlock_tokens", DEFAULT_isUnlockTokensPurchased);
        totalSupportAmount = settings.getInt("purchases_total_support_amount", DEFAULT_totalSupportAmount);
    }

    public static HashMap<String, String> getAllData() {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("isRemoveAdsPurchased", Boolean.toString(isRemoveAdsPurchased));
        hashMap.put("isUnlockTokensPurchased", Boolean.toString(isUnlockTokensPurchased));
        hashMap.put("totalSupportAmount", Integer.toString(totalSupportAmount));
        return hashMap;
    }

    public static void resetAllData(Context context) {
        isRemoveAdsPurchased = DEFAULT_isRemoveAdsPurchased;
        isUnlockTokensPurchased = DEFAULT_isUnlockTokensPurchased;
        totalSupportAmount = DEFAULT_totalSupportAmount;

        SharedPreferences settings = context.getSharedPreferences("purchases_data", MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.clear();
        editor.apply();
    }
}

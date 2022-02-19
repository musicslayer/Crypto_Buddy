package com.musicslayer.cryptobuddy.persistence;

import android.content.SharedPreferences;

import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;
import com.musicslayer.cryptobuddy.data.Exportation;
import com.musicslayer.cryptobuddy.util.SharedPreferencesUtil;

public class Purchases extends PersistentDataStore {
    public String getName() { return "Purchases"; }

    public boolean canExport() { return false; }
    public String doExport() { return Exportation.exportData(this, Purchases.class); }
    public void doImport(String s) { Exportation.importData(this, s, Purchases.class); }

    private final static boolean DEFAULT_isRemoveAdsPurchased = false;
    private static boolean isRemoveAdsPurchased;

    private final static boolean DEFAULT_isUnlockPremiumFeaturesPurchased = false;
    private static boolean isUnlockPremiumFeaturesPurchased;

    private final static int DEFAULT_totalSupportAmount = 0;
    private static int totalSupportAmount; // In Cents

    public String getSharedPreferencesKey() {
        return "purchases_data";
    }

    public static boolean isRemoveAdsPurchased() {
        return isRemoveAdsPurchased;
    }

    public static boolean isUnlockPremiumFeaturesPurchased() {
        return isUnlockPremiumFeaturesPurchased;
    }

    public static boolean isUnlockTokensPurchased() {
        // Currently, this is only sold as a premium feature.
        return isUnlockPremiumFeaturesPurchased;
    }

    public static boolean isUnlockExchangeIntegrationPurchased() {
        // Currently, this is only sold as a premium feature.
        return isUnlockPremiumFeaturesPurchased;
    }

    public static boolean isUnlockReflectionsCalculatorPurchased() {
        // Currently, this is only sold as a premium feature.
        return isUnlockPremiumFeaturesPurchased;
    }

    public static boolean isUnlockDataManagementPurchased() {
        // Currently, this is only sold as a premium feature.
        return isUnlockPremiumFeaturesPurchased;
    }

    public static int getTotalSupportAmount() {

        return totalSupportAmount;
    }

    public void updatePurchase(String sku, boolean isPurchased) {
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        switch(sku) {
            case "remove_ads":
                isRemoveAdsPurchased = isPurchased;
                editor.putBoolean("purchases_remove_ads", isPurchased);
                break;
            case "premium":
                boolean hasChanged = isUnlockPremiumFeaturesPurchased != isPurchased;

                isUnlockPremiumFeaturesPurchased = isPurchased;
                editor.putBoolean("purchases_unlock_premium_features", isPurchased);

                // If we changed the purchase status, update TokenManagers right now.
                if(hasChanged) {
                    TokenManager.initialize();
                    if(!isPurchased) {
                        TokenManager.resetAllTokens();
                        PersistentDataStore.getInstance(TokenManagerList.class).resetAllData();
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

    public void loadAllPurchases() {
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        isRemoveAdsPurchased = sharedPreferences.getBoolean("purchases_remove_ads", DEFAULT_isRemoveAdsPurchased);
        isUnlockPremiumFeaturesPurchased = sharedPreferences.getBoolean("purchases_unlock_premium_features", DEFAULT_isUnlockPremiumFeaturesPurchased);
        totalSupportAmount = sharedPreferences.getInt("purchases_total_support_amount", DEFAULT_totalSupportAmount);
    }

    public void resetAllData() {
        isRemoveAdsPurchased = DEFAULT_isRemoveAdsPurchased;
        isUnlockPremiumFeaturesPurchased = DEFAULT_isUnlockPremiumFeaturesPurchased;
        totalSupportAmount = DEFAULT_totalSupportAmount;

        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.clear();
        editor.apply();
    }
}

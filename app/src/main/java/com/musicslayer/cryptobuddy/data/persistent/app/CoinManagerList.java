package com.musicslayer.cryptobuddy.data.persistent.app;

import android.content.SharedPreferences;

import com.musicslayer.cryptobuddy.asset.coinmanager.CoinManager;
import com.musicslayer.cryptobuddy.data.bridge.DataBridge;
import com.musicslayer.cryptobuddy.data.bridge.Exportation;
import com.musicslayer.cryptobuddy.data.bridge.Serialization;
import com.musicslayer.cryptobuddy.util.SharedPreferencesUtil;

public class CoinManagerList extends PersistentAppDataStore implements Exportation.ExportableToJSON, Exportation.Versionable {
    public String getName() { return "CoinManagerList"; }

    public boolean canExport() { return true; }
    public String doExport() { return Exportation.exportData(this, CoinManagerList.class); }
    public void doImport(String s) { Exportation.importData(this, s, CoinManagerList.class); }

    // Just pick something that would never actually be saved.
    public final static String DEFAULT = "!UNKNOWN!";

    public String getSharedPreferencesKey() {
        return "coin_manager_data";
    }

    public void updateCoinManager(CoinManager coinManager) {
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("coin_manager_" + coinManager.getSettingsKey(), Serialization.serialize(coinManager, CoinManager.class));
        editor.apply();
    }

    public void saveAllData() {
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.clear();

        for(CoinManager coinManager : CoinManager.coinManagers) {
            editor.putString("coin_manager_" + coinManager.getSettingsKey(), Serialization.serialize(coinManager, CoinManager.class));
        }

        editor.apply();
    }

    public void loadAllData() {
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());

        // For each CoinManager, look for any stored data to fill in.
        for(CoinManager coinManager : CoinManager.coinManagers) {
            String serialString = sharedPreferences.getString("coin_manager_" + coinManager.getSettingsKey(), DEFAULT);

            CoinManager copyCoinManager = DEFAULT.equals(serialString) ? null : Serialization.deserialize(serialString, CoinManager.class);
            if(copyCoinManager != null) {
                coinManager.addHardcodedCoin(copyCoinManager.hardcoded_coins);
                coinManager.addFoundCoin(copyCoinManager.found_coins);
                coinManager.addCustomCoin(copyCoinManager.custom_coins);
            }

            coinManager.initializeHardcodedCoins();
        }
    }

    public void resetAllData() {
        // Only reset data stored in settings, not the TokenManager class.
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.clear();
        editor.apply();
    }

    public static String exportationVersion() {
        return "1";
    }

    public static String exportationType(String version) {
        return "!OBJECT!";
    }

    public String exportDataToJSON() throws org.json.JSONException {
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());

        DataBridge.JSONObjectDataBridge o = new DataBridge.JSONObjectDataBridge();

        for(CoinManager coinManager : CoinManager.coinManagers) {
            String key = "coin_manager_" + coinManager.getSettingsKey();
            String serialString = sharedPreferences.getString(key, DEFAULT);

            // We do not want to export hardcoded coins, so let's remove them.
            String newSerialString;
            try {
                CoinManager copyCoinManager = Serialization.deserialize(serialString, CoinManager.class);
                copyCoinManager.resetHardcodedCoins();
                newSerialString = Serialization.serialize(copyCoinManager, CoinManager.class);
            }
            catch(Exception e) {
                throw new IllegalStateException(e);
            }

            o.serialize(key, newSerialString, String.class);
        }

        return o.toStringOrNull();
    }


    public void importDataFromJSON(String s, String version) throws org.json.JSONException {
        DataBridge.JSONObjectDataBridge o = new DataBridge.JSONObjectDataBridge(s);

        // Only import coin managers that currently exist.
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        for(CoinManager coinManager : CoinManager.coinManagers) {
            String key = "coin_manager_" + coinManager.getSettingsKey();
            if(o.has(key)) {
                String value = o.deserialize(key, String.class);
                if(!DEFAULT.equals(value)) {
                    editor.putString(key, Serialization.cycle(value, CoinManager.class));
                }
            }
        }

        editor.apply();

        // Reinitialize data.
        initialize();
    }
}

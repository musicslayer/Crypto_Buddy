package com.musicslayer.cryptobuddy.data.persistent.app;

import android.content.SharedPreferences;

import com.musicslayer.cryptobuddy.asset.coinmanager.CoinManager;
import com.musicslayer.cryptobuddy.asset.coinmanager.UnknownCoinManager;
import com.musicslayer.cryptobuddy.data.bridge.DataBridge;
import com.musicslayer.cryptobuddy.data.persistent.user.PersistentUserDataStore;
import com.musicslayer.cryptobuddy.data.persistent.user.SettingList;
import com.musicslayer.cryptobuddy.settings.setting.Setting;
import com.musicslayer.cryptobuddy.util.SharedPreferencesUtil;

import java.io.IOException;

public class CoinManagerList extends PersistentAppDataStore implements DataBridge.ExportableToJSON {
    public String getName() { return "CoinManagerList"; }

    public boolean canExport() { return true; }
    public String doExport() { return DataBridge.exportData(this, CoinManagerList.class); }
    public void doImport(String s) { DataBridge.importData(this, s, CoinManagerList.class); }

    // Just pick something that would never actually be saved.
    public final static String DEFAULT = "!UNKNOWN!";

    public String getSharedPreferencesKey() {
        return "coin_manager_data";
    }

    public void updateSetting() {
        // When CoinManagers are altered, the default coin setting option may no longer exist.
        // Note that the setting may not have been initialized yet.
        if(Setting.settings != null) {
            Setting setting = Setting.getSettingFromKey("DefaultCoinSetting");
            setting.refreshSetting();
            PersistentUserDataStore.getInstance(SettingList.class).saveSetting(setting);
        }
    }

    public void updateCoinManager(CoinManager coinManager) {
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("coin_manager_" + coinManager.getSettingsKey(), DataBridge.serialize(coinManager, CoinManager.class));
        editor.apply();

        updateSetting();
    }

    public void saveAllData() {
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.clear();

        for(CoinManager coinManager : CoinManager.coinManagers) {
            editor.putString("coin_manager_" + coinManager.getSettingsKey(), DataBridge.serialize(coinManager, CoinManager.class));
        }

        editor.apply();

        updateSetting();
    }

    public void loadAllData() {
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());

        // For each CoinManager, look for any stored data to fill in.
        for(CoinManager coinManager : CoinManager.coinManagers) {
            coinManager.resetHardcodedCoins();
            coinManager.resetFoundCoins();
            coinManager.resetCustomCoins();

            String serialString = sharedPreferences.getString("coin_manager_" + coinManager.getSettingsKey(), DEFAULT);

            CoinManager copyCoinManager = DEFAULT.equals(serialString) ? null : DataBridge.deserialize(serialString, CoinManager.class);
            if(copyCoinManager != null) {
                coinManager.addHardcodedCoin(copyCoinManager.hardcoded_coins);
                coinManager.addFoundCoin(copyCoinManager.found_coins);
                coinManager.addCustomCoin(copyCoinManager.custom_coins);
            }

            coinManager.initializeHardcodedCoins();
        }

        updateSetting();
    }

    public void resetAllData() {
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.clear();
        editor.apply();

        loadAllData();
        updateSetting();
    }

    @Override
    public void exportDataToJSON(DataBridge.Writer o) throws IOException {
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());

        o.beginObject();
        o.serialize("!V!", "1", String.class);

        for(CoinManager coinManager : CoinManager.coinManagers) {
            String key = "coin_manager_" + coinManager.getSettingsKey();
            String serialString = sharedPreferences.getString(key, DEFAULT);

            // We do not want to export hardcoded coins, so let's remove them.
            String newSerialString;
            try {
                CoinManager copyCoinManager = DataBridge.deserialize(serialString, CoinManager.class);
                copyCoinManager.resetHardcodedCoins();
                newSerialString = DataBridge.serialize(copyCoinManager, CoinManager.class);
            }
            catch(Exception e) {
                throw new IllegalStateException(e);
            }

            o.serialize(key, newSerialString, String.class);
        }

        o.endObject();
    }

    @Override
    public void importDataFromJSON(DataBridge.Reader o) throws IOException {
        o.beginObject();

        String version = o.deserialize("!V!", String.class);
        if(!"1".equals(version)) {
            throw new IllegalStateException();
        }

        // Only import coin managers that currently exist.
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        while(o.jsonReader.hasNext()) {
            String key = o.getName();
            String value = o.getString();
            String settings_key = key.replace("coin_manager_", "");

            CoinManager coinManager = CoinManager.getCoinManagerFromSettingsKey(settings_key);
            if(!(coinManager instanceof UnknownCoinManager) && !DEFAULT.equals(value)) {
                editor.putString(key, DataBridge.cycleSerialization(value, CoinManager.class));
            }
        }

        editor.apply();

        o.endObject();

        // Reinitialize data.
        loadAllData();
        updateSetting();
    }
}

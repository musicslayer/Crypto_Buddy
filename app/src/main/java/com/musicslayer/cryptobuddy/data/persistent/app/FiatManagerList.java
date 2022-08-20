package com.musicslayer.cryptobuddy.data.persistent.app;

import android.content.SharedPreferences;

import com.musicslayer.cryptobuddy.asset.fiatmanager.FiatManager;
import com.musicslayer.cryptobuddy.asset.fiatmanager.UnknownFiatManager;
import com.musicslayer.cryptobuddy.data.bridge.DataBridge;
import com.musicslayer.cryptobuddy.data.persistent.user.PersistentUserDataStore;
import com.musicslayer.cryptobuddy.data.persistent.user.SettingList;
import com.musicslayer.cryptobuddy.settings.setting.Setting;
import com.musicslayer.cryptobuddy.util.SharedPreferencesUtil;

import java.io.IOException;

public class FiatManagerList extends PersistentAppDataStore implements DataBridge.ExportableToJSON {
    public String getName() { return "FiatManagerList"; }

    public boolean isVisible() { return true; }
    public String doExport() { return DataBridge.exportData(this, FiatManagerList.class); }
    public void doImport(String s) { DataBridge.importData(this, s, FiatManagerList.class); }

    // Just pick something that would never actually be saved.
    public final static String DEFAULT = "!UNKNOWN!";

    public String getSharedPreferencesKey() {
        return "fiat_manager_data";
    }

    public void updateSetting() {
        // When FiatManagers are altered, the default fiat setting option may no longer exist.
        // Note that the setting may not have been initialized yet.
        if(Setting.settings != null) {
            Setting setting = Setting.getSettingFromKey("DefaultFiatSetting");
            setting.refreshSetting();
            PersistentUserDataStore.getInstance(SettingList.class).saveSetting(setting);
        }
    }

    public void updateFiatManager(FiatManager fiatManager) {
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("fiat_manager_" + fiatManager.getSettingsKey(), DataBridge.serialize(fiatManager, FiatManager.class));
        editor.apply();

        updateSetting();
    }

    public void saveAllData() {
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.clear();

        for(FiatManager fiatManager : FiatManager.fiatManagers) {
            editor.putString("fiat_manager_" + fiatManager.getSettingsKey(), DataBridge.serialize(fiatManager, FiatManager.class));
        }

        editor.apply();

        updateSetting();
    }

    public void loadAllData() {
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());

        // For each FiatManager, look for any stored data to fill in.
        for(FiatManager fiatManager : FiatManager.fiatManagers) {
            fiatManager.resetHardcodedFiats();
            fiatManager.resetFoundFiats();
            fiatManager.resetCustomFiats();

            String serialString = sharedPreferences.getString("fiat_manager_" + fiatManager.getSettingsKey(), DEFAULT);

            FiatManager copyFiatManager = DEFAULT.equals(serialString) ? null : DataBridge.deserialize(serialString, FiatManager.class);
            if(copyFiatManager != null) {
                fiatManager.addHardcodedFiat(copyFiatManager.hardcoded_fiats);
                fiatManager.addFoundFiat(copyFiatManager.found_fiats);
                fiatManager.addCustomFiat(copyFiatManager.custom_fiats);
            }

            fiatManager.initializeHardcodedFiats();
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

        for(String key : SharedPreferencesUtil.getDataKeys(getSharedPreferencesKey())) {
            String serialString = sharedPreferences.getString(key, DEFAULT);

            // We do not want to export hardcoded fiats, so let's remove them ourselves.
            FiatManager copyFiatManager = DataBridge.deserialize(serialString, FiatManager.class);
            copyFiatManager.resetHardcodedFiats();
            String newSerialString = DataBridge.serialize(copyFiatManager, FiatManager.class);

            o.serialize(key, newSerialString, String.class);
        }

        o.endObject();
    }

    @Override
    public void importDataFromJSON(DataBridge.Reader o) throws IOException {
        o.beginObject();

        String version = o.deserialize("!V!", String.class);
        if(!"1".equals(version)) {
            throw new IllegalStateException("version = " + version);
        }

        // Only import fiat managers that currently exist.
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        while(o.jsonReader.hasNext()) {
            String key = o.getName();
            String value = o.getString();
            String settings_key = key.replace("fiat_manager_", "");

            FiatManager fiatManager = FiatManager.getFiatManagerFromSettingsKey(settings_key);
            if(!(fiatManager instanceof UnknownFiatManager) && !DEFAULT.equals(value)) {
                // Hardcoded fiats will not be imported, so let's add them ourselves.
                FiatManager copyFiatManager = DataBridge.deserialize(value, FiatManager.class);
                copyFiatManager.addHardcodedFiat(fiatManager.hardcoded_fiats);
                String newValue = DataBridge.serialize(copyFiatManager, FiatManager.class);

                editor.putString(key, newValue);
            }
        }

        editor.apply();

        o.endObject();

        // Reinitialize data.
        loadAllData();
        updateSetting();
    }
}

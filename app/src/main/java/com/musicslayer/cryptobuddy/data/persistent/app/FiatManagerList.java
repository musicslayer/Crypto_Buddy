package com.musicslayer.cryptobuddy.data.persistent.app;

import android.content.SharedPreferences;

import com.musicslayer.cryptobuddy.asset.fiatmanager.FiatManager;
import com.musicslayer.cryptobuddy.asset.fiatmanager.UnknownFiatManager;
import com.musicslayer.cryptobuddy.data.bridge.DataBridge;
import com.musicslayer.cryptobuddy.util.SharedPreferencesUtil;

import java.io.IOException;

public class FiatManagerList extends PersistentAppDataStore implements DataBridge.ExportableToJSON {
    public String getName() { return "FiatManagerList"; }

    public boolean canExport() { return true; }
    public String doExport() { return DataBridge.exportData(this, FiatManagerList.class); }
    public void doImport(String s) { DataBridge.importData(this, s, FiatManagerList.class); }

    // Just pick something that would never actually be saved.
    public final static String DEFAULT = "!UNKNOWN!";

    public String getSharedPreferencesKey() {
        return "fiat_manager_data";
    }

    public void updateFiatManager(FiatManager fiatManager) {
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("fiat_manager_" + fiatManager.getSettingsKey(), DataBridge.serialize(fiatManager, FiatManager.class));
        editor.apply();
    }

    public void saveAllData() {
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.clear();

        for(FiatManager fiatManager : FiatManager.fiatManagers) {
            editor.putString("fiat_manager_" + fiatManager.getSettingsKey(), DataBridge.serialize(fiatManager, FiatManager.class));
        }

        editor.apply();
    }

    public void loadAllData() {
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());

        // For each FiatManager, look for any stored data to fill in.
        for(FiatManager fiatManager : FiatManager.fiatManagers) {
            String serialString = sharedPreferences.getString("fiat_manager_" + fiatManager.getSettingsKey(), DEFAULT);

            FiatManager copyFiatManager = DEFAULT.equals(serialString) ? null : DataBridge.deserialize(serialString, FiatManager.class);
            if(copyFiatManager != null) {
                fiatManager.addHardcodedFiat(copyFiatManager.hardcoded_fiats);
                fiatManager.addFoundFiat(copyFiatManager.found_fiats);
                fiatManager.addCustomFiat(copyFiatManager.custom_fiats);
            }

            fiatManager.initializeHardcodedFiats();
        }
    }

    public void resetAllData() {
        // Only reset data stored in settings, not the TokenManager class.
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.clear();
        editor.apply();
    }

    @Override
    public void exportDataToJSON(DataBridge.Writer o) throws IOException {
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());

        o.beginObject();
        o.serialize("!V!", "1", String.class);

        for(FiatManager fiatManager : FiatManager.fiatManagers) {
            String key = "fiat_manager_" + fiatManager.getSettingsKey();
            String serialString = sharedPreferences.getString(key, DEFAULT);

            // We do not want to export hardcoded fiats, so let's remove them.
            String newSerialString;
            try {
                FiatManager copyFiatManager = DataBridge.deserialize(serialString, FiatManager.class);
                copyFiatManager.resetHardcodedFiats();
                newSerialString = DataBridge.serialize(copyFiatManager, FiatManager.class);
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

        // Only import fiat managers that currently exist.
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        while(o.jsonReader.hasNext()) {
            String key = o.getName();
            String value = o.getString();
            String settings_key = key.replace("fiat_manager_", "");

            FiatManager fiatManager = FiatManager.getFiatManagerFromSettingsKey(settings_key);
            if(!(fiatManager instanceof UnknownFiatManager) && !DEFAULT.equals(value)) {
                editor.putString(key, DataBridge.cycleSerialization(value, FiatManager.class));
            }
        }

        editor.apply();

        o.endObject();

        // Reinitialize data.
        loadAllData();
    }
}

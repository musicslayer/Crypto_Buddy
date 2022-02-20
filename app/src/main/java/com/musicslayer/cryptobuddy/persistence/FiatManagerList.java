package com.musicslayer.cryptobuddy.persistence;

import android.content.SharedPreferences;

import com.musicslayer.cryptobuddy.asset.fiatmanager.FiatManager;
import com.musicslayer.cryptobuddy.data.DataBridge;
import com.musicslayer.cryptobuddy.data.Exportation;
import com.musicslayer.cryptobuddy.json.JSONWithNull;
import com.musicslayer.cryptobuddy.data.Serialization;
import com.musicslayer.cryptobuddy.util.SharedPreferencesUtil;

public class FiatManagerList extends PersistentDataStore implements Exportation.ExportableToJSON, Exportation.Versionable {
    public String getName() { return "FiatManagerList"; }

    public boolean canExport() { return true; }
    public String doExport() { return Exportation.exportData(this, FiatManagerList.class); }
    public void doImport(String s) { Exportation.importData(this, s, FiatManagerList.class); }

    // Just pick something that would never actually be saved.
    public final static String DEFAULT = "!UNKNOWN!";

    public String getSharedPreferencesKey() {
        return "fiat_manager_data";
    }

    public void updateFiatManager(FiatManager fiatManager) {
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("fiat_manager_" + fiatManager.getSettingsKey(), Serialization.serialize(fiatManager, FiatManager.class));
        editor.apply();
    }

    public void saveAllData() {
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.clear();

        for(FiatManager fiatManager : FiatManager.fiatManagers) {
            editor.putString("fiat_manager_" + fiatManager.getSettingsKey(), Serialization.serialize(fiatManager, FiatManager.class));
        }

        editor.apply();
    }

    public void loadAllData() {
        // For now, do nothing.
        // TODO Fill in the data for FiatManagers, and then we can decouple the classes.
    }

    public FiatManager loadData(String settingsKey) {
        // FiatManager will create empty objects, but then this method will fill them in with data.
        // If a new FiatManager is introduced later, it will still be created but will get no data from here.
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        String serialString = sharedPreferences.getString("fiat_manager_" + settingsKey, DEFAULT);
        return DEFAULT.equals(serialString) ? null : Serialization.deserialize(serialString, FiatManager.class);
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

        for(FiatManager fiatManager : FiatManager.fiatManagers) {
            String key = "fiat_manager_" + fiatManager.getSettingsKey();
            String serialString = sharedPreferences.getString(key, DEFAULT);

            // We do not want to export hardcoded fiats, so let's remove them.
            String newSerialString;
            try {
                // TODO deserialize, remove tokens, and then serialize.
                JSONWithNull.JSONObjectWithNull oldJSON = new JSONWithNull.JSONObjectWithNull(serialString);
                oldJSON.putJSONArray("hardcoded_fiats", new JSONWithNull.JSONArrayWithNull());
                newSerialString = oldJSON.toStringOrNull();
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

        // Only import fiat managers that currently exist.
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        for(FiatManager fiatManager : FiatManager.fiatManagers) {
            String key = "fiat_manager_" + fiatManager.getSettingsKey();
            if(o.has(key)) {
                String value = o.deserialize(key, String.class);
                if(!DEFAULT.equals(value)) {
                    editor.putString(key, Serialization.cycle(value, FiatManager.class));
                }
            }
        }

        editor.apply();

        // Reinitialize data.
        initialize();
    }
}

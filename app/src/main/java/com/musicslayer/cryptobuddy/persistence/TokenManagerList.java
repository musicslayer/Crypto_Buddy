package com.musicslayer.cryptobuddy.persistence;

import android.content.SharedPreferences;

import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;
import com.musicslayer.cryptobuddy.data.DataBridge;
import com.musicslayer.cryptobuddy.data.Exportation;
import com.musicslayer.cryptobuddy.data.Serialization;
import com.musicslayer.cryptobuddy.util.SharedPreferencesUtil;

public class TokenManagerList extends PersistentDataStore implements Exportation.ExportableToJSON, Exportation.Versionable {
    public String getName() { return "TokenManagerList"; }

    public boolean canExport() { return true; }
    public String doExport() { return Exportation.exportData(this, TokenManagerList.class); }
    public void doImport(String s) { Exportation.importData(this, s, TokenManagerList.class); }

    // Just pick something that would never actually be saved.
    public final static String DEFAULT = "!UNKNOWN!";

    public String getSharedPreferencesKey() {
        return "token_manager_data";
    }

    public void updateTokenManager(TokenManager tokenManager) {
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("token_manager_" + tokenManager.getSettingsKey(), Serialization.serialize(tokenManager, TokenManager.class));
        editor.apply();
    }

    public void saveAllData() {
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.clear();

        for(TokenManager tokenManager : TokenManager.tokenManagers) {
            editor.putString("token_manager_" + tokenManager.getSettingsKey(), Serialization.serialize(tokenManager, TokenManager.class));
        }

        editor.apply();
    }

    public void loadAllData() {
        // For now, do nothing.
        // TODO Fill in the data for TokenManagers, and then we can decouple the classes.
    }

    public TokenManager loadData(String settingsKey) {
        // TokenManager will create empty objects, but then this method will fill them in with data.
        // If a new TokenManager is introduced later, it will still be created but will get no data from here.
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        String serialString = sharedPreferences.getString("token_manager_" + settingsKey, DEFAULT);
        return DEFAULT.equals(serialString) ? null : Serialization.deserialize(serialString, TokenManager.class);
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

        for(TokenManager tokenManager : TokenManager.tokenManagers) {
            String key = "token_manager_" + tokenManager.getSettingsKey();
            String serialString = sharedPreferences.getString(key, DEFAULT);

            // We do not want to export downloaded tokens, so let's remove them.
            String newSerialString;
            try {
                TokenManager dummyTokenManager = Serialization.deserialize(serialString, TokenManager.class);
                dummyTokenManager.resetDownloadedTokens();
                newSerialString = Serialization.serialize(dummyTokenManager, TokenManager.class);
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

        // Only import token managers that currently exist.
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        for(TokenManager tokenManager : TokenManager.tokenManagers) {
            String key = "token_manager_" + tokenManager.getSettingsKey();
            if(o.has(key)) {
                String value = o.deserialize(key, String.class);
                if(!DEFAULT.equals(value)) {
                    editor.putString(key, Serialization.cycle(value, TokenManager.class));
                }
            }
        }

        editor.apply();

        // Reinitialize data.
        initialize();
    }
}

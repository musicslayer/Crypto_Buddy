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
        // For each TokenManager, look for any stored data to fill in.
        for(TokenManager tokenManager : TokenManager.tokenManagers) {
            SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
            String serialString = sharedPreferences.getString("token_manager_" + tokenManager.getSettingsKey(), DEFAULT);

            TokenManager copyTokenManager = DEFAULT.equals(serialString) ? null : Serialization.deserialize(serialString, TokenManager.class);
            if(copyTokenManager != null) {
                tokenManager.addDownloadedToken(copyTokenManager.downloaded_tokens);
                tokenManager.addFoundToken(copyTokenManager.found_tokens);
                tokenManager.addCustomToken(copyTokenManager.custom_tokens);
            }
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

        for(TokenManager tokenManager : TokenManager.tokenManagers) {
            String key = "token_manager_" + tokenManager.getSettingsKey();
            String serialString = sharedPreferences.getString(key, DEFAULT);

            // We do not want to export downloaded tokens, so let's remove them.
            String newSerialString;
            try {
                TokenManager copyTokenManager = Serialization.deserialize(serialString, TokenManager.class);
                copyTokenManager.resetDownloadedTokens();
                newSerialString = Serialization.serialize(copyTokenManager, TokenManager.class);
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

package com.musicslayer.cryptobuddy.data.persistent.app;

import android.content.SharedPreferences;

import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;
import com.musicslayer.cryptobuddy.asset.tokenmanager.UnknownTokenManager;
import com.musicslayer.cryptobuddy.data.bridge.DataBridge;
import com.musicslayer.cryptobuddy.util.SharedPreferencesUtil;

import java.io.IOException;

public class TokenManagerList extends PersistentAppDataStore implements DataBridge.ExportableToJSON {
    public String getName() { return "TokenManagerList"; }

    public boolean isVisible() { return true; }
    public String doExport() { return DataBridge.exportData(this, TokenManagerList.class); }
    public void doImport(String s) { DataBridge.importData(this, s, TokenManagerList.class); }

    // Just pick something that would never actually be saved.
    public final static String DEFAULT = "!UNKNOWN!";

    public String getSharedPreferencesKey() {
        return "token_manager_data";
    }

    public void updateTokenManager(TokenManager tokenManager) {
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("token_manager_" + tokenManager.getSettingsKey(), DataBridge.serialize(tokenManager, TokenManager.class));
        editor.apply();
    }

    public void saveAllData() {
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.clear();

        for(TokenManager tokenManager : TokenManager.tokenManagers) {
            editor.putString("token_manager_" + tokenManager.getSettingsKey(), DataBridge.serialize(tokenManager, TokenManager.class));
        }

        editor.apply();
    }

    public void loadAllData() {
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());

        // For each TokenManager, look for any stored data to fill in.
        for(TokenManager tokenManager : TokenManager.tokenManagers) {
            tokenManager.resetDownloadedTokens();
            tokenManager.resetFoundTokens();
            tokenManager.resetCustomTokens();

            String serialString = sharedPreferences.getString("token_manager_" + tokenManager.getSettingsKey(), DEFAULT);

            TokenManager copyTokenManager = DEFAULT.equals(serialString) ? null : DataBridge.deserialize(serialString, TokenManager.class);
            if(copyTokenManager != null) {
                tokenManager.addDownloadedToken(copyTokenManager.downloaded_tokens);
                tokenManager.addFoundToken(copyTokenManager.found_tokens);
                tokenManager.addCustomToken(copyTokenManager.custom_tokens);
            }
        }
    }

    public void resetAllData() {
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.clear();
        editor.apply();

        loadAllData();
    }

    @Override
    public void exportDataToJSON(DataBridge.Writer o) throws IOException {
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());

        o.beginObject();
        o.serialize("!V!", "1", String.class);

        for(String key : SharedPreferencesUtil.getDataKeys(getSharedPreferencesKey())) {
            String serialString = sharedPreferences.getString(key, DEFAULT);

            // We do not want to export downloaded tokens, so let's remove them ourselves.
            TokenManager copyTokenManager = DataBridge.deserialize(serialString, TokenManager.class);
            copyTokenManager.resetDownloadedTokens();
            String newSerialString = DataBridge.serialize(copyTokenManager, TokenManager.class);

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

        // Only import token managers that currently exist.
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        while(o.jsonReader.hasNext()) {
            String key = o.getName();
            String value = o.getString();
            String settings_key = key.replace("token_manager_", "");

            TokenManager tokenManager = TokenManager.getTokenManagerFromSettingsKey(settings_key);
            if(!(tokenManager instanceof UnknownTokenManager) && !DEFAULT.equals(value)) {
                // Downloaded tokens will not be imported, so let's add them ourselves.
                TokenManager copyTokenManager = DataBridge.deserialize(value, TokenManager.class);
                copyTokenManager.addDownloadedToken(tokenManager.downloaded_tokens);
                String newValue = DataBridge.serialize(copyTokenManager, TokenManager.class);

                editor.putString(key, newValue);
            }
        }

        editor.apply();

        o.endObject();

        // Reinitialize data.
        loadAllData();
    }
}

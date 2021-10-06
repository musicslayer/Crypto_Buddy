package com.musicslayer.cryptobuddy.asset.tokenmanager;

import com.musicslayer.cryptobuddy.asset.crypto.token.Token;
import com.musicslayer.cryptobuddy.util.RESTUtil;

import org.json.JSONArray;
import org.json.JSONObject;

public class WavesTokenManager extends TokenManager {
    public String getKey() { return "WavesTokenManager"; }
    public String getName() { return "WavesTokenManager"; }
    public String getBlockchainID() { return "waves"; }
    public String getTokenType() { return "WAVES"; }
    public String getSettingsKey() { return "waves"; }

    public Token lookupToken(String baseURL, String id) {
        String tokenString = RESTUtil.get(baseURL + "/assets/details?id=" + id);

        try {
            JSONArray tokenInfoArray = new JSONArray(tokenString);
            JSONObject tokenInfoObject = tokenInfoArray.getJSONObject(0);

            String name = tokenInfoObject.getString("name");
            String display_name = name;
            int scale = tokenInfoObject.getInt("decimals");

            Token token = new Token(id, name, display_name, scale, id, getBlockchainID(), getTokenType());
            return token;
        }
        catch(Exception ignored) {
            return null;
        }
    }
}
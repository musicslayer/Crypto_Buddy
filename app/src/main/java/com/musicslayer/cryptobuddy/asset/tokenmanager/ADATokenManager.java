package com.musicslayer.cryptobuddy.asset.tokenmanager;

import com.musicslayer.cryptobuddy.asset.crypto.token.Token;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;
import com.musicslayer.cryptobuddy.util.RESTUtil;

import org.json.JSONArray;
import org.json.JSONObject;

// Token Registry:
// https://github.com/cardano-foundation/cardano-token-registry/

public class ADATokenManager extends TokenManager {
    public String getKey() { return "ADATokenManager"; }
    public String getName() { return "ADATokenManager"; }
    public String getBlockchainID() { return "cardano"; }
    public String getTokenType() { return "ADA"; }
    public String getSettingsKey() { return "ada"; }

    public boolean canGetJSON() { return true; }

    public String getJSON() {
        String baseURL = "https://raw.githubusercontent.com/cardano-foundation/cardano-token-registry/master/mappings/";

        // Get all file info.
        String fileArray = RESTUtil.get("https://api.github.com/repos/cardano-foundation/cardano-token-registry/contents/mappings");

        // For each file, get the token info.
        try {
            StringBuilder json = new StringBuilder("[");

            JSONArray fileArrayJSON = new JSONArray(fileArray);
            for(int i = 0; i < fileArrayJSON.length(); i++) {
                JSONObject file = fileArrayJSON.getJSONObject(i);
                String filename = file.getString("name");
                String tokenInfo = RESTUtil.get(baseURL + filename);

                if(tokenInfo == null) {
                    return null;
                }

                json.append(tokenInfo);

                if(i < fileArrayJSON.length() - 1) {
                    json.append(",");
                }
            }
            json.append("]");
            return json.toString();
        }
        catch(Exception ignored) {
            return null;
        }
    }

    public boolean parse(String tokenJSON) {
        try {
            JSONArray jsonArray = new JSONArray(tokenJSON);
            for(int i = 0; i < jsonArray.length(); i++) {
                JSONObject tokenInfo = jsonArray.getJSONObject(i);

                String display_name = tokenInfo.getJSONObject("name").getString("value");

                String name;
                if(tokenInfo.has("ticker")) {
                    name = tokenInfo.getJSONObject("ticker").getString("value");
                }
                else {
                    name = display_name;
                }

                int scale;
                if(tokenInfo.has("decimals")) {
                    scale = tokenInfo.getJSONObject("decimals").getInt("value");
                }
                else {
                    scale = 0;
                }

                String id = tokenInfo.getString("subject");
                String blockchain_id = "cardano";
                String token_type = "ADA";
                String key = id;

                Token token = new Token(key, name, display_name, scale, id, blockchain_id, token_type);
                addDownloadedToken(token);
            }

            return true;
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            return false;
        }
    }
}
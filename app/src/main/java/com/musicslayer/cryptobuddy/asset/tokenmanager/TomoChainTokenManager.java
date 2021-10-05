package com.musicslayer.cryptobuddy.asset.tokenmanager;

import com.musicslayer.cryptobuddy.asset.crypto.token.Token;
import com.musicslayer.cryptobuddy.util.ThrowableLogger;
import com.musicslayer.cryptobuddy.util.REST;

import org.json.JSONArray;
import org.json.JSONObject;

public class TomoChainTokenManager extends TokenManager {
    public String getKey() { return "TomoChainTokenManager"; }
    public String getName() { return "TomoChainTokenManager"; }
    public String getBlockchainID() { return "tomochain"; }
    public String getTokenType() { return "TOMO - TRC20"; }
    public String getSettingsKey() { return "tomo_trc20"; }

    public boolean canGetJSON() { return true; }

    public Token lookupToken(String baseURL, String id) {
        String tokenString = REST.get(baseURL + "/api/tokens/" + id);

        try {
            JSONObject tokenObj = new JSONObject(tokenString);

            String key = id;
            String name = tokenObj.getString("symbol");
            String display_name = tokenObj.getString("name");
            int scale = tokenObj.getInt("decimals");

            Token token = new Token(key, name, display_name, scale, id, getBlockchainID(), getTokenType());
            return token;
        }
        catch(Exception ignored) {
            return null;
        }
    }

    public String getJSON() {
        int numPages = 0;
        StringBuilder jsonPages = new StringBuilder();
        boolean done = false;

        while(!done) {
            numPages++;
            String pageData = REST.get("https://scan.tomochain.com/api/tokens?type=trc20&limit=50&page=" + numPages);
            if(pageData == null) { return null; }

            jsonPages.append("\"page").append(numPages).append("\":").append(pageData);

            try {
                JSONObject json = new JSONObject(pageData);
                done = json.getJSONArray("items").length() == 0;
            }
            catch(Exception ignored) {
                return null;
            }

            if(!done) {
                jsonPages.append(",");
            }
        }

        return "{\"numPages\":" + numPages + "," + jsonPages + "}";
    }

    public void parse(String tokenJSON) {
        if("{}".equals(tokenJSON)) { return; }

        try {
            JSONObject jsonOverall = new JSONObject(tokenJSON);
            int numPages = jsonOverall.getInt("numPages");
            for (int p = 0; p < numPages; p++) {
                int page = p + 1;
                JSONArray jsonArray = jsonOverall.getJSONObject("page" + page).getJSONArray("items");

                for(int i = 0; i < jsonArray.length(); i++) {
                    JSONObject json = jsonArray.getJSONObject(i);

                    String name = json.getString("symbol");
                    String display_name = json.getString("name");

                    // Tokens must have a nonempty name and display name.
                    if(name.isEmpty() || display_name.isEmpty()) {
                        continue;
                    }

                    int scale = json.getInt("decimals");

                    String id = json.getString("hash"); // Contract
                    //String key = json.getString("_id").toLowerCase();
                    String key = id;
                    String blockchain_id = "tomochain";
                    String token_type = "TOMO - TRC20";

                    Token token = new Token(key, name, display_name, scale, id, blockchain_id, token_type);
                    addDownloadedToken(token);
                }
            }
        }
        catch(Exception e) {
            ThrowableLogger.processThrowable(e);
        }
    }
}

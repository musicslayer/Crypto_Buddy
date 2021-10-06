package com.musicslayer.cryptobuddy.asset.tokenmanager;

import com.musicslayer.cryptobuddy.asset.crypto.token.Token;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;
import com.musicslayer.cryptobuddy.util.RESTUtil;

import org.json.JSONArray;
import org.json.JSONObject;

public class TomoChainZTokenManager extends TokenManager {
    public String getKey() { return "TomoChainZTokenManager"; }
    public String getName() { return "TomoChainZTokenManager"; }
    public String getBlockchainID() { return "tomochain"; }
    public String getTokenType() { return "TOMO - TRC21"; }
    public String getSettingsKey() { return "tomo_trc21"; }

    public boolean canGetJSON() { return true; }

    public Token lookupToken(String baseURL, String id) {
        String tokenString = RESTUtil.get(baseURL + "/api/tokens/" + id);

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
            String pageData = RESTUtil.get("https://scan.tomochain.com/api/tokens?type=trc21&limit=50&page=" + numPages);
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

    public boolean parse(String tokenJSON) {
        if("{}".equals(tokenJSON)) { return true; }

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

                    int scale = json.getInt("decimals");

                    String id = json.getString("hash"); // Contract
                    //String key = json.getString("_id").toLowerCase();
                    String key = id;
                    String blockchain_id = "tomochain";
                    String token_type = "TOMO - TRC21";

                    Token token = new Token(key, name, display_name, scale, id, blockchain_id, token_type);
                    addDownloadedToken(token);
                }
            }

            return true;
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            return false;
        }
    }
}

package com.musicslayer.cryptobuddy.asset.tokenmanager;

import com.musicslayer.cryptobuddy.api.address.CryptoAddress;
import com.musicslayer.cryptobuddy.asset.crypto.token.Token;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;
import com.musicslayer.cryptobuddy.util.RESTUtil;

import org.json.JSONArray;
import org.json.JSONObject;

public class TomoChainTokenManager extends TokenManager {
    public String getKey() { return "TomoChainTokenManager"; }
    public String getName() { return "TomoChainTokenManager"; }
    public String getBlockchainID() { return "tomochain"; }
    public String getTokenType() { return "TOMO - TRC20"; }
    public String getSettingsKey() { return "tomo_trc20"; }

    public boolean canGetJSON() { return true; }

    public Token lookupToken(CryptoAddress cryptoAddress, String key, String name, String display_name, int scale, String id) {
        // Don't use most inputs - we should be able to download everything needed to create the Token.
        String baseURL;
        if(cryptoAddress.network.isMainnet()) {
            baseURL = "https://scan.tomochain.com";
        }
        else {
            baseURL = "https://scan.testnet.tomochain.com";
        }

        String tokenString = RESTUtil.get(baseURL + "/api/tokens/" + id);

        try {
            JSONObject tokenObj = new JSONObject(tokenString);

            String key2 = id;
            String name2 = tokenObj.getString("symbol");
            String display_name2 = tokenObj.getString("name");
            int scale2 = tokenObj.getInt("decimals");

            Token token = new Token(key2, name2, display_name2, scale2, id, getBlockchainID(), getTokenType());
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
            String pageData = RESTUtil.get("https://scan.tomochain.com/api/tokens?type=trc20&limit=50&page=" + numPages);
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

            return true;
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            return false;
        }
    }
}

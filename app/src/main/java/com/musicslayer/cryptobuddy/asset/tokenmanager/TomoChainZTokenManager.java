package com.musicslayer.cryptobuddy.asset.tokenmanager;

import com.musicslayer.cryptobuddy.api.address.CryptoAddress;
import com.musicslayer.cryptobuddy.asset.crypto.token.Token;
import com.musicslayer.cryptobuddy.dialog.ProgressDialogFragment;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;
import com.musicslayer.cryptobuddy.util.WebUtil;

import org.json.JSONArray;
import org.json.JSONObject;

public class TomoChainZTokenManager extends TokenManager {
    public String getKey() { return "TomoChainZTokenManager"; }
    public String getName() { return "TomoChainZTokenManager"; }
    public String getCoinGeckoBlockchainID() { return "tomochain"; }
    public String getTokenType() { return "TOMO - TRC21"; }
    public String getSettingsKey() { return "tomo_trc21"; }

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

        String tokenString = WebUtil.get(baseURL + "/api/tokens/" + id);

        try {
            JSONObject tokenObj = new JSONObject(tokenString);

            String key2 = id;
            String name2 = tokenObj.getString("symbol");
            String display_name2 = tokenObj.getString("name");
            int scale2 = tokenObj.getInt("decimals");

            Token token = Token.buildToken(key2, name2, display_name2, scale2, getTokenType(), id, getCoinGeckoBlockchainID());
            return token;
        }
        catch(Exception ignored) {
            return null;
        }
    }

    public String getJSON() {
        ProgressDialogFragment.updateProgressSubtitle("Downloading " + getTokenType() + " Tokens...");

        int progress_current = 0;
        int progress_total;

        // Use this to get the total number of tokens.
        String totalData = WebUtil.get("https://scan.tomochain.com/api/tokens?type=trc21&limit=1&page=0");
        try {
            JSONObject json = new JSONObject(totalData);
            progress_total = json.getInt("total");
        }
        catch(Exception ignored) {
            return null;
        }

        int numPages = 0;
        StringBuilder jsonPages = new StringBuilder();
        boolean done = false;

        while(!done) {
            numPages++;
            String pageData = WebUtil.get("https://scan.tomochain.com/api/tokens?type=trc21&limit=50&page=" + numPages);
            if(pageData == null) { return null; }

            jsonPages.append("\"page").append(numPages).append("\":").append(pageData);

            try {
                JSONObject json = new JSONObject(pageData);
                int numItems = json.getJSONArray("items").length();

                progress_current += numItems;
                done = progress_current == progress_total;
                ProgressDialogFragment.reportProgress(progress_current, progress_total, getTokenType() + " Tokens Processed");
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

                    int scale = json.getInt("decimals");

                    String id = json.getString("hash"); // Contract
                    //String key = json.getString("_id").toLowerCase();
                    String key = id;

                    Token token = Token.buildToken(key, name, display_name, scale, getTokenType(), id, getCoinGeckoBlockchainID());
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

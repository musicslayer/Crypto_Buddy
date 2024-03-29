package com.musicslayer.cryptobuddy.asset.tokenmanager;

import com.musicslayer.cryptobuddy.asset.crypto.token.Token;
import com.musicslayer.cryptobuddy.dialog.ProgressDialogFragment;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;
import com.musicslayer.cryptobuddy.util.WebUtil;

import org.json.JSONArray;
import org.json.JSONObject;

// VIP180

// Token Registry:
// https://github.com/vechain/token-registry/tree/master/tokens/main/

public class VETTokenManager extends TokenManager {
    public String getKey() { return "VETTokenManager"; }
    public String getName() { return "VETTokenManager"; }
    public String getCoinGeckoBlockchainID() { return "vechain"; }
    public String getTokenType() { return "VET"; }
    public String getSettingsKey() { return "vet"; }

    public boolean canGetJSON() { return true; }

    public String getJSON() {
        ProgressDialogFragment.updateProgressSubtitle("Downloading " + getTokenType() + " Tokens...");
        return WebUtil.get("https://vechain.github.io/token-registry/main.json");
    }

    public boolean parse(String tokenJSON) {
        try {
            JSONArray jsonArray = new JSONArray(tokenJSON);
            for(int i = 0; i < jsonArray.length(); i++) {
                JSONObject json = jsonArray.getJSONObject(i);
                String name = json.getString("symbol");

                // We exclude VeThor here because we treat it like a coin.
                if("VTHO".equals(name)) {
                    continue;
                }

                String display_name = json.getString("name");
                int scale = json.getInt("decimals");

                String id = json.getString("address").toLowerCase();
                String key = name;

                Token token = Token.buildToken(key, name, display_name, scale, getTokenType(), id, getCoinGeckoBlockchainID());
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
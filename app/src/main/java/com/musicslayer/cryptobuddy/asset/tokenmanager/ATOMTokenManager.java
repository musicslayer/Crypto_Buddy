package com.musicslayer.cryptobuddy.asset.tokenmanager;

import com.musicslayer.cryptobuddy.asset.crypto.token.Token;
import com.musicslayer.cryptobuddy.dialog.ProgressDialogFragment;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;
import com.musicslayer.cryptobuddy.util.WebUtil;

import org.json.JSONArray;
import org.json.JSONObject;

public class ATOMTokenManager extends TokenManager {
    public String getKey() { return "ATOMTokenManager"; }
    public String getName() { return "ATOMTokenManager"; }
    public String getCoinGeckoBlockchainID() { return "cosmos"; }
    public String getTokenType() { return "ATOM"; }
    public String getSettingsKey() { return "atom"; }

    public boolean canGetJSON() { return true; }

    public String getJSON() {
        ProgressDialogFragment.updateProgressSubtitle("Downloading " + getTokenType() + " Tokens...");
        return WebUtil.get("https://api-utility.cosmostation.io/v1/ibc/tokens/cosmoshub-4");
    }

    public boolean parse(String tokenJSON) {
        try {
            JSONObject json = new JSONObject(tokenJSON);
            JSONArray jsonArray = json.getJSONArray("ibc_tokens");
            for(int i = 0; i < jsonArray.length(); i++) {
                JSONObject tokenInfo = jsonArray.getJSONObject(i);

                // Only include "real" tokens where we have full information.
                if(!(tokenInfo.has("auth") && tokenInfo.getBoolean("auth"))) {
                    continue;
                }

                String name = tokenInfo.getString("display_denom").toUpperCase();
                String display_name = name;
                int scale = tokenInfo.getInt("decimal");
                String id = tokenInfo.getString("hash");
                String key = id;

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
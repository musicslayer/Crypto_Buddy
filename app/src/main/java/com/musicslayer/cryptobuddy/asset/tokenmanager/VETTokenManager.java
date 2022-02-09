package com.musicslayer.cryptobuddy.asset.tokenmanager;

import com.musicslayer.cryptobuddy.asset.crypto.token.Token;
import com.musicslayer.cryptobuddy.dialog.ProgressDialogFragment;
import com.musicslayer.cryptobuddy.util.HashMapUtil;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;
import com.musicslayer.cryptobuddy.util.WebUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

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

                HashMap<String, String> additionalInfo = new HashMap<>();
                HashMapUtil.putValueInMap(additionalInfo, "contract_address", id);
                HashMapUtil.putValueInMap(additionalInfo, "coin_gecko_id", id);
                HashMapUtil.putValueInMap(additionalInfo, "coin_gecko_blockchain_id", getCoinGeckoBlockchainID());

                Token token = new Token(key, name, display_name, scale, getTokenType(), additionalInfo);
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
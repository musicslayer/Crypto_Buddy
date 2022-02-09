package com.musicslayer.cryptobuddy.asset.tokenmanager;

import com.musicslayer.cryptobuddy.asset.crypto.token.Token;
import com.musicslayer.cryptobuddy.dialog.ProgressDialogFragment;
import com.musicslayer.cryptobuddy.util.HashMapUtil;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;
import com.musicslayer.cryptobuddy.util.WebUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

public class KavaTokenManager extends TokenManager {
    public String getKey() { return "KavaTokenManager"; }
    public String getName() { return "KavaTokenManager"; }
    public String getCoinGeckoBlockchainID() { return "kava"; }
    public String getTokenType() { return "KAVA"; }
    public String getSettingsKey() { return "kava"; }

    public boolean canGetJSON() { return true; }

    public String getJSON() {
        ProgressDialogFragment.updateProgressSubtitle("Downloading " + getTokenType() + " Tokens...");
        return WebUtil.get("https://api-utility.cosmostation.io/v1/params/kava-9");
    }

    public boolean parse(String tokenJSON) {
        try {
            JSONObject jsonOverall = new JSONObject(tokenJSON);
            JSONArray jsonArray = jsonOverall.getJSONObject("Params").getJSONObject("supply").getJSONArray("supply");

            for(int i = 0; i < jsonArray.length(); i++) {
                JSONObject json = jsonArray.getJSONObject(i);

                String name = json.getString("denom").toUpperCase();
                if("UKAVA".equals(name)) { continue; }
                if(name.startsWith("IBC/")) { continue; }

                // Manually enter scale based on token name.
                int scale;
                if("HARD".equals(name) || "USDX".equals(name)) {
                    scale = 6;
                }
                else {
                    scale = 8;
                }

                String id = "?";

                HashMap<String, String> additionalInfo = new HashMap<>();
                HashMapUtil.putValueInMap(additionalInfo, "contract_address", id);
                HashMapUtil.putValueInMap(additionalInfo, "coin_gecko_id", id);
                HashMapUtil.putValueInMap(additionalInfo, "coin_gecko_blockchain_id", getCoinGeckoBlockchainID());

                Token token = new Token(name, name, name, scale, getTokenType(), additionalInfo);
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

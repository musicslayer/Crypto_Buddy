package com.musicslayer.cryptobuddy.asset.tokenmanager;

import com.musicslayer.cryptobuddy.asset.crypto.token.Token;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;
import com.musicslayer.cryptobuddy.util.RESTUtil;

import org.json.JSONArray;
import org.json.JSONObject;

public class KavaTokenManager extends TokenManager {
    public String getKey() { return "KavaTokenManager"; }
    public String getName() { return "KavaTokenManager"; }
    public String getBlockchainID() { return "kava"; }
    public String getTokenType() { return "KAVA"; }
    public String getSettingsKey() { return "kava"; }

    public boolean canGetJSON() { return true; }

    public String getJSON() {
        return RESTUtil.get("https://api.data.kava.io/supply/total");
    }

    public void parse(String tokenJSON) {
        if("{}".equals(tokenJSON)) { return; }

        try {
            JSONObject jsonOverall = new JSONObject(tokenJSON);
            JSONArray jsonArray = jsonOverall.getJSONArray("result");

            for(int i = 0; i < jsonArray.length(); i++) {
                JSONObject json = jsonArray.getJSONObject(i);

                String name = json.getString("denom").toUpperCase();
                if("UKAVA".equals(name)) { continue; }

                // Manually enter scale based on token name.
                int scale;
                if("HARD".equals(name) || "USDX".equals(name)) {
                    scale = 6;
                }
                else {
                    scale = 8;
                }

                String id = "?";
                String blockchain_id = "kava";
                String token_type = "KAVA";

                Token token = new Token(name, name, name, scale, id, blockchain_id, token_type);
                addDownloadedToken(token);
            }
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
        }
    }
}

package com.musicslayer.cryptobuddy.asset.tokenmanager;

import com.musicslayer.cryptobuddy.api.address.CryptoAddress;
import com.musicslayer.cryptobuddy.asset.crypto.token.Token;
import com.musicslayer.cryptobuddy.asset.network.WAVES_Stagenet;
import com.musicslayer.cryptobuddy.asset.network.WAVES_Testnet;
import com.musicslayer.cryptobuddy.util.WebUtil;

import org.json.JSONArray;
import org.json.JSONObject;

public class WavesTokenManager extends TokenManager {
    public String getKey() { return "WavesTokenManager"; }
    public String getName() { return "WavesTokenManager"; }
    public String getBlockchainID() { return "waves"; }
    public String getTokenType() { return "WAVES"; }
    public String getSettingsKey() { return "waves"; }

    public Token lookupToken(CryptoAddress cryptoAddress, String key, String name, String display_name, int scale, String id) {
        // Don't use most inputs - we should be able to download everything needed to create the Token.
        String baseURL;
        if(cryptoAddress.network.isMainnet()) {
            baseURL = "https://nodes.wavesnodes.com";
        }
        else if(cryptoAddress.network instanceof WAVES_Testnet) {
            baseURL = "https://nodes-testnet.wavesnodes.com";
        }
        else if(cryptoAddress.network instanceof WAVES_Stagenet) {
            baseURL = "https://nodes-stagenet.wavesnodes.com";
        }
        else {
            return null;
        }

        String tokenString = WebUtil.get(baseURL + "/assets/details?id=" + id);

        try {
            JSONArray tokenInfoArray = new JSONArray(tokenString);
            JSONObject tokenInfoObject = tokenInfoArray.getJSONObject(0);

            String name2 = tokenInfoObject.getString("name");
            String display_name2 = name2;
            int scale2 = tokenInfoObject.getInt("decimals");

            Token token = new Token(id, name2, display_name2, scale2, id, getBlockchainID(), getTokenType());
            return token;
        }
        catch(Exception ignored) {
            return null;
        }
    }
}
package com.musicslayer.cryptobuddy.asset.tokenmanager;

import com.musicslayer.cryptobuddy.asset.crypto.token.Token;
import com.musicslayer.cryptobuddy.util.Exception;
import com.musicslayer.cryptobuddy.util.REST;

import org.json.JSONArray;
import org.json.JSONObject;

// mainnet: https://dex.binance.org/api/v1/mini/tokens
// TODO testnet: https://testnet-dex.binance.org/api/v1/mini/tokens

// BEP8
public class BinanceChainMiniTokenManager extends TokenManager {
    public String getKey() { return "BinanceChainMiniTokenManager"; }
    public String getName() { return "BinanceChainMiniTokenManager"; }
    public String getBlockchainID() { return "binancecoin"; }
    public String getTokenType() { return "BNBc - BEP8"; }
    public String getSettingsKey() { return "bnbc_bep8"; }

    public boolean canGetJSON() { return true; }

    public String getJSON() {
        return REST.get("https://dex.binance.org/api/v1/mini/tokens?limit=1000");
    }

    public void parse(String tokenJSON) {
        if("{}".equals(tokenJSON)) { return; }

        try {
            JSONArray jsonArray = new JSONArray(tokenJSON);
            for(int i = 0; i < jsonArray.length(); i++) {
                JSONObject json = jsonArray.getJSONObject(i);

                String name = json.getString("symbol");
                String display_name = json.getString("name");
                int scale = 8; // All mini tokens have scale of 8.
                String id = "?"; // All mini tokens have no ID.
                String blockchain_id = "binance-smart-chain"; // We use smart chain to get these prices.
                String token_type = "BNBc - BEP8";

                Token token = new Token(name, name, display_name, scale, id, blockchain_id, token_type);
                addDownloadedToken(token);
            }
        }
        catch(java.lang.Exception e) {
            Exception.processException(e);
        }
    }
}
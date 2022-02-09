package com.musicslayer.cryptobuddy.asset.tokenmanager;

import com.musicslayer.cryptobuddy.asset.crypto.token.Token;
import com.musicslayer.cryptobuddy.dialog.ProgressDialogFragment;
import com.musicslayer.cryptobuddy.util.HashMapUtil;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;
import com.musicslayer.cryptobuddy.util.WebUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

// mainnet: https://dex.binance.org/api/v1/mini/tokens
// testnet: https://testnet-dex.binance.org/api/v1/mini/tokens (We don't support testnet tokens currently)

// BEP8
public class BinanceChainMiniTokenManager extends TokenManager {
    public String getKey() { return "BinanceChainMiniTokenManager"; }
    public String getName() { return "BinanceChainMiniTokenManager"; }
    public String getCoinGeckoBlockchainID() { return "binance-smart-chain"; } // We use smart chain to get these prices.
    public String getTokenType() { return "BNBc - BEP8"; }
    public String getSettingsKey() { return "bnbc_bep8"; }

    public boolean canGetJSON() { return true; }

    public String getJSON() {
        ProgressDialogFragment.updateProgressSubtitle("Downloading " + getTokenType() + " Tokens...");
        return WebUtil.get("https://dex.binance.org/api/v1/mini/tokens?limit=1000");
    }

    public boolean parse(String tokenJSON) {
        try {
            JSONArray jsonArray = new JSONArray(tokenJSON);
            for(int i = 0; i < jsonArray.length(); i++) {
                JSONObject json = jsonArray.getJSONObject(i);

                String name = json.getString("symbol");
                String display_name = json.getString("name");
                int scale = 8; // All mini tokens have scale of 8.
                String id = "?"; // All mini tokens have no ID.

                Token token = Token.buildToken(name, name, display_name, scale, getTokenType(), id, getCoinGeckoBlockchainID());
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
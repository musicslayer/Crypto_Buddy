package com.musicslayer.cryptobuddy.asset.tokenmanager;

import com.musicslayer.cryptobuddy.asset.crypto.token.Token;
import com.musicslayer.cryptobuddy.dialog.ProgressDialogFragment;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;
import com.musicslayer.cryptobuddy.util.WebUtil;

import org.json.JSONArray;
import org.json.JSONObject;

// mainnet: https://dex.binance.org/api/v1/tokens
// testnet: https://testnet-dex.binance.org/api/v1/tokens (We don't support testnet tokens currently)

public class BinanceChainTokenManager extends TokenManager {
    public String getKey() { return "BinanceChainTokenManager"; }
    public String getName() { return "BinanceChainTokenManager"; }
    public String getCoinGeckoBlockchainID() { return "binance-smart-chain"; } // We use smart chain to get these prices.
    public String getTokenType() { return "BNBc - BEP2"; }
    public String getSettingsKey() { return "bnbc_bep2"; }

    public boolean canGetJSON() { return true; }

    public String getJSON() {
        ProgressDialogFragment.updateProgressSubtitle("Downloading " + getTokenType() + " Tokens...");
        return WebUtil.get("https://dex.binance.org/api/v1/tokens?limit=1000");
    }

    public boolean parse(String tokenJSON) {
        try {
            JSONArray jsonArray = new JSONArray(tokenJSON);
            for(int i = 0; i < jsonArray.length(); i++) {
                JSONObject json = jsonArray.getJSONObject(i);

                String name = json.getString("symbol");
                String display_name = json.getString("name");

                // If the scale is not listed, just default to 8
                int scale;
                try {
                    scale = json.getInt("contract_decimals");
                }
                catch(org.json.JSONException ignored) {
                    scale = 8;
                }

                // Only tokens with a contract address can get prices (from smart chain version of token).
                String id;
                try {
                    id = json.getString("contract_address").toLowerCase();
                }
                catch(org.json.JSONException ignored) {
                    id = "?";
                }

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
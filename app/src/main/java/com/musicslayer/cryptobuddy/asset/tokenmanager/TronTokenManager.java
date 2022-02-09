package com.musicslayer.cryptobuddy.asset.tokenmanager;

import com.musicslayer.cryptobuddy.asset.crypto.token.Token;
import com.musicslayer.cryptobuddy.dialog.ProgressDialogFragment;
import com.musicslayer.cryptobuddy.util.HashMapUtil;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;
import com.musicslayer.cryptobuddy.util.WebUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigInteger;
import java.util.HashMap;

// The "key" of a TRC10 token is a unique number. The regular name may not be unique.
public class TronTokenManager extends TokenManager {
    public String getKey() { return "TronTokenManager"; }
    public String getName() { return "TronTokenManager"; }
    public String getCoinGeckoBlockchainID() { return "tron"; }
    public String getTokenType() { return "TRX - TRC10"; }
    public String getSettingsKey() { return "trx_trc10"; }

    public boolean canGetJSON() { return true; }

    public String getJSON() {
        ProgressDialogFragment.updateProgressSubtitle("Downloading " + getTokenType() + " Tokens...");
        return WebUtil.get("https://api.trongrid.io/wallet/getassetissuelist");
    }

    public boolean parse(String tokenJSON) {
        try {
            JSONObject jsonOverall = new JSONObject(tokenJSON);
            JSONArray jsonArray = jsonOverall.getJSONArray("assetIssue");

            for(int i = 0; i < jsonArray.length(); i++) {
                JSONObject json = jsonArray.getJSONObject(i);

                String name;
                if(json.has("abbr")) {
                    name = new String(new BigInteger(json.getString("abbr"),16).toByteArray());
                }
                else {
                    name = "";
                }

                //String display_name = json.getString("name");
                String display_name = new String(new BigInteger(json.getString("name"),16).toByteArray());

                int scale;
                if(json.has("precision")) {
                    scale = json.getInt("precision");
                }
                else {
                    scale = 0;
                }

                String id = json.getString("id"); // Instead of the contract address, use the token ID
                String key = id;

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

package com.musicslayer.cryptobuddy.asset.crypto.token;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.util.HashMapUtil;

import java.util.HashMap;

public class Token extends Crypto {
    public String original_name;
    public String original_display_name;
    public String original_combo_name;

    public String key;
    public String name;
    public String display_name;
    public String combo_name;
    public int scale;
    public String token_type;
    public HashMap<String, String> additionalInfo;

    public static Token buildToken(String key, String name, String display_name, int scale, String token_type, String id, String coin_gecko_blockchain_id) {
        HashMap<String, String> additionalInfo = new HashMap<>();
        HashMapUtil.putValueInMap(additionalInfo, "contract_address", id);
        HashMapUtil.putValueInMap(additionalInfo, "coin_gecko_id", id);
        HashMapUtil.putValueInMap(additionalInfo, "coin_gecko_blockchain_id", coin_gecko_blockchain_id);

        return new Token(key, name, display_name, scale, token_type, additionalInfo);
    }

    public Token(String key, String name, String display_name, int scale, String token_type, HashMap<String, String> additionalInfo) {
        this.original_name = name;
        this.original_display_name = display_name;
        this.original_combo_name = display_name + " (" + name + ")";

        this.key = key;
        this.scale = scale;
        this.token_type = token_type;
        this.additionalInfo = additionalInfo;

        this.name = modify(name);
        this.display_name = modify(display_name);
        this.combo_name = modify(original_combo_name);
    }

    public String getKey() { return key; }
    public String getName() { return name; }
    public String getDisplayName() { return display_name; }
    public String getComboName() { return combo_name; }
    public int getScale() { return scale; }
    public String getAssetKind() { return "!TOKEN!"; }
    public String getAssetType() { return token_type; }
    public HashMap<String, String> getAdditionalInfo() { return additionalInfo; }

    public String getContractAddress() {
        String s = HashMapUtil.getValueFromMap(getAdditionalInfo(), "contract_address");
        if(s == null) {
            s = "?";
        }
        return s;
    }

    public String getCoinGeckoID() {
        String s = HashMapUtil.getValueFromMap(getAdditionalInfo(), "coin_gecko_id");
        if(s == null) {
            s = "?";
        }
        return s;
    }

    public String getCoinGeckoBlockchainID() {
        // Asset platform for tokens (https://api.coingecko.com/api/v3/asset_platforms)
        String s = HashMapUtil.getValueFromMap(getAdditionalInfo(), "coin_gecko_blockchain_id");
        if(s == null) {
            s = "?";
        }
        return s;
    }

    public String modify(String s) {
        return s + " (" + token_type + ")";
    }

    public boolean isComplete() {
        // Tokens may be created from incomplete information, and while we may use the token,
        // we do not want to store it long term and have it prevent the complete version from being used later.

        // Note that all scales are "complete".
        return key != null && original_name != null && original_display_name != null && token_type != null;
    }
}

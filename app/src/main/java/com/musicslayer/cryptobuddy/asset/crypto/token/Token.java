package com.musicslayer.cryptobuddy.asset.crypto.token;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.util.HashMapUtil;

import java.util.HashMap;

public class Token extends Crypto {
    public String original_key;
    public String original_name;
    public String original_display_name;
    public int original_scale;
    public String original_token_type;
    public HashMap<String, String> original_additional_info;

    public String key;
    public String name;
    public String display_name;
    public String combo_name;
    public int scale;
    public String token_type;
    public HashMap<String, String> additional_info;

    public static Token buildToken(String key, String name, String display_name, int scale, String token_type, String id, String coin_gecko_blockchain_id) {
        HashMap<String, String> additional_info = new HashMap<>();
        HashMapUtil.putValueInMap(additional_info, "contract_address", id);
        HashMapUtil.putValueInMap(additional_info, "coin_gecko_id", id);
        HashMapUtil.putValueInMap(additional_info, "coin_gecko_blockchain_id", coin_gecko_blockchain_id);

        return new Token(key, name, display_name, scale, token_type, additional_info);
    }

    public Token(String key, String name, String display_name, int scale, String token_type, HashMap<String, String> additional_info) {
        this.original_key = key;
        this.original_name = name;
        this.original_display_name = display_name;
        this.original_scale = scale;
        this.original_token_type = token_type;
        this.original_additional_info = additional_info;

        // Modify everything to be non-null.
        if(key == null) {
            key = "?";
        }
        this.key = key;

        if(name == null) {
            name = "?";
        }
        this.name = name;

        if(display_name == null) {
            display_name = "?";
        }
        this.display_name = display_name;

        this.scale = scale;

        if(token_type == null) {
            token_type = "?";
        }
        this.token_type = token_type;

        if(additional_info == null) {
            additional_info = new HashMap<>();
        }
        this.additional_info = additional_info;

        // Further modify names for display purposes.
        modifyNames(this.name, this.display_name);
    }

    public String getOriginalKey() { return original_key; }
    public String getOriginalName() { return original_name; }
    public String getOriginalDisplayName() { return original_display_name; }
    public int getOriginalScale() { return original_scale; }
    public String getOriginalAssetType() { return original_token_type; }
    public HashMap<String, String> getOriginalAdditionalInfo() { return original_additional_info; }

    public String getKey() { return key; }
    public String getName() { return name; }
    public String getDisplayName() { return display_name; }
    public String getComboName() { return combo_name; }
    public int getScale() { return scale; }
    public String getAssetType() { return token_type; }
    public HashMap<String, String> getAdditionalInfo() { return additional_info; }

    public String getAssetKind() { return "!TOKEN!"; }

    public void modifyNames(String name, String displayName) {
        this.name = name + " (" + token_type + ")";
        this.display_name = displayName + " (" + token_type + ")";
        this.combo_name = displayName + " (" + name + ") (" + token_type + ")";
    }

    public boolean isComplete() {
        // Tokens may be created from incomplete information, and while we may use the token,
        // we do not want to store it long term and have it prevent the complete version from being used later.

        // Note that all scales are "complete".
        return original_key != null && original_name != null && original_display_name != null && original_token_type != null && original_additional_info != null;
    }

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
}

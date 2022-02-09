package com.musicslayer.cryptobuddy.asset.crypto.coin;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.util.HashMapUtil;

import java.util.HashMap;

public class Coin extends Crypto {
    public String original_key;
    public String original_name;
    public String original_display_name;
    public int original_scale;
    public String original_coin_type;
    public HashMap<String, String> original_additional_info;

    public String key;
    public String name;
    public String display_name;
    public String combo_name;
    public int scale;
    public String coin_type;
    public HashMap<String, String> additional_info;

    public static Coin buildCoin(String key, String name, String display_name, int scale, String coin_type, String id) {
        HashMap<String, String> additional_info = new HashMap<>();
        HashMapUtil.putValueInMap(additional_info, "coin_gecko_id", id);

        return new Coin(key, name, display_name, scale, coin_type, additional_info);
    }

    public Coin(String key, String name, String display_name, int scale, String coin_type, HashMap<String, String> additional_info) {
        this.original_key = key;
        this.original_name = name;
        this.original_display_name = display_name;
        this.original_scale = scale;
        this.original_coin_type = coin_type;
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

        if(coin_type == null) {
            coin_type = "?";
        }
        this.coin_type = coin_type;

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
    public String getOriginalAssetType() { return original_coin_type; }
    public HashMap<String, String> getOriginalAdditionalInfo() { return original_additional_info; }

    public String getKey() { return key; }
    public String getName() { return name; }
    public String getDisplayName() { return display_name; }
    public String getComboName() { return combo_name; }
    public int getScale() { return scale; }
    public String getAssetType() { return coin_type; }
    public HashMap<String, String> getAdditionalInfo() { return additional_info; }

    public String getAssetKind() { return "!COIN!"; }

    public void modifyNames(String name, String displayName) {
        // For now, don't add types.
        this.combo_name = displayName + " (" + name + ")";
    }

    public boolean isComplete() { // TODO original values or modified values?
        // Coins may be created from incomplete information, and while we may use the coin,
        // we do not want to store it long term and have it prevent the complete version from being used later.

        // Note that all scales are "complete".
        return key != null && original_name != null && original_display_name != null && coin_type != null;
    }

    public String getCoinGeckoID() {
        String s = HashMapUtil.getValueFromMap(getAdditionalInfo(), "coin_gecko_id");
        if(s == null) {
            s = "?";
        }
        return s;
    }
}

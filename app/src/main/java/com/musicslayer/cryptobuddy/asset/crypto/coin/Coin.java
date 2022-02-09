package com.musicslayer.cryptobuddy.asset.crypto.coin;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.util.HashMapUtil;

import java.util.HashMap;

public class Coin extends Crypto {
    public String original_name;
    public String original_display_name;
    public String original_combo_name;

    public String key;
    public String name;
    public String display_name;
    public String combo_name;
    public int scale;
    public String coin_type;
    public HashMap<String, String> additionalInfo;

    public Coin(String key, String name, String display_name, int scale, String coin_type, HashMap<String, String> additionalInfo) {
        this.original_name = name;
        this.original_display_name = display_name;
        this.original_combo_name = display_name + " (" + name + ")";

        this.key = key;
        this.scale = scale;
        this.coin_type = coin_type;
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
    public String getAssetType() { return coin_type; }
    public String getAssetKind() { return "!COIN!"; }
    public HashMap<String, String> getAdditionalInfo() { return additionalInfo; }

    public String getCoinGeckoID() {
        String s = HashMapUtil.getValueFromMap(getAdditionalInfo(), "coin_gecko_id");
        if(s == null) {
            s = "?";
        }
        return s;
    }

    public String modify(String s) {
        // For now, do nothing since all Coins have the same type.
        // In the future, we would add on the type like we do for tokens.
        return s;
    }

    public boolean isComplete() {
        // Coins may be created from incomplete information, and while we may use the coin,
        // we do not want to store it long term and have it prevent the complete version from being used later.

        // Note that all scales are "complete".
        return key != null && original_name != null && original_display_name != null && coin_type != null;
    }
}

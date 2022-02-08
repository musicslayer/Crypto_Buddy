package com.musicslayer.cryptobuddy.asset.crypto.coin;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;

public class Coin extends Crypto {
    public String original_name;
    public String original_display_name;

    public String key;
    public String name;
    public String display_name;
    public int scale;
    public String id;
    public String coin_type;

    public Coin(String key, String name, String display_name, int scale, String id, String coin_type) {
        this.original_name = name;
        this.original_display_name = display_name;

        this.key = key;
        this.scale = scale;
        this.id = id;
        this.coin_type = coin_type;

        this.name = modify(name);
        this.display_name = modify(display_name);
    }

    public String getKey() { return key; }
    public String getName() { return name; }
    public String getDisplayName() { return display_name; }
    public int getScale() { return scale; }
    public String getID() { return id; }
    public String getAssetType() { return coin_type; }
    public String getAssetKind() { return "!COIN!"; }

    public String modify(String s) {
        // For now, do nothing since all Coins have the same type.
        // In the future, we would add on the type like we do for tokens.
        return s;
    }

    public boolean isComplete() {
        // Coins may be created from incomplete information, and while we may use the coin,
        // we do not want to store it long term and have it prevent the complete version from being used later.

        // Note that all scales are "complete".
        return getKey() != null && getName() != null && getDisplayName() != null && getID() != null;
    }
}

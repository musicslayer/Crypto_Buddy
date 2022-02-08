package com.musicslayer.cryptobuddy.asset.fiat;

import com.musicslayer.cryptobuddy.asset.Asset;

public class Fiat extends Asset {
    public String original_name;
    public String original_display_name;

    public String key;
    public String name;
    public String display_name;
    public int scale;
    public String fiat_type;

    public Fiat(String key, String name, String display_name, int scale, String fiat_type) {
        this.original_name = name;
        this.original_display_name = display_name;

        this.key = key;
        this.scale = scale;
        this.fiat_type = fiat_type;

        this.name = modify(name);
        this.display_name = modify(display_name);
    }

    public String getKey() { return key; }
    public String getName() { return name; }
    public String getDisplayName() { return display_name; }
    public int getScale() { return scale; }
    public String getAssetType() { return fiat_type; }
    public String getAssetKind() { return "!FIAT!"; }

    public String getID() {
        // For now, just use lowercase symbol.
        return getName().toLowerCase();
    }

    public String modify(String s) {
        // For now, do nothing since all Fiats have the same type.
        // In the future, we would add on the type like we do for tokens.
        return s;
    }

    public boolean isComplete() {
        // Fiats may be created from incomplete information, and while we may use the fiat,
        // we do not want to store it long term and have it prevent the complete version from being used later.

        // Note that all scales are "complete".
        return getKey() != null && getName() != null && getDisplayName() != null;
    }
}

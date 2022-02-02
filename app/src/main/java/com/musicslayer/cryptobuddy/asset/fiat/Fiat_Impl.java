package com.musicslayer.cryptobuddy.asset.fiat;

public class Fiat_Impl extends Fiat {
    public String key;
    public String name;
    public String display_name;
    public int scale;

    public Fiat_Impl(String key, String name, String display_name, int scale) {
        this.key = key;
        this.name = name;
        this.display_name = display_name;
        this.scale = scale;
    }

    public String getKey() { return key; }
    public String getName() { return name; }
    public String getDisplayName() { return display_name; }
    public int getScale() { return scale; }
}

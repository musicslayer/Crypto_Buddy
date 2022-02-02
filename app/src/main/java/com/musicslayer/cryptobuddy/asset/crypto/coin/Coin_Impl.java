package com.musicslayer.cryptobuddy.asset.crypto.coin;

public class Coin_Impl extends Coin {
    public String key;
    public String name;
    public String display_name;
    public int scale;
    public String id;

    public Coin_Impl(String key, String name, String display_name, int scale, String id) {
        this.key = key;
        this.name = name;
        this.display_name = display_name;
        this.scale = scale;
        this.id = id;
    }

    public String getKey() { return key; }
    public String getName() { return name; }
    public String getDisplayName() { return display_name; }
    public int getScale() { return scale; }
    public String getID() { return id; }
}

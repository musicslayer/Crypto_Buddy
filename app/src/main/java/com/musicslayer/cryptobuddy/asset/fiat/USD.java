package com.musicslayer.cryptobuddy.asset.fiat;

public class USD extends Fiat {
    public String getKey() { return "USD"; }
    public String getName() { return "USD"; }
    public String getDisplayName() { return "United States Dollar"; }
    public int getScale() { return 2; }
}

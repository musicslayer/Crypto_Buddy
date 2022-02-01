package com.musicslayer.cryptobuddy.asset.fiat;

public class EUR extends Fiat {
    public String getKey() { return "EUR"; }
    public String getName() { return "EUR"; }
    public String getDisplayName() { return "Euro"; }
    public int getScale() { return 2; }
}

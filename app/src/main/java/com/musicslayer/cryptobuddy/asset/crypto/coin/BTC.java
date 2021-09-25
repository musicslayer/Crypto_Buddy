package com.musicslayer.cryptobuddy.asset.crypto.coin;

public class BTC extends Coin {
    public String getKey() { return "BTC"; }
    public String getName() { return "BTC"; }
    public String getDisplayName() { return "Bitcoin"; }
    public int getScale() { return 8; }

    public String getID() { return "bitcoin"; }
}

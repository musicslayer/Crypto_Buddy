package com.musicslayer.cryptobuddy.asset.crypto.coin;

public class BNBs extends Coin {
    public String getKey() { return "BNBs"; }
    public String getName() { return "BNBs"; }
    public String getDisplayName() { return "Binance Coin (Smart Chain)"; }
    public int getScale() { return 18; }

    public String getID() { return "binancecoin"; } // Price is same as BNBc using CoinGecko.
}

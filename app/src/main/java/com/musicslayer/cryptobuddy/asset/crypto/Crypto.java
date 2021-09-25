package com.musicslayer.cryptobuddy.asset.crypto;

import com.musicslayer.cryptobuddy.asset.Asset;

abstract public class Crypto extends Asset {
    abstract public String getID(); // Used by CoinGecko to query price
}

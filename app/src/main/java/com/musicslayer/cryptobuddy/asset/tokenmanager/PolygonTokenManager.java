package com.musicslayer.cryptobuddy.asset.tokenmanager;

public class PolygonTokenManager extends TokenManager {
    public String getKey() { return "PolygonTokenManager"; }
    public String getName() { return "PolygonTokenManager"; }
    public String getCoinGeckoBlockchainID() { return "polygon-pos"; }
    public String getTokenType() { return "MATIC - ERC20"; } // Polygon uses ERC20 tokens.
    public String getSettingsKey() { return "matic_erc20"; }
}
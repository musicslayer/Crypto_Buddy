package com.musicslayer.cryptobuddy.asset.tokenmanager;

// CLO20
public class CallistoTokenManager extends TokenManager {
    public String getKey() { return "CallistoTokenManager"; }
    public String getName() { return "CallistoTokenManager"; }
    public String getBlockchainID() { return "ethereum"; }
    public String getTokenType() { return "CLO - ERC20"; } // Callisto uses ERC20 tokens.
    public String getSettingsKey() { return "clo_erc20"; }
}
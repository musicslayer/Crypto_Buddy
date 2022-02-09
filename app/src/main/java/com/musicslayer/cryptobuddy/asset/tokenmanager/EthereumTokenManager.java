package com.musicslayer.cryptobuddy.asset.tokenmanager;

public class EthereumTokenManager extends TokenManager {
    public String getKey() { return "EthereumTokenManager"; }
    public String getName() { return "EthereumTokenManager"; }
    public String getCoinGeckoBlockchainID() { return "ethereum"; }
    public String getTokenType() { return "ETH - ERC20"; }
    public String getSettingsKey() { return "eth_erc20"; }
}
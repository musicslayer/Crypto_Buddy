package com.musicslayer.cryptobuddy.asset.tokenmanager;

public class TronSmartTokenManager extends TokenManager {
    public String getKey() { return "TronSmartTokenManager"; }
    public String getName() { return "TronSmartTokenManager"; }
    public String getBlockchainID() { return "tron"; }
    public String getTokenType() { return "TRX - TRC20"; }
    public String getSettingsKey() { return "trx_trc20"; }
}

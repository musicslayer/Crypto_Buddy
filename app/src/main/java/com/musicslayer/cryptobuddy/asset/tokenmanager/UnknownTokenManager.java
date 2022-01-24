package com.musicslayer.cryptobuddy.asset.tokenmanager;

import com.musicslayer.cryptobuddy.api.address.CryptoAddress;
import com.musicslayer.cryptobuddy.asset.crypto.token.Token;
import com.musicslayer.cryptobuddy.asset.crypto.token.UnknownToken;

import java.util.ArrayList;
import java.util.HashMap;

public class UnknownTokenManager extends TokenManager {
    String key;
    String tokenType;

    public String getKey() { return key; }
    public String getName() { return "UnknownTokenManager"; }
    public String getBlockchainID() { return "?"; }
    public String getTokenType() { return tokenType; }
    public String getSettingsKey() { return "?"; }

    public static UnknownTokenManager createUnknownTokenManager(String key, String tokenType) {
        return new UnknownTokenManager(key, tokenType);
    }

    private UnknownTokenManager(String key, String tokenType) {
        this.key = key;
        this.tokenType = tokenType;

        this.downloaded_tokens = new ArrayList<>();
        this.downloaded_token_map = new HashMap<>();
        this.downloaded_token_names = new ArrayList<>();
        this.downloaded_token_display_names = new ArrayList<>();

        this.found_tokens = new ArrayList<>();
        this.found_token_map = new HashMap<>();
        this.found_token_names = new ArrayList<>();
        this.found_token_display_names = new ArrayList<>();

        this.custom_tokens = new ArrayList<>();
        this.custom_token_map = new HashMap<>();
        this.custom_token_names = new ArrayList<>();
        this.custom_token_display_names = new ArrayList<>();
    }

    @Override
    // Always return unknown tokens, regardless of if the information is complete.
    public Token getToken(CryptoAddress cryptoAddress, String key, String name, String display_name, int scale, String id) {
        return UnknownToken.createUnknownToken(key, name, display_name, scale, id, getBlockchainID(), getTokenType());
    }
}

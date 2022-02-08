package com.musicslayer.cryptobuddy.asset.crypto.token;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;

public class Token extends Crypto {
    public String original_name;
    public String original_display_name;

    public String key;
    public String name;
    public String display_name;
    public int scale;
    public String id; // contract for tokens
    public String blockchain_id; // asset platform for tokens (https://api.coingecko.com/api/v3/asset_platforms)
    public String token_type;

    public Token(String key, String name, String display_name, int scale, String id, String blockchain_id, String token_type) {
        this.original_name = name;
        this.original_display_name = display_name;

        this.key = key;
        this.scale = scale;
        this.id = id;
        this.blockchain_id = blockchain_id;
        this.token_type = token_type;

        this.name = modify(name);
        this.display_name = modify(display_name);
    }

    public String getKey() { return key; }
    public String getName() { return name; }
    public String getDisplayName() { return display_name; }
    public int getScale() { return scale; }
    public String getID() { return id; }
    public String getBlockchainID() { return blockchain_id; }
    public String getAssetKind() { return "!TOKEN!"; }
    public String getAssetType() { return token_type; }

    public String modify(String s) {
        return s + " (" + token_type + ")";
    }

    public boolean isComplete() {
        // Tokens may be created from incomplete information, and while we may use the token,
        // we do not want to store it long term and have it prevent the complete version from being used later.

        // Note that all scales are "complete".
        return getKey() != null && original_name != null && original_display_name != null && getID() != null && getBlockchainID() != null && getAssetType() != null;
    }
}

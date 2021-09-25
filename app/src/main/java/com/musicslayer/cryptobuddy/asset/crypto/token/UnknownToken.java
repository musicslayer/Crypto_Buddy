package com.musicslayer.cryptobuddy.asset.crypto.token;

public class UnknownToken extends Token {
    public static UnknownToken createUnknownToken(String key, String name, String display_name, int scale, String id, String blockchain_id, String token_type) {
        // "id" and "blockchain_id" is ? because we cannot lookup the price.
        // Other fields are modified to attempt to show an unknown token to the user.
        String unknownKey;
        if(key == null) {
            unknownKey = "?";
        }
        else {
            unknownKey = key;
        }

        String unknownName;
        if(name == null) {
            unknownName = "?UNKNOWN_TOKEN?";
        }
        else {
            unknownName = "?UNKNOWN_TOKEN (" + name + ")?";
        }

        String unknownDisplayName;
        if(display_name == null) {
            unknownDisplayName = "?Unknown Token?";
        }
        else {
            unknownDisplayName = "?Unknown Token (" + display_name + ")?";
        }

        return new UnknownToken(unknownKey, unknownName, unknownDisplayName, scale, "?", "?", token_type);
    }

    private UnknownToken(String key, String name, String display_name, int scale, String id, String blockchain_id, String token_type) {
        super(key, name, display_name, scale, id, blockchain_id, token_type);
    }
}

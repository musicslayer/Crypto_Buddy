package com.musicslayer.cryptobuddy.asset.crypto.token;

public class UnknownToken extends Token {
    public static UnknownToken createUnknownToken(String key, String name, String display_name, int scale, String id, String blockchain_id, String token_type) {
        // "id" and "blockchain_id" are ? because we cannot lookup the price.
        // Other fields are modified to show an unknown token to the user.
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

        String unknownTokenType;
        if(token_type == null) {
            unknownTokenType = "?";
        }
        else {
            unknownTokenType = token_type;
        }

        return new UnknownToken(unknownKey, unknownName, unknownDisplayName, scale, "?", "?", unknownTokenType);
    }

    private UnknownToken(String key, String name, String display_name, int scale, String id, String blockchain_id, String token_type) {
        super(key, name, display_name, scale, id, blockchain_id, token_type);
    }

    public boolean isComplete() {
        // UnknownTokens are never complete, since by definition they represent a Token where we do not know all the information.
        return false;
    }
}

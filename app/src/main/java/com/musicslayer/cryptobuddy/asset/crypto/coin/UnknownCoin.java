package com.musicslayer.cryptobuddy.asset.crypto.coin;

public class UnknownCoin extends Coin {
    public static UnknownCoin createUnknownCoin(String key, String name, String display_name, int scale, String id, String coin_type) {
        // "id" is ? because we cannot lookup the price.
        // Other fields are modified to show an unknown coin to the user.
        String unknownKey;
        if(key == null) {
            unknownKey = "?";
        }
        else {
            unknownKey = key;
        }

        String unknownName;
        if(name == null) {
            unknownName = "?UNKNOWN_COIN?";
        }
        else {
            unknownName = "?UNKNOWN_COIN (" + name + ")?";
        }

        String unknownDisplayName;
        if(display_name == null) {
            unknownDisplayName = "?Unknown Coin?";
        }
        else {
            unknownDisplayName = "?Unknown Coin (" + display_name + ")?";
        }

        String unknownCoinType;
        if(coin_type == null) {
            unknownCoinType = "?";
        }
        else {
            unknownCoinType = coin_type;
        }

        return new UnknownCoin(unknownKey, unknownName, unknownDisplayName, scale, "?", unknownCoinType);
    }

    private UnknownCoin(String key, String name, String display_name, int scale, String id, String coin_type) {
        super(key, name, display_name, scale, id, coin_type);
    }

    public boolean isComplete() {
        // UnknownCoins are never complete, since by definition they represent a Coin where we do not know all the information.
        return false;
    }
}

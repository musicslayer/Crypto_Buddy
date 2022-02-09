package com.musicslayer.cryptobuddy.asset.crypto.coin;

import com.musicslayer.cryptobuddy.util.HashMapUtil;

import java.util.HashMap;

public class UnknownCoin extends Coin {
    public static UnknownCoin createUnknownCoin(String key, String name, String display_name, int scale, String coin_type, String id) {
        HashMap<String, String> additionalInfo = new HashMap<>();
        HashMapUtil.putValueInMap(additionalInfo, "coin_gecko_id", id);

        return createUnknownCoin(key, name, display_name, scale, coin_type, additionalInfo);
    }

    public static UnknownCoin createUnknownCoin(String key, String name, String display_name, int scale, String coin_type, HashMap<String, String> additionalInfo) {
        // Fields are modified to show an unknown coin to the user.
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

        return new UnknownCoin(unknownKey, unknownName, unknownDisplayName, scale, unknownCoinType, additionalInfo);
    }

    private UnknownCoin(String key, String name, String display_name, int scale, String coin_type, HashMap<String, String> additionalInfo) {
        super(key, name, display_name, scale, coin_type, additionalInfo);
    }

    public boolean isComplete() {
        // UnknownCoins are never complete, since by definition they represent a Coin where we do not know all the information.
        return false;
    }
}

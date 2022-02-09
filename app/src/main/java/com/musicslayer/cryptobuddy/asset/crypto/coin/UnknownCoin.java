package com.musicslayer.cryptobuddy.asset.crypto.coin;

import com.musicslayer.cryptobuddy.util.HashMapUtil;

import java.util.HashMap;

public class UnknownCoin extends Coin {
    public static UnknownCoin createUnknownCoin(String key, String name, String display_name, int scale, String coin_type, String id) {
        HashMap<String, String> additional_info = new HashMap<>();
        HashMapUtil.putValueInMap(additional_info, "coin_gecko_id", id);

        return createUnknownCoin(key, name, display_name, scale, coin_type, additional_info);
    }

    public static UnknownCoin createUnknownCoin(String key, String name, String display_name, int scale, String coin_type, HashMap<String, String> additional_info) {
        return new UnknownCoin(key, name, display_name, scale, coin_type, additional_info);
    }

    private UnknownCoin(String key, String name, String display_name, int scale, String coin_type, HashMap<String, String> additional_info) {
        super(key, name, display_name, scale, coin_type, additional_info);
    }

    @Override
    public void modifyNames(String name, String displayName) {
        // For now, don't add types.
        this.name = "?UNKNOWN_COIN? <" + name + ">";
        this.display_name = "?UNKNOWN_COIN? <" + displayName + ">";
        this.combo_name = "?UNKNOWN_COIN? <" + displayName + " (" + name + ")>";
    }

    public boolean isComplete() {
        // UnknownCoins are never complete, since by definition they represent a Coin where we do not know all the information.
        return false;
    }
}

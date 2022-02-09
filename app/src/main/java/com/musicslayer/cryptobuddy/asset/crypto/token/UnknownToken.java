package com.musicslayer.cryptobuddy.asset.crypto.token;

import com.musicslayer.cryptobuddy.util.HashMapUtil;

import java.util.HashMap;

public class UnknownToken extends Token {
    public static UnknownToken createUnknownToken(String key, String name, String display_name, int scale, String token_type, String id, String coin_gecko_blockchain_id) {
        HashMap<String, String> additional_info = new HashMap<>();
        HashMapUtil.putValueInMap(additional_info, "contract_address", id);
        HashMapUtil.putValueInMap(additional_info, "coin_gecko_id", id);
        HashMapUtil.putValueInMap(additional_info, "coin_gecko_blockchain_id", coin_gecko_blockchain_id);

        return createUnknownToken(key, name, display_name, scale, token_type, additional_info);
    }

    public static UnknownToken createUnknownToken(String key, String name, String display_name, int scale, String token_type, HashMap<String, String> additional_info) {
        return new UnknownToken(key, name, display_name, scale, token_type, additional_info);
    }

    private UnknownToken(String key, String name, String display_name, int scale, String token_type, HashMap<String, String> additional_info) {
        super(key, name, display_name, scale, token_type, additional_info);
    }

    @Override
    public void modifyNames(String name, String displayName) {
        this.name = "?UNKNOWN_TOKEN? <" + name + " (" + token_type + ")>";
        this.display_name = "?UNKNOWN_TOKEN? <" + displayName + " (" + token_type + ")>";
        this.combo_name = "?UNKNOWN_TOKEN? <" + displayName + " (" + name + ") (" + token_type + ")>";
    }

    @Override
    public boolean isComplete() {
        // UnknownTokens are never complete, since by definition they represent a Token where we do not know all the information.
        return false;
    }
}

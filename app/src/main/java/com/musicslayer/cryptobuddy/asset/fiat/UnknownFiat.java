package com.musicslayer.cryptobuddy.asset.fiat;

import java.util.HashMap;

public class UnknownFiat extends Fiat {
    public static UnknownFiat createUnknownFiat(String key, String name, String display_name, int scale, String fiat_type) {
        HashMap<String, String> additional_info = new HashMap<>();
        return createUnknownFiat(key, name, display_name, scale, fiat_type, additional_info);
    }

    public static UnknownFiat createUnknownFiat(String key, String name, String display_name, int scale, String fiat_type, HashMap<String, String> additional_info) {
        return new UnknownFiat(key, name, display_name, scale, fiat_type, additional_info);
    }

    private UnknownFiat(String key, String name, String display_name, int scale, String fiat_type, HashMap<String, String> additional_info) {
        super(key, name, display_name, scale, fiat_type, additional_info);
    }

    @Override
    public void modifyNames(String name, String displayName) {
        // For now, don't add types.
        this.name = "?UNKNOWN_FIAT? <" + name + ">";
        this.display_name = "?UNKNOWN_FIAT? <" + displayName + ">";
        this.combo_name = "?UNKNOWN_FIAT? <" + displayName + " (" + name + ")>";
    }

    public boolean isComplete() {
        // UnknownFiats are never complete, since by definition they represent a Fiat where we do not know all the information.
        return false;
    }
}

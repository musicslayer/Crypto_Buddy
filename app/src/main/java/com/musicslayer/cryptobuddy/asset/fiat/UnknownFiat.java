package com.musicslayer.cryptobuddy.asset.fiat;

import java.util.HashMap;

public class UnknownFiat extends Fiat {
    public static UnknownFiat createUnknownFiat(String key, String name, String display_name, int scale, String fiat_type) {
        // Fields are modified to show an unknown fiat to the user.
        String unknownKey;
        if(key == null) {
            unknownKey = "?";
        }
        else {
            unknownKey = key;
        }

        String unknownName;
        if(name == null) {
            unknownName = "?UNKNOWN_FIAT?";
        }
        else {
            unknownName = "?UNKNOWN_FIAT (" + name + ")?";
        }

        String unknownDisplayName;
        if(display_name == null) {
            unknownDisplayName = "?Unknown Fiat?";
        }
        else {
            unknownDisplayName = "?Unknown Fiat (" + display_name + ")?";
        }

        String unknownFiatType;
        if(fiat_type == null) {
            unknownFiatType = "?";
        }
        else {
            unknownFiatType = fiat_type;
        }

        return new UnknownFiat(unknownKey, unknownName, unknownDisplayName, scale, unknownFiatType);
    }

    private UnknownFiat(String key, String name, String display_name, int scale, String fiat_type) {
        super(key, name, display_name, scale, fiat_type, new HashMap<>());
    }

    public boolean isComplete() {
        // UnknownFiats are never complete, since by definition they represent a Fiat where we do not know all the information.
        return false;
    }
}

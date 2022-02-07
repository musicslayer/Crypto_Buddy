package com.musicslayer.cryptobuddy.asset.fiat;

import android.content.Context;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.asset.Asset;
import com.musicslayer.cryptobuddy.asset.fiatmanager.FiatManager;
import com.musicslayer.cryptobuddy.util.FileUtil;
import com.musicslayer.cryptobuddy.util.ReflectUtil;

import java.util.ArrayList;
import java.util.HashMap;

abstract public class Fiat extends Asset {
    public static ArrayList<Fiat> fiats;
    public static HashMap<String, Fiat> fiat_map;
    public static ArrayList<String> fiat_names;
    public static ArrayList<String> fiat_display_names;

    public static void initialize(Context context) {
        fiat_names = FileUtil.readFileIntoLines(context, R.raw.asset_fiat);

        fiats = new ArrayList<>();
        fiat_map = new HashMap<>();
        fiat_display_names = new ArrayList<>();

        for(String fiatName : fiat_names) {
            Fiat fiat = ReflectUtil.constructClassInstanceFromName("com.musicslayer.cryptobuddy.asset.fiat." + fiatName);
            fiats.add(fiat);
            fiat_map.put(fiatName, fiat);
            fiat_display_names.add(fiat.getDisplayName());
        }
    }

    public static Fiat getFiatFromKey(String key) {
        FiatManager fiatManager = FiatManager.getDefaultFiatManager();

        Fiat fiat = fiatManager.hardcoded_fiat_map.get(key);
        if(fiat == null) {
            fiat = fiatManager.found_fiat_map.get(key);
        }
        if(fiat == null) {
            fiat = fiatManager.custom_fiat_map.get(key);
        }
        if(fiat == null) {
            fiat = UnknownFiat.createUnknownFiat(key);
        }

        return fiat;
    }

    public String getAssetType() {
        return "!FIAT!";
    }

    public String getID() {
        // For now, just use lowercase symbol.
        return getName().toLowerCase();
    }

    public boolean isComplete() {
        // Fiats may be created from incomplete information, and while we may use the fiat,
        // we do not want to store it long term and have it prevent the complete version from being used later.

        // Note that all scales are "complete".
        return getKey() != null && getName() != null && getDisplayName() != null;
    }
}

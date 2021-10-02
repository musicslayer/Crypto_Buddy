package com.musicslayer.cryptobuddy.asset.fiat;

import android.content.Context;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.asset.Asset;
import com.musicslayer.cryptobuddy.persistence.Settings;
import com.musicslayer.cryptobuddy.util.File;
import com.musicslayer.cryptobuddy.util.Reflect;

import java.util.ArrayList;
import java.util.HashMap;

abstract public class Fiat extends Asset {
    public static ArrayList<Fiat> fiats;
    public static HashMap<String, Fiat> fiat_map;
    public static ArrayList<String> fiat_names;
    public static ArrayList<String> fiat_display_names;
    public static ArrayList<String> fiat_combo_names;

    public static void initialize(Context context) {
        fiat_names = File.readFileIntoLines(context, R.raw.asset_fiat);

        fiats = new ArrayList<>();
        fiat_map = new HashMap<>();
        fiat_display_names = new ArrayList<>();
        fiat_combo_names = new ArrayList<>();

        for(String fiatName : fiat_names) {
            Fiat fiat = Reflect.constructClassInstanceFromName("com.musicslayer.cryptobuddy.asset.fiat." + fiatName);
            fiats.add(fiat);
            fiat_map.put(fiatName, fiat);
            fiat_display_names.add(fiat.getDisplayName());
            fiat_combo_names.add(fiatName + " " + fiat.getDisplayName());
        }
    }

    public static ArrayList<String> getAllFiatSettingNames() {
        if("full".equals(Settings.setting_asset)) {
            return fiat_display_names;
        }
        else {
            return fiat_names;
        }
    }

    public static Fiat getFiatFromKey(String key) {
        Fiat fiat = fiat_map.get(key);
        if(fiat == null) {
            fiat = UnknownFiat.createUnknownFiat(key);
        }

        return fiat;
    }
}

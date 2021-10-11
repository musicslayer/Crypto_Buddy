package com.musicslayer.cryptobuddy.asset.crypto.coin;

import android.content.Context;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.settings.AssetDisplaySetting;
import com.musicslayer.cryptobuddy.util.FileUtil;
import com.musicslayer.cryptobuddy.util.ReflectUtil;

import java.util.ArrayList;
import java.util.HashMap;

abstract public class Coin extends Crypto {
    public static ArrayList<Coin> coins;
    public static HashMap<String, Coin> coin_map;
    public static ArrayList<String> coin_names;
    public static ArrayList<String> coin_display_names;
    public static ArrayList<String> coin_combo_names;

    public static void initialize(Context context) {
        coin_names = FileUtil.readFileIntoLines(context, R.raw.asset_coin);

        coins = new ArrayList<>();
        coin_map = new HashMap<>();
        coin_display_names = new ArrayList<>();
        coin_combo_names = new ArrayList<>();

        for(String coinName : coin_names) {
            Coin coin = ReflectUtil.constructClassInstanceFromName("com.musicslayer.cryptobuddy.asset.crypto.coin." + coinName);
            coins.add(coin);
            coin_map.put(coinName, coin);
            coin_display_names.add(coin.getDisplayName());
            coin_combo_names.add(coinName + " " + coin.getDisplayName());
        }
    }

    public static ArrayList<String> getAllCoinSettingNames() {
        if("full".equals(AssetDisplaySetting.value)) {
            return coin_display_names;
        }
        else {
            return coin_names;
        }
    }

    public static Coin getCoinFromKey(String key) {
        Coin coin = coin_map.get(key);
        if(coin == null) {
            coin = UnknownCoin.createUnknownCoin(key);
        }

        return coin;
    }
}

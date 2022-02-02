package com.musicslayer.cryptobuddy.asset.crypto.coin;

import android.content.Context;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.asset.coinmanager.CoinManager;
import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.util.FileUtil;
import com.musicslayer.cryptobuddy.util.ReflectUtil;

import java.util.ArrayList;
import java.util.HashMap;

abstract public class Coin extends Crypto {
    public static ArrayList<Coin> coins;
    public static HashMap<String, Coin> coin_map;
    public static ArrayList<String> coin_names;
    public static ArrayList<String> coin_display_names;

    public static void initialize(Context context) {
        coin_names = FileUtil.readFileIntoLines(context, R.raw.asset_coin);

        coins = new ArrayList<>();
        coin_map = new HashMap<>();
        coin_display_names = new ArrayList<>();

        for(String coinName : coin_names) {
            Coin coin = ReflectUtil.constructClassInstanceFromName("com.musicslayer.cryptobuddy.asset.crypto.coin." + coinName);
            coins.add(coin);
            coin_map.put(coinName, coin);
            coin_display_names.add(coin.getDisplayName());
        }
    }

    public static Coin getCoinFromKey(String key) {
        CoinManager coinManager = CoinManager.getCoinManagerFromKey("BaseCoinManager");

        Coin coin = coinManager.hardcoded_coin_map.get(key);
        if(coin == null) {
            coin = coinManager.found_coin_map.get(key);
        }
        if(coin == null) {
            coin = coinManager.custom_coin_map.get(key);
        }
        if(coin == null) {
            coin = UnknownCoin.createUnknownCoin(key);
        }

        return coin;
    }

    public String getAssetType() {
        return "!COIN!";
    }

    public boolean isComplete() {
        // Coins may be created from incomplete information, and while we may use the coin,
        // we do not want to store it long term and have it prevent the complete version from being used later.

        // Note that all scales are "complete".
        return getKey() != null && getName() != null && getDisplayName() != null && getID() != null;
    }
}

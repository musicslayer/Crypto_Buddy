package com.musicslayer.cryptobuddy.asset.coinmanager;

import android.content.Context;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.persistence.CoinManagerList;
import com.musicslayer.cryptobuddy.util.FileUtil;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;

import org.json.JSONArray;
import org.json.JSONObject;

public class BaseCoinManager extends CoinManager {
    public String getKey() { return "BaseCoinManager"; }
    public String getName() { return "BaseCoinManager"; }
    public String getCoinType() { return "BASE"; }
    public String getSettingsKey() { return "base"; }

    public void initializeHardcodedCoins(Context context) {
        resetHardcodedCoins();
        String coinJSON = FileUtil.readFile(context, R.raw.asset_coin_hardcoded);

        try {
            JSONObject jsonObject = new JSONObject(coinJSON);
            JSONArray jsonArray = jsonObject.getJSONArray("coins");
            for(int i = 0; i < jsonArray.length(); i++) {
                JSONObject json = jsonArray.getJSONObject(i);

                String key = json.getString("key");
                String name = json.getString("name");
                String display_name = json.getString("display_name");
                int scale = json.getInt("scale");
                String id = json.getString("id");

                Coin coin = new Coin(key, name, display_name, scale, id, getCoinType());
                addHardcodedCoin(coin);
            }
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            throw new IllegalStateException(e);
        }

        CoinManagerList.updateCoinManager(context, this);
    }
}
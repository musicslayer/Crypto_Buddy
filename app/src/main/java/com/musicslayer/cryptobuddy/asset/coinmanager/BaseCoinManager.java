package com.musicslayer.cryptobuddy.asset.coinmanager;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.data.persistent.app.CoinManagerList;
import com.musicslayer.cryptobuddy.data.persistent.app.PersistentAppDataStore;
import com.musicslayer.cryptobuddy.util.FileUtil;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;

import org.json.JSONArray;
import org.json.JSONObject;

public class BaseCoinManager extends CoinManager {
    public String getKey() { return "BaseCoinManager"; }
    public String getName() { return "BaseCoinManager"; }
    public String getCoinType() { return "BASE"; }
    public String getSettingsKey() { return "base"; }

    public void initializeHardcodedCoins() {
        resetHardcodedCoins();
        String coinJSON = FileUtil.readFile(R.raw.asset_coin_hardcoded);

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

                Coin coin = Coin.buildCoin(key, name, display_name, scale, getCoinType(), id);
                addHardcodedCoin(coin);
            }
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            throw new IllegalStateException(e);
        }

        PersistentAppDataStore.getInstance(CoinManagerList.class).updateCoinManager(this);
    }
}
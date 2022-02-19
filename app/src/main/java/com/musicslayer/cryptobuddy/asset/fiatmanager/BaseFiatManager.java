package com.musicslayer.cryptobuddy.asset.fiatmanager;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.asset.fiat.Fiat;
import com.musicslayer.cryptobuddy.persistence.FiatManagerList;
import com.musicslayer.cryptobuddy.persistence.PersistentDataStore;
import com.musicslayer.cryptobuddy.util.FileUtil;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;

import org.json.JSONArray;
import org.json.JSONObject;

public class BaseFiatManager extends FiatManager {
    public String getKey() { return "BaseFiatManager"; }
    public String getName() { return "BaseFiatManager"; }
    public String getFiatType() { return "BASE"; }
    public String getSettingsKey() { return "base"; }

    public void initializeHardcodedFiats() {
        resetHardcodedFiats();
        String fiatJSON = FileUtil.readFile(R.raw.asset_fiat_hardcoded);

        try {
            JSONObject jsonObject = new JSONObject(fiatJSON);
            JSONArray jsonArray = jsonObject.getJSONArray("fiats");
            for(int i = 0; i < jsonArray.length(); i++) {
                JSONObject json = jsonArray.getJSONObject(i);

                String key = json.getString("key");
                String name = json.getString("name");
                String display_name = json.getString("display_name");
                int scale = json.getInt("scale");

                Fiat fiat = Fiat.buildFiat(key, name, display_name, scale, getFiatType());
                addHardcodedFiat(fiat);
            }
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            throw new IllegalStateException(e);
        }

        PersistentDataStore.getInstance(FiatManagerList.class).updateFiatManager(this);
    }
}
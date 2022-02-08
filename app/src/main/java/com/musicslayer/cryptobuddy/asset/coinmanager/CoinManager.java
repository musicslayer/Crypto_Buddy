package com.musicslayer.cryptobuddy.asset.coinmanager;

import android.content.Context;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin_Impl;
import com.musicslayer.cryptobuddy.asset.crypto.coin.UnknownCoin;
import com.musicslayer.cryptobuddy.persistence.CoinManagerList;
import com.musicslayer.cryptobuddy.serialize.Serialization;
import com.musicslayer.cryptobuddy.util.FileUtil;
import com.musicslayer.cryptobuddy.util.ReflectUtil;

import java.util.ArrayList;
import java.util.HashMap;

abstract public class CoinManager implements Serialization.SerializableToJSON, Serialization.Versionable {
    public static ArrayList<CoinManager> coinManagers;
    public static HashMap<String, CoinManager> coinManagers_map;
    public static HashMap<String, CoinManager> coinManagers_coin_type_map;
    public static ArrayList<String> coinManagers_names;
    public static ArrayList<String> coinManagers_coin_types;

    public ArrayList<Coin> hardcoded_coins;
    public HashMap<String, Coin> hardcoded_coin_map;
    public ArrayList<String> hardcoded_coin_names;
    public ArrayList<String> hardcoded_coin_display_names;

    public ArrayList<Coin> found_coins;
    public HashMap<String, Coin> found_coin_map;
    public ArrayList<String> found_coin_names;
    public ArrayList<String> found_coin_display_names;

    public ArrayList<Coin> custom_coins;
    public HashMap<String, Coin> custom_coin_map;
    public ArrayList<String> custom_coin_names;
    public ArrayList<String> custom_coin_display_names;

    abstract public String getKey();
    abstract public String getName();
    abstract public String getCoinType();

    // Used to store persistent state
    abstract public String getSettingsKey();

    // Most times we cannot do lookup, but subclasses can override if they can.
    public Coin lookupCoin(String key, String name, String display_name, int scale, String id) { return null; }

    public CoinManager() {
        this.hardcoded_coins = new ArrayList<>();
        this.hardcoded_coin_map = new HashMap<>();
        this.hardcoded_coin_names = new ArrayList<>();
        this.hardcoded_coin_display_names = new ArrayList<>();

        this.found_coins = new ArrayList<>();
        this.found_coin_map = new HashMap<>();
        this.found_coin_names = new ArrayList<>();
        this.found_coin_display_names = new ArrayList<>();

        this.custom_coins = new ArrayList<>();
        this.custom_coin_map = new HashMap<>();
        this.custom_coin_names = new ArrayList<>();
        this.custom_coin_display_names = new ArrayList<>();
    }

    public static void initialize(Context context) {
        coinManagers = new ArrayList<>();
        coinManagers_map = new HashMap<>();
        coinManagers_coin_type_map = new HashMap<>();
        coinManagers_coin_types = new ArrayList<>();

        CoinManagerList.initializeRawArray();

        coinManagers_names = FileUtil.readFileIntoLines(context, R.raw.asset_coinmanager);
        for(String coinManagerName : coinManagers_names) {
            CoinManager coinManager = ReflectUtil.constructClassInstanceFromName("com.musicslayer.cryptobuddy.asset.coinmanager." + coinManagerName);

            // Use the deserialized dummy object to fill in the coins in this real one.
            CoinManager copyCoinManager = CoinManagerList.loadData(context, coinManager.getSettingsKey());

            // If this is a new CoinManager that wasn't previously saved, then there are no coins to add.
            if(copyCoinManager != null) {
                coinManager.addHardcodedCoin(copyCoinManager.hardcoded_coins);
                coinManager.addFoundCoin(copyCoinManager.found_coins);
                coinManager.addCustomCoin(copyCoinManager.custom_coins);
            }

            coinManagers.add(coinManager);
            coinManagers_map.put(coinManager.getKey(), coinManager);
            coinManagers_coin_type_map.put(coinManager.getCoinType(), coinManager);
            coinManagers_coin_types.add(coinManager.getCoinType());
        }
    }

    public static CoinManager getDefaultCoinManager() {
        return CoinManager.getCoinManagerFromKey("BaseCoinManager");
    }

    public static CoinManager getCoinManagerFromKey(String key) {
        CoinManager coinManager = coinManagers_map.get(key);
        if(coinManager == null) {
            coinManager = UnknownCoinManager.createUnknownCoinManager(key, "?");
        }

        return coinManager;
    }

    public static CoinManager getCoinManagerFromCoinType(String coinType) {
        CoinManager coinManager = coinManagers_coin_type_map.get(coinType);
        if(coinManager == null) {
            coinManager = UnknownCoinManager.createUnknownCoinManager("?", coinType);
        }

        return coinManager;
    }

    // Try to get the coin from storage, then try to look it up, then try to create it from the input information.
    // If all of that fails, then return an UnknownCoin instance.
    public Coin getCoin(String key, String name, String display_name, int scale, String id) {
        Coin coin = getCoinWithPrecedence(key);

        if(coin == null) {
            coin = lookupCoin(key, name, display_name, scale, id);

            if(coin == null || !coin.isComplete()) {
                coin = new Coin_Impl(key, name, display_name, scale, id, getCoinType());

                if(!coin.isComplete()) {
                    coin = UnknownCoin.createUnknownCoin(key);
                }
            }
        }

        addFoundCoin(coin);
        return coin;
    }

    private Coin getCoinWithPrecedence(String key) {
        // Maintain precedence - Hardcoded, then Found, then Custom Coins.
        Coin coin = hardcoded_coin_map.get(key);
        if(coin == null) {
            coin = found_coin_map.get(key);
        }
        if(coin == null) {
            coin = custom_coin_map.get(key);
        }

        return coin;
    }

    public void addHardcodedCoin(ArrayList<Coin> coinArrayList) {
        if(coinArrayList == null) { return; }

        for(Coin coin : coinArrayList) {
            addHardcodedCoin(coin);
        }
    }

    public void addHardcodedCoin(Coin coin) {
        if(coin == null || !coin.isComplete()) { return; }

        String key = coin.getKey();

        // Add or replace coin.
        if(hardcoded_coin_map.get(key) == null) {
            hardcoded_coins.add(coin);
            hardcoded_coin_map.put(key, coin);
            hardcoded_coin_names.add(coin.getName());
            hardcoded_coin_display_names.add(coin.getDisplayName());
        }
        else {
            hardcoded_coin_map.put(key, coin);

            int idx = hardcoded_coins.indexOf(coin);
            hardcoded_coins.set(idx, coin);
            hardcoded_coin_names.set(idx, coin.getName());
            hardcoded_coin_display_names.set(idx, coin.getDisplayName());
        }
    }

    public void addFoundCoin(ArrayList<Coin> coinArrayList) {
        if(coinArrayList == null) { return; }

        for(Coin coin : coinArrayList) {
            addFoundCoin(coin);
        }
    }

    public void addFoundCoin(Coin coin) {
        if(coin == null || !coin.isComplete()) { return; }

        String key = coin.getKey();

        // Add or replace coin.
        if(found_coin_map.get(key) == null) {
            found_coins.add(coin);
            found_coin_map.put(key, coin);
            found_coin_names.add(coin.getName());
            found_coin_display_names.add(coin.getDisplayName());
        }
        else {
            found_coin_map.put(key, coin);

            int idx = found_coins.indexOf(coin);
            found_coins.set(idx, coin);
            found_coin_names.set(idx, coin.getName());
            found_coin_display_names.set(idx, coin.getDisplayName());
        }
    }

    public void addCustomCoin(ArrayList<Coin> coinArrayList) {
        if(coinArrayList == null) { return; }

        for(Coin coin : coinArrayList) {
            addCustomCoin(coin);
        }
    }

    public void addCustomCoin(Coin coin) {
        if(coin == null || !coin.isComplete()) { return; }

        String key = coin.getKey();

        // Add or replace coin.
        if(custom_coin_map.get(key) == null) {
            custom_coins.add(coin);
            custom_coin_map.put(key, coin);
            custom_coin_names.add(coin.getName());
            custom_coin_display_names.add(coin.getDisplayName());
        }
        else {
            custom_coin_map.put(key, coin);

            int idx = custom_coins.indexOf(coin);
            custom_coins.set(idx, coin);
            custom_coin_names.set(idx, coin.getName());
            custom_coin_display_names.set(idx, coin.getDisplayName());
        }
    }

    // Hardcoded coins are normally not reset.
    public static void resetAllHardcodedCoins() {
        for(CoinManager coinManager : coinManagers) {
            coinManager.resetHardcodedCoins();
        }
    }

    public void resetHardcodedCoins() {
        hardcoded_coins = new ArrayList<>();
        hardcoded_coin_map = new HashMap<>();
        hardcoded_coin_names = new ArrayList<>();
        hardcoded_coin_display_names = new ArrayList<>();
    }

    public static void resetAllFoundCoins() {
        for(CoinManager coinManager : coinManagers) {
            coinManager.resetFoundCoins();
        }
    }

    public void resetFoundCoins() {
        found_coins = new ArrayList<>();
        found_coin_map = new HashMap<>();
        found_coin_names = new ArrayList<>();
        found_coin_display_names = new ArrayList<>();
    }

    public static void resetAllCustomCoins() {
        for(CoinManager coinManager : coinManagers) {
            coinManager.resetCustomCoins();
        }
    }

    public void resetCustomCoins() {
        custom_coins = new ArrayList<>();
        custom_coin_map = new HashMap<>();
        custom_coin_names = new ArrayList<>();
        custom_coin_display_names = new ArrayList<>();
    }

    public static ArrayList<Coin> getAllCoins() {
        ArrayList<Coin> coins = new ArrayList<>();

        for(CoinManager coinManager : coinManagers) {
            coins.addAll(coinManager.getCoins());
        }

        return coins;
    }

    public ArrayList<Coin> getCoins() {
        // Here we take into account precedence by favoring hardcoded coins over found coins over custom coins.
        // We don't actually delete the shadowed coins from the CoinManager, we merely don't add them to the list this method returns.
        ArrayList<Coin> coins = new ArrayList<>();

        // Found coins -> remove hardcoded keys
        ArrayList<Coin> copy_found_coins = new ArrayList<>(found_coins);
        copy_found_coins.removeAll(hardcoded_coins);

        // Custom coins -> remove hardcoded/found keys
        ArrayList<Coin> copy_custom_coins = new ArrayList<>(custom_coins);
        copy_custom_coins.removeAll(hardcoded_coins);
        copy_custom_coins.removeAll(copy_found_coins);

        coins.addAll(hardcoded_coins);
        coins.addAll(copy_found_coins);
        coins.addAll(copy_custom_coins);

        return coins;
    }

    public static ArrayList<String> getAllCoinNames() {
        ArrayList<String> names = new ArrayList<>();

        for(CoinManager coinManager : coinManagers) {
            names.addAll(coinManager.getCoinNames());
        }

        return names;
    }

    public ArrayList<String> getCoinNames() {
        ArrayList<String> names = new ArrayList<>();

        for(Coin coin : getCoins()) {
            names.add(coin.getName());
        }

        return names;
    }

    public static ArrayList<String> getAllCoinDisplayNames() {
        ArrayList<String> displayNames = new ArrayList<>();

        for(CoinManager coinManager : coinManagers) {
            displayNames.addAll(coinManager.getCoinDisplayNames());
        }

        return displayNames;
    }

    public ArrayList<String> getCoinDisplayNames() {
        ArrayList<String> displayNames = new ArrayList<>();

        for(Coin coin : getCoins()) {
            displayNames.add(coin.getDisplayName());
        }

        return displayNames;
    }

    public String serializationVersion() { return "1"; }

    public String serializeToJSON() throws org.json.JSONException {
        // Just serialize the coin array lists. CoinManagerList keeps track of which CoinManager had these.
        return new Serialization.JSONObjectWithNull()
            .put("key", Serialization.string_serialize(getKey()))
            .put("coin_type", Serialization.string_serialize(getCoinType()))
            .put("hardcoded_coins", new Serialization.JSONArrayWithNull(Serialization.coin_serializeArrayList(hardcoded_coins)))
            .put("found_coins", new Serialization.JSONArrayWithNull(Serialization.coin_serializeArrayList(found_coins)))
            .put("custom_coins", new Serialization.JSONArrayWithNull(Serialization.coin_serializeArrayList(custom_coins)))
            .toStringOrNull();
    }

    public static CoinManager deserializeFromJSON1(String s) throws org.json.JSONException {
        Serialization.JSONObjectWithNull o = new Serialization.JSONObjectWithNull(s);
        String key = Serialization.string_deserialize(o.getString("key"));
        String coin_type = Serialization.string_deserialize(o.getString("coin_type"));
        ArrayList<Coin> hardcoded_coins = Serialization.coin_deserializeArrayList(o.getJSONArrayString("hardcoded_coins"));
        ArrayList<Coin> found_coins = Serialization.coin_deserializeArrayList(o.getJSONArrayString("found_coins"));
        ArrayList<Coin> custom_coins = Serialization.coin_deserializeArrayList(o.getJSONArrayString("custom_coins"));

        // This is a dummy object that only has to hold onto the coin array lists.
        // We don't need to call the proper add* methods here.
        CoinManager coinManager = UnknownCoinManager.createUnknownCoinManager(key, coin_type);
        coinManager.hardcoded_coins = hardcoded_coins;
        coinManager.found_coins = found_coins;
        coinManager.custom_coins = custom_coins;

        return coinManager;
    }
}

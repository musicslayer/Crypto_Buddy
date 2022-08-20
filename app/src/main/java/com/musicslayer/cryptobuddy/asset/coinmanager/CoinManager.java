package com.musicslayer.cryptobuddy.asset.coinmanager;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.asset.crypto.coin.UnknownCoin;
import com.musicslayer.cryptobuddy.data.bridge.DataBridge;
import com.musicslayer.cryptobuddy.util.FileUtil;
import com.musicslayer.cryptobuddy.util.HashMapUtil;
import com.musicslayer.cryptobuddy.util.ReflectUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

abstract public class CoinManager implements DataBridge.SerializableToJSON  {
    public static ArrayList<CoinManager> coinManagers;
    public static HashMap<String, CoinManager> coinManagers_map;
    public static HashMap<String, CoinManager> coinManagers_settings_map;
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

    abstract public void initializeHardcodedCoins();

    // Most times we cannot do lookup, but subclasses can override if they can.
    public Coin lookupCoin(String key, String name, String display_name, int scale) { return null; }

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

    public static void initialize() {
        coinManagers = new ArrayList<>();
        coinManagers_map = new HashMap<>();
        coinManagers_settings_map = new HashMap<>();
        coinManagers_coin_type_map = new HashMap<>();
        coinManagers_coin_types = new ArrayList<>();

        coinManagers_names = FileUtil.readFileIntoLines(R.raw.asset_coinmanager);
        for(String coinManagerName : coinManagers_names) {
            CoinManager coinManager = ReflectUtil.constructClassInstanceFromName("com.musicslayer.cryptobuddy.asset.coinmanager." + coinManagerName);

            coinManagers.add(coinManager);
            coinManagers_map.put(coinManager.getKey(), coinManager);
            coinManagers_settings_map.put(coinManager.getSettingsKey(), coinManager);
            coinManagers_coin_type_map.put(coinManager.getCoinType(), coinManager);
            coinManagers_coin_types.add(coinManager.getCoinType());

            coinManager.initializeHardcodedCoins();
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

    public static CoinManager getCoinManagerFromSettingsKey(String settings_key) {
        CoinManager coinManager = coinManagers_settings_map.get(settings_key);
        if(coinManager == null) {
            coinManager = UnknownCoinManager.createUnknownCoinManager("?", "?");
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

    public Coin getCoin(String key, String name, String display_name, int scale, String id) {
        // Try to get the coin from storage, then try to look it up, then try to create it from the input information.
        // If all of that fails, then return an UnknownCoin instance.
        Coin coin = getCoinWithPrecedence(key);

        if(coin == null) {
            coin = lookupCoin(key, name, display_name, scale);

            if(coin == null || !coin.isComplete()) {
                coin = Coin.buildCoin(key, name, display_name, scale, getCoinType(), id);

                if(!coin.isComplete()) {
                    coin = UnknownCoin.createUnknownCoin(key, name, display_name, scale, getCoinType(), id);
                }
            }

            addFoundCoin(coin);
        }

        return coin;
    }

    public Coin getExistingCoin(String key, String name, String display_name, int scale, HashMap<String, String> additionalInfo) {
        // Only return a stored coin. Do not build or lookup one.
        Coin coin = getCoinWithPrecedence(key);
        if(coin == null) {
            coin = UnknownCoin.createUnknownCoin(key, name, display_name, scale, getCoinType(), additionalInfo);
        }

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

    public Coin getHardcodedCoin(String key) {
        // Only allow the hardcoded coin to be found.
        return hardcoded_coin_map.get(key);
    }

    // Hardcoded coins are not normally deleted.
    public void removeHardcodedCoin(Coin coin) {
        String key = coin.getKey();
        if(hardcoded_coin_map.get(key) != null) {
            hardcoded_coins.remove(coin);
            HashMapUtil.removeValueFromMap(hardcoded_coin_map, key);
            hardcoded_coin_names.remove(coin.getName());
            hardcoded_coin_display_names.remove(coin.getDisplayName());
        }
    }

    public void removeFoundCoin(Coin coin) {
        String key = coin.getKey();
        if(found_coin_map.get(key) != null) {
            found_coins.remove(coin);
            HashMapUtil.removeValueFromMap(found_coin_map, key);
            found_coin_names.remove(coin.getName());
            found_coin_display_names.remove(coin.getDisplayName());
        }
    }

    public void removeCustomCoin(Coin coin) {
        String key = coin.getKey();
        if(custom_coin_map.get(key) != null) {
            custom_coins.remove(coin);
            HashMapUtil.removeValueFromMap(custom_coin_map, key);
            custom_coin_names.remove(coin.getName());
            custom_coin_display_names.remove(coin.getDisplayName());
        }
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

    // Hardcoded coins are not normally deleted.
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

    @Override
    public void serializeToJSON(DataBridge.Writer o) throws IOException {
        o.beginObject()
                .serialize("!V!", "3", String.class)
                .serialize("key", getKey(), String.class)
                .serialize("coin_type", getCoinType(), String.class)
                .serializeArrayList("hardcoded_coins", hardcoded_coins, Coin.class)
                .serializeArrayList("found_coins", found_coins, Coin.class)
                .serializeArrayList("custom_coins", custom_coins, Coin.class)
                .endObject();
    }

    public static CoinManager deserializeFromJSON(DataBridge.Reader o) throws IOException {
        o.beginObject();

        String version = o.deserialize("!V!", String.class);
        CoinManager coinManager;

        if("3".equals(version)) {
            String key = o.deserialize("key", String.class);
            String coin_type = o.deserialize("coin_type", String.class);
            ArrayList<Coin> hardcoded_coins = o.deserializeArrayList("hardcoded_coins", Coin.class);
            ArrayList<Coin> found_coins = o.deserializeArrayList("found_coins", Coin.class);
            ArrayList<Coin> custom_coins = o.deserializeArrayList("custom_coins", Coin.class);
            o.endObject();

            // This is a dummy object that only has to hold onto the coin array lists.
            // We don't need to call the proper add* methods here.
            coinManager = UnknownCoinManager.createUnknownCoinManager(key, coin_type);
            coinManager.hardcoded_coins = hardcoded_coins;
            coinManager.found_coins = found_coins;
            coinManager.custom_coins = custom_coins;
        }
        else {
            throw new IllegalStateException("version = " + version);
        }

        return coinManager;
    }
}

package com.musicslayer.cryptobuddy.api.price;

import com.musicslayer.cryptobuddy.asset.Asset;
import com.musicslayer.cryptobuddy.data.bridge.DataBridge;
import com.musicslayer.cryptobuddy.data.bridge.LegacyDataBridge;
import com.musicslayer.cryptobuddy.rich.RichStringBuilder;
import com.musicslayer.cryptobuddy.data.bridge.LegacySerialization;
import com.musicslayer.cryptobuddy.settings.setting.PriceDisplaySetting;
import com.musicslayer.cryptobuddy.transaction.AssetPrice;
import com.musicslayer.cryptobuddy.transaction.AssetQuantity;
import com.musicslayer.cryptobuddy.transaction.Timestamp;
import com.musicslayer.cryptobuddy.util.HashMapUtil;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class PriceData implements LegacySerialization.SerializableToJSON, DataBridge.SerializableToJSON {
    final public CryptoPrice cryptoPrice;
    final public PriceAPI priceAPI_price;
    final public PriceAPI priceAPI_marketCap;
    final public HashMap<Asset, AssetQuantity> priceHashMap;
    final public HashMap<Asset, AssetQuantity> marketCapHashMap;
    final public Timestamp timestamp_price;
    final public Timestamp timestamp_marketCap;

    public static String legacy_serializationType(String version) {
        return "!OBJECT!";
    }

    @Override
    public String legacy_serializeToJSON() throws org.json.JSONException {
        return new LegacyDataBridge.JSONObjectDataBridge()
                .serialize("cryptoPrice", cryptoPrice, CryptoPrice.class)
                .serialize("priceAPI_price", priceAPI_price, PriceAPI.class)
                .serialize("priceAPI_marketCap", priceAPI_marketCap, PriceAPI.class)
                .putJSONObjectString("priceHashMap", splitHashMap(priceHashMap))
                .putJSONObjectString("marketCapHashMap", splitHashMap(marketCapHashMap))
                .serialize("timestamp_price", timestamp_price, Timestamp.class)
                .serialize("timestamp_marketCap", timestamp_marketCap, Timestamp.class)
                .toStringOrNull();
    }

    public static String splitHashMap(HashMap<Asset, AssetQuantity> hashMap) throws JSONException {
        // Split HashMaps so Assets are referenced but AssetQuantities are serialized.
        if(hashMap == null) { return null; }

        ArrayList<Asset> keyArrayList = new ArrayList<>(hashMap.keySet());
        ArrayList<AssetQuantity> valueArrayList = new ArrayList<>();
        for(Asset key : keyArrayList) {
            valueArrayList.add(hashMap.get(key));
        }

        return new LegacyDataBridge.JSONObjectDataBridge()
                .referenceArrayList("keys", keyArrayList, Asset.class)
                .serializeArrayList("values", valueArrayList, AssetQuantity.class)
                .toStringOrNull();
    }

    public static PriceData legacy_deserializeFromJSON(String s, String version) throws org.json.JSONException {
        LegacyDataBridge.JSONObjectDataBridge o = new LegacyDataBridge.JSONObjectDataBridge(s);
        CryptoPrice cryptoPrice = o.deserialize("cryptoPrice", CryptoPrice.class);
        PriceAPI priceAPI_price = o.deserialize("priceAPI_price", PriceAPI.class);
        PriceAPI priceAPI_marketCap = o.deserialize("priceAPI_marketCap", PriceAPI.class);
        HashMap<Asset, AssetQuantity> priceHashMap = combineHashMap(o.getJSONObjectString("priceHashMap"));
        HashMap<Asset, AssetQuantity> marketCapHashMap = combineHashMap(o.getJSONObjectString("marketCapHashMap"));
        Timestamp timestamp_price = o.deserialize("timestamp_price", Timestamp.class);
        Timestamp timestamp_marketCap = o.deserialize("timestamp_marketCap", Timestamp.class);
        return new PriceData(cryptoPrice, priceAPI_price, priceAPI_marketCap, priceHashMap, marketCapHashMap, timestamp_price, timestamp_marketCap);
    }

    public static HashMap<Asset, AssetQuantity> combineHashMap(String s) throws org.json.JSONException {
        // Combine HashMaps so Assets are dereferenced but AssetQuantities are deserialized.
        if(s == null) { return null; }

        LegacyDataBridge.JSONObjectDataBridge o = new LegacyDataBridge.JSONObjectDataBridge(s);

        ArrayList<Asset> arrayListT = o.dereferenceArrayList("keys", Asset.class);
        ArrayList<AssetQuantity> arrayListU = o.deserializeArrayList("values", AssetQuantity.class);

        if(arrayListT == null || arrayListU == null || arrayListT.size() != arrayListU.size()) {
            return null;
        }

        HashMap<Asset, AssetQuantity> hashMap = new HashMap<>();
        for(int i = 0; i < arrayListT.size(); i++) {
            hashMap.put(arrayListT.get(i), arrayListU.get(i));
        }

        return hashMap;
    }

    @Override
    public void serializeToJSON(DataBridge.Writer o) throws IOException {
        o.beginObject()
                .serialize("cryptoPrice", cryptoPrice, CryptoPrice.class)
                .serialize("priceAPI_price", priceAPI_price, PriceAPI.class)
                .serialize("priceAPI_marketCap", priceAPI_marketCap, PriceAPI.class)
                .splitHashMap("priceHashMap", priceHashMap, Asset.class, AssetQuantity.class)
                .splitHashMap("marketCapHashMap", marketCapHashMap, Asset.class, AssetQuantity.class)
                .serialize("timestamp_price", timestamp_price, Timestamp.class)
                .serialize("timestamp_marketCap", timestamp_marketCap, Timestamp.class)
                .endObject();
    }

    public static PriceData deserializeFromJSON(DataBridge.Reader o) throws IOException {
        o.beginObject();
        CryptoPrice cryptoPrice = o.deserialize("cryptoPrice", CryptoPrice.class);
        PriceAPI priceAPI_price = o.deserialize("priceAPI_price", PriceAPI.class);
        PriceAPI priceAPI_marketCap = o.deserialize("priceAPI_marketCap", PriceAPI.class);
        HashMap<Asset, AssetQuantity> priceHashMap = o.combineHashMap("priceHashMap", Asset.class, AssetQuantity.class);
        HashMap<Asset, AssetQuantity> marketCapHashMap = o.combineHashMap("marketCapHashMap", Asset.class, AssetQuantity.class);
        Timestamp timestamp_price = o.deserialize("timestamp_price", Timestamp.class);
        Timestamp timestamp_marketCap = o.deserialize("timestamp_marketCap", Timestamp.class);
        o.endObject();

        return new PriceData(cryptoPrice, priceAPI_price, priceAPI_marketCap, priceHashMap, marketCapHashMap, timestamp_price, timestamp_marketCap);
    }

    public PriceData(CryptoPrice cryptoPrice, PriceAPI priceAPI_price, PriceAPI priceAPI_marketCap, HashMap<Asset, AssetQuantity> priceHashMap, HashMap<Asset, AssetQuantity> marketCapHashMap, Timestamp timestamp_price, Timestamp timestamp_marketCap) {
        this.cryptoPrice = cryptoPrice;
        this.priceAPI_price = priceAPI_price;
        this.priceAPI_marketCap = priceAPI_marketCap;
        this.priceHashMap = priceHashMap;
        this.marketCapHashMap = marketCapHashMap;
        this.timestamp_price = timestamp_price;
        this.timestamp_marketCap = timestamp_marketCap;
    }

    public static PriceData getAllData(CryptoPrice cryptoPrice) {
        PriceAPI priceAPI_price_f = UnknownPriceAPI.createUnknownPriceAPI(null);
        PriceAPI priceAPI_marketCap_f = UnknownPriceAPI.createUnknownPriceAPI(null);
        HashMap<Asset, AssetQuantity> priceHashMap_f = null;
        HashMap<Asset, AssetQuantity> marketCapHashMap_f = null;

        // Get price information.
        for(PriceAPI priceAPI : PriceAPI.price_apis) {
            if(!priceAPI.isSupported(cryptoPrice)) {
                continue;
            }

            priceHashMap_f = priceAPI.getPrice(cryptoPrice);
            if(priceHashMap_f != null) {
                priceAPI_price_f = priceAPI;
                break;
            }
        }

        // Get market cap information.
        for(PriceAPI priceAPI : PriceAPI.price_apis) {
            if(!priceAPI.isSupported(cryptoPrice)) {
                continue;
            }

            marketCapHashMap_f = priceAPI.getMarketCap(cryptoPrice);
            if(marketCapHashMap_f != null) {
                priceAPI_marketCap_f = priceAPI;
                break;
            }
        }

        return new PriceData(cryptoPrice, priceAPI_price_f, priceAPI_marketCap_f, priceHashMap_f, marketCapHashMap_f, new Timestamp(), new Timestamp());
    }

    public static PriceData getPriceData(CryptoPrice cryptoPrice) {
        PriceAPI priceAPI_price_f = UnknownPriceAPI.createUnknownPriceAPI(null);
        PriceAPI priceAPI_marketCap_f = UnknownPriceAPI.createUnknownPriceAPI(null);
        HashMap<Asset, AssetQuantity> priceHashMap_f = null;
        HashMap<Asset, AssetQuantity> marketCapHashMap_f = null;

        // Get price information.
        for(PriceAPI priceAPI : PriceAPI.price_apis) {
            if(!priceAPI.isSupported(cryptoPrice)) {
                continue;
            }

            priceHashMap_f = priceAPI.getPrice(cryptoPrice);
            if(priceHashMap_f != null) {
                priceAPI_price_f = priceAPI;
                break;
            }
        }

        return new PriceData(cryptoPrice, priceAPI_price_f, priceAPI_marketCap_f, priceHashMap_f, marketCapHashMap_f, new Timestamp(), new Timestamp());
    }

    public static PriceData getMarketCapData(CryptoPrice cryptoPrice) {
        PriceAPI priceAPI_price_f = UnknownPriceAPI.createUnknownPriceAPI(null);
        PriceAPI priceAPI_marketCap_f = UnknownPriceAPI.createUnknownPriceAPI(null);
        HashMap<Asset, AssetQuantity> priceHashMap_f = null;
        HashMap<Asset, AssetQuantity> marketCapHashMap_f = null;

        // Get market cap information.
        for(PriceAPI priceAPI : PriceAPI.price_apis) {
            if(!priceAPI.isSupported(cryptoPrice)) {
                continue;
            }

            marketCapHashMap_f = priceAPI.getMarketCap(cryptoPrice);
            if(marketCapHashMap_f != null) {
                priceAPI_marketCap_f = priceAPI;
                break;
            }
        }

        return new PriceData(cryptoPrice, priceAPI_price_f, priceAPI_marketCap_f, priceHashMap_f, marketCapHashMap_f, new Timestamp(), new Timestamp());
    }

    public static PriceData getNoData(CryptoPrice cryptoPrice) {
        PriceAPI priceAPI_price_f = UnknownPriceAPI.createUnknownPriceAPI(null);
        PriceAPI priceAPI_marketCap_f = UnknownPriceAPI.createUnknownPriceAPI(null);
        HashMap<Asset, AssetQuantity> priceHashMap_f = null;
        HashMap<Asset, AssetQuantity> marketCapHashMap_f = null;

        return new PriceData(cryptoPrice, priceAPI_price_f, priceAPI_marketCap_f, priceHashMap_f, marketCapHashMap_f, new Timestamp(), new Timestamp());
    }

    public String getPriceInfoString(Asset asset, boolean isRich) {
        // Get info for "CryptoPriceDialog".
        // Note that prices are all positive, so we do not need to call "appendAssetQuantity"
        RichStringBuilder s = new RichStringBuilder(isRich);

        if(!isPriceComplete(asset)) {
            s.appendRich("(Price information not present.)");
        }
        else {
            AssetQuantity priceAssetQuantity = HashMapUtil.getValueFromMap(priceHashMap, asset);

            AssetPrice assetPrice = new AssetPrice(new AssetQuantity("1", asset), priceAssetQuantity);
            s.appendRich("Forward Price = ").appendRich(assetPrice.toString());
            if("ForwardBackward".equals(PriceDisplaySetting.value)) {
                s.appendRich("\nBackward Price = ").appendRich(assetPrice.reverseAssetPrice().toString());
            }
            s.appendRich("\n\nPrice Data Source = ").appendRich(priceAPI_price.getDisplayName());
            s.appendRich("\nPrice Data Timestamp = ").appendRich(timestamp_price.toString());
        }

        if(!isMarketCapComplete(asset)) {
            s.appendRich("\n\n(Market cap information not present.)");
        }
        else {
            AssetQuantity marketCapAssetQuantity = HashMapUtil.getValueFromMap(marketCapHashMap, asset);

            s.appendRich("\n\nMarket Cap = ").appendRich(marketCapAssetQuantity.toString());
            s.appendRich("\n\nMarket Cap Data Source = ").appendRich(priceAPI_marketCap.getDisplayName());
            s.appendRich("\nMarket Cap Data Timestamp = ").appendRich(timestamp_marketCap.toString());
        }

        return s.toString();
    }

    public String getConverterInfoString(String amount, Asset assetPrimary, Asset assetSecondary, boolean isRich) {
        // Get info for "CryptoConverterDialog".
        // Note that prices are all positive, so we do not need to call "appendAssetQuantity"
        RichStringBuilder s = new RichStringBuilder(isRich);

        if(!isPriceComplete(assetPrimary) || !isPriceComplete(assetSecondary)) {
            s.appendRich("(Conversion information not present.)");
        }
        else {
            AssetQuantity primaryPriceAssetQuantity = HashMapUtil.getValueFromMap(priceHashMap, assetPrimary);
            AssetQuantity secondaryPriceAssetQuantity = HashMapUtil.getValueFromMap(priceHashMap, assetSecondary);

            AssetQuantity primaryAssetQuantity = new AssetQuantity(amount, assetPrimary);
            AssetPrice primaryAssetPrice = new AssetPrice(new AssetQuantity("1", assetPrimary), primaryPriceAssetQuantity);
            AssetPrice secondaryAssetPrice = new AssetPrice(new AssetQuantity("1", assetSecondary), secondaryPriceAssetQuantity);
            AssetQuantity secondaryAssetQuantity = primaryAssetQuantity.convert(primaryAssetPrice).convert(secondaryAssetPrice.reverseAssetPrice());

            s.appendRich("Conversion:\n").appendRich(primaryAssetQuantity.toString()).appendRich(" = ").appendRich(secondaryAssetQuantity.toString());

            s.appendRich("\n\nForward Prices:\n").appendRich(primaryAssetPrice.toString()).appendRich("\n").appendRich(secondaryAssetPrice.toString());
            if("ForwardBackward".equals(PriceDisplaySetting.value)) {
                s.appendRich("\n\nBackward Prices:\n").appendRich(primaryAssetPrice.reverseAssetPrice().toString()).appendRich("\n").appendRich(secondaryAssetPrice.reverseAssetPrice().toString());
            }
            s.appendRich("\n\nPrice Data Source = ").appendRich(priceAPI_price.getDisplayName());
            s.appendRich("\nPrice Data Timestamp = ").appendRich(timestamp_price.toString());
        }

        return s.toString();
    }

    public boolean isComplete() {
        return isPriceComplete() && isMarketCapComplete();
    }

    public boolean isPriceComplete() {
        return !(priceAPI_price instanceof UnknownPriceAPI) && priceHashMap != null;
    }

    public boolean isMarketCapComplete() {
        return !(priceAPI_marketCap instanceof UnknownPriceAPI) && marketCapHashMap != null;
    }

    // Since we get the info for multiple assets, these functions check that a particular asset has complete information.
    public boolean isComplete(Asset asset) {
        return isPriceComplete(asset) && isMarketCapComplete(asset);
    }

    public boolean isPriceComplete(Asset asset) {
        return isPriceComplete() && HashMapUtil.getValueFromMap(priceHashMap, asset) != null;
    }

    public boolean isMarketCapComplete(Asset asset) {
        return isMarketCapComplete() && HashMapUtil.getValueFromMap(marketCapHashMap, asset) != null;
    }

    // Check that all assets have complete data.
    public boolean isFull() {
        return isPriceFull() && isMarketFull();
    }

    public boolean isPriceFull() {
        return isPriceComplete() && priceHashMap.keySet().containsAll(cryptoPrice.assetArrayList);
    }

    public boolean isMarketFull() {
        return isMarketCapComplete() && marketCapHashMap.keySet().containsAll(cryptoPrice.assetArrayList);
    }
}

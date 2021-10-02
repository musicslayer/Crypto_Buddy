package com.musicslayer.cryptobuddy.api.price;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.fiat.USD;
import com.musicslayer.cryptobuddy.transaction.AssetPrice;
import com.musicslayer.cryptobuddy.transaction.AssetQuantity;
import com.musicslayer.cryptobuddy.util.DateTime;
import com.musicslayer.cryptobuddy.util.Serialization;
import com.musicslayer.cryptobuddy.util.Toast;

import org.json.JSONObject;

import java.util.Date;

// TODO should fields store strings, or custom objects?

public class PriceData implements Serialization.SerializableToJSON {
    public Crypto crypto;
    public PriceAPI priceAPI_usdPrice;
    public PriceAPI priceAPI_usdMarketCap;
    public String usdPrice; // BD
    public String usdMarketCap; // BD
    //public String timestamp;
    // TODO priceData from API should tell us the date that the data is from.

    public String serializationVersion() { return "1"; }

    public String serializeToJSON() {
        return "{\"crypto\":" + Serialization.serialize(crypto) + ",\"priceAPI_usdPrice\":" + Serialization.serialize(priceAPI_usdPrice) + ",\"priceAPI_usdMarketCap\":" + Serialization.serialize(priceAPI_usdMarketCap) + ",\"usdPrice\":\"" + usdPrice + "\",\"usdMarketCap\":\"" + usdMarketCap + "\"}";
    }

    public static PriceData deserializeFromJSON1(String s) throws org.json.JSONException {
        JSONObject o = new JSONObject(s);
        Crypto crypto = Serialization.deserialize(o.getJSONObject("crypto").toString(), Crypto.class);
        PriceAPI priceAPI_usdPrice = Serialization.deserialize(o.getJSONObject("priceAPI_usdPrice").toString(), PriceAPI.class);
        PriceAPI priceAPI_usdMarketCap = Serialization.deserialize(o.getJSONObject("priceAPI_usdMarketCap").toString(), PriceAPI.class);
        String usdPrice = o.getString("usdPrice");
        String usdMarketCap = o.getString("usdMarketCap");
        return new PriceData(crypto, priceAPI_usdPrice, priceAPI_usdMarketCap, usdPrice, usdMarketCap, DateTime.toDateString(new Date()));
    }

    public PriceData(Crypto crypto, PriceAPI priceAPI_usdPrice, PriceAPI priceAPI_usdMarketCap, String usdPrice, String usdMarketCap, String timestamp) {
        this.crypto = crypto;
        this.priceAPI_usdPrice = priceAPI_usdPrice;
        this.priceAPI_usdMarketCap = priceAPI_usdMarketCap;
        this.usdPrice = usdPrice;
        this.usdMarketCap = usdMarketCap;
        //this.timestamp = timestamp;
    }

    public static PriceData getPriceData(Crypto crypto) {
        PriceAPI priceAPI_usdPrice_f = UnknownPriceAPI.createUnknownPriceAPI(null);
        PriceAPI priceAPI_usdMarketCap_f = UnknownPriceAPI.createUnknownPriceAPI(null);
        String usdPrice_f = null;
        String usdMarketCap_f = null;

        // Get USD price information.
        for(PriceAPI priceAPI : PriceAPI.price_apis) {
            if(!priceAPI.isSupported(crypto)) {
                continue;
            }

            usdPrice_f = priceAPI.getUSDPrice(crypto);
            if(usdPrice_f != null) {
                priceAPI_usdPrice_f = priceAPI;
                break;
            }
        }

        // Get USD market cap information.
        for(PriceAPI priceAPI : PriceAPI.price_apis) {
            if(!priceAPI.isSupported(crypto)) {
                continue;
            }

            usdMarketCap_f = priceAPI.getUSDMarketCap(crypto);
            if(usdMarketCap_f != null) {
                priceAPI_usdMarketCap_f = priceAPI;
                break;
            }
        }

        return new PriceData(crypto, priceAPI_usdPrice_f, priceAPI_usdMarketCap_f, usdPrice_f, usdMarketCap_f, DateTime.toDateString(new Date()));
    }

    public boolean isComplete() {
        return !(priceAPI_usdPrice instanceof UnknownPriceAPI) && !(priceAPI_usdMarketCap instanceof UnknownPriceAPI) && usdPrice != null && usdMarketCap != null;
    }

    public boolean alertUser() {
        // Show a toast if some information could not be found.
        // Return true if any toast was shown, or false if nothing had to be shown.
        if(isComplete()) {
            return false;
        }
        else {
            Toast.showToast("no_price_data");
            return true;
        }
    }

    public AssetPrice getAssetPrice() {
        return new AssetPrice(new AssetQuantity("1", crypto), new AssetQuantity(usdPrice, new USD()));
    }
}

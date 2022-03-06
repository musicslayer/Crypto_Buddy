package com.musicslayer.cryptobuddy.api.price;

import com.musicslayer.cryptobuddy.asset.Asset;
import com.musicslayer.cryptobuddy.asset.CoinbaseAsset;
import com.musicslayer.cryptobuddy.asset.coinmanager.CoinManager;
import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.fiat.Fiat;
import com.musicslayer.cryptobuddy.dialog.ProgressDialogFragment;
import com.musicslayer.cryptobuddy.transaction.AssetPrice;
import com.musicslayer.cryptobuddy.transaction.AssetQuantity;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;
import com.musicslayer.cryptobuddy.util.WebUtil;

import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;

public class Coinbase extends PriceAPI {
    public String getName() { return "Coinbase"; }
    public String getDisplayName() { return "Coinbase REST API V2"; }

    public boolean isSupported(CryptoPrice cryptoPrice) {
        // Only assets on this exchange are supported
        for(Asset asset : cryptoPrice.assetArrayList) {
            if(!(asset instanceof CoinbaseAsset)) {
                return false;
            }
        }
        return true;
    }

    public HashMap<Asset, AssetQuantity> getPrice(CryptoPrice cryptoPrice) {
        Fiat priceFiat = cryptoPrice.fiat;
        String priceFiatName = priceFiat.getName();

        // Separate assetArrayList into fiat and crypto.
        ArrayList<Asset> fiatArrayList = new ArrayList<>();
        ArrayList<Asset> cryptoArrayList = new ArrayList<>();

        for(Asset asset : cryptoPrice.assetArrayList) {
            String coinbaseType = ((CoinbaseAsset)asset).getCoinbaseType();
            if("fiat".equals(coinbaseType)) {
                fiatArrayList.add(asset);
            }
            else if("crypto".equals(coinbaseType)) {
                cryptoArrayList.add(asset);
            }
        }

        HashMap<Asset, AssetQuantity> priceHashMap = new HashMap<>();

        // Cryptos
        ProgressDialogFragment.updateProgressSubtitle("Processing Crypto...");
        String priceDataCryptoJSON = WebUtil.get("https://api.coinbase.com/v2/exchange-rates?currency=" + priceFiatName);
        if(priceDataCryptoJSON != null) {
            try {
                JSONObject json = new JSONObject(priceDataCryptoJSON);
                JSONObject data = json.getJSONObject("data");
                JSONObject rates = data.getJSONObject("rates");

                for(Asset crypto : cryptoArrayList) {
                    if(rates.has(crypto.getName())) {
                        // These prices are the inverse of the quantity we want.
                        BigDecimal d = new BigDecimal(rates.getString(crypto.getName()));
                        d = BigDecimal.ONE.divide(d, 50, RoundingMode.HALF_UP);
                        priceHashMap.put(crypto, new AssetQuantity(d.toPlainString(), priceFiat));
                    }
                }
            }
            catch(Exception e) {
                // Ignore error and return null.
                // Even though some entries were filled, something went wrong so we assume the data may be suspect.
                ThrowableUtil.processThrowable(e);
                return null;
            }
        }

        // Fiats
        // The strategy is to get the price of Bitcoin in all our fiats, and then use that for the conversion.
        ProgressDialogFragment.updateProgressSubtitle("Processing Fiats...");

        Crypto conversionCrypto = CoinManager.getDefaultCoinManager().getHardcodedCoin("BTC");
        String priceDataPriceFiatJSON = WebUtil.get("https://api.coinbase.com/v2/prices/" + conversionCrypto.getName() + "-" + priceFiatName + "/spot");

        BigDecimal dPrice;
        try {
            JSONObject json = new JSONObject(priceDataPriceFiatJSON);
            JSONObject data = json.getJSONObject("data");
            dPrice = new BigDecimal(data.getString("amount"));
        }
        catch(Exception e) {
            // Ignore error and return null.
            // Even though some entries were filled, something went wrong so we assume the data may be suspect.
            ThrowableUtil.processThrowable(e);
            return null;
        }

        for(Asset fiat : fiatArrayList) {
            // Hardcode this to be exact.
            if(priceFiatName.equals(fiat.getName())) {
                priceHashMap.put(fiat, new AssetQuantity("1", priceFiat));
                continue;
            }

            String priceDataFiatJSON = WebUtil.get("https://api.coinbase.com/v2/prices/" + conversionCrypto.getName() + "-" + fiat.getName() + "/spot");
            if(priceDataFiatJSON != null) {
                try {
                    JSONObject json = new JSONObject(priceDataFiatJSON);
                    JSONObject data = json.getJSONObject("data");
                    BigDecimal dFiat = new BigDecimal(data.getString("amount"));

                    AssetQuantity fiatAssetQuantity = new AssetQuantity("1", fiat);
                    AssetPrice fiatAssetPrice = new AssetPrice(new AssetQuantity(dFiat.toPlainString(), fiat), new AssetQuantity("1", conversionCrypto));
                    AssetPrice priceAssetPrice = new AssetPrice(new AssetQuantity(dPrice.toPlainString(), priceFiat), new AssetQuantity("1", conversionCrypto));
                    AssetQuantity priceAssetQuantity = fiatAssetQuantity.convert(fiatAssetPrice).convert(priceAssetPrice.reverseAssetPrice());

                    priceHashMap.put(fiat, priceAssetQuantity);
                }
                catch(Exception e) {
                    // Ignore error and return null.
                    // Even though some entries were filled, something went wrong so we assume the data may be suspect.
                    ThrowableUtil.processThrowable(e);
                    return null;
                }
            }
        }

        return priceHashMap;
    }

    public HashMap<Asset, AssetQuantity> getMarketCap(CryptoPrice cryptoPrice) {
        // This class should not be used to get Market Cap data.
        return null;
    }
}

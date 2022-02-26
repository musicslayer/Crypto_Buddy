package com.musicslayer.cryptobuddy.api.chart;

import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.asset.fiat.Fiat;
import com.musicslayer.cryptobuddy.asset.fiatmanager.FiatManager;
import com.musicslayer.cryptobuddy.transaction.Timestamp;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;
import com.musicslayer.cryptobuddy.util.WebUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;

// For these APIs, we can only query one asset at a time.

public class CoinGecko extends ChartAPI {
    public String getName() { return "CoinGecko"; }
    public String getDisplayName() { return "CoinGecko API V3"; }

    public boolean isSupported(CryptoChart cryptoChart) {
        // Right now, everything is supported.
        return true;
    }

    public ArrayList<PricePoint> getPricePoints(CryptoChart cryptoChart) {
        // For now, just assume USD and that it's a coin.
        Fiat priceFiat = FiatManager.getDefaultFiatManager().getHardcodedFiat("USD");
        String priceFiatName = priceFiat.getCoinGeckoID();

        Coin coin = (Coin)cryptoChart.crypto;
        String coinString = coin.getCoinGeckoID();

        String priceDataCoinJSON = WebUtil.get("https://api.coingecko.com/api/v3/coins/" + coinString + "/market_chart?vs_currency=" + priceFiatName + "&days=1&interval=hourly");

        ArrayList<PricePoint> pricePointArrayList = new ArrayList<>();

        if(priceDataCoinJSON != null) {
            try {
                JSONObject json = new JSONObject(priceDataCoinJSON);
                JSONArray prices = json.getJSONArray("prices");
                for(int i = 0; i < prices.length(); i++) {
                    JSONArray price = prices.getJSONArray(i);

                    String timeString = price.getString(0);
                    Date date = new Date(new BigDecimal(timeString).longValue());

                    String priceString = price.getString(1);

                    pricePointArrayList.add(new PricePoint(new Timestamp(date), new BigDecimal(priceString)));
                }
            }
            catch(Exception e) {
                // Ignore error and return null.
                // Even though some entries were filled, something went wrong so we assume the data may be suspect.
                ThrowableUtil.processThrowable(e);
                return null;
            }
        }

        return pricePointArrayList;
    }

    public ArrayList<Candle> getCandles(CryptoChart cryptoChart) {
        Fiat priceFiat = FiatManager.getDefaultFiatManager().getHardcodedFiat("USD");
        String priceFiatName = priceFiat.getCoinGeckoID();

        Coin coin = (Coin)cryptoChart.crypto;
        String coinString = coin.getCoinGeckoID();

        String priceDataCoinJSON = WebUtil.get("https://api.coingecko.com/api/v3/coins/" + coinString + "/ohlc?vs_currency=" + priceFiatName + "&days=1");

        ArrayList<Candle> candleArrayList = new ArrayList<>();

        if(priceDataCoinJSON != null) {
            try {
                JSONArray candles = new JSONArray(priceDataCoinJSON);
                for(int i = 0; i < candles.length(); i++) {
                    JSONArray candle = candles.getJSONArray(i);

                    String timeString = candle.getString(0);
                    Date date = new Date(new BigDecimal(timeString).longValue());

                    String openPriceString = candle.getString(1);
                    String highPriceString = candle.getString(2);
                    String lowPriceString = candle.getString(3);
                    String closePriceString = candle.getString(4);


                    candleArrayList.add(new Candle(new Timestamp(date), new BigDecimal(openPriceString), new BigDecimal(highPriceString), new BigDecimal(lowPriceString), new BigDecimal(closePriceString)));
                }
            }
            catch(Exception e) {
                // Ignore error and return null.
                // Even though some entries were filled, something went wrong so we assume the data may be suspect.
                ThrowableUtil.processThrowable(e);
                return null;
            }
        }

        return candleArrayList;
    }
}

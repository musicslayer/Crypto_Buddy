package com.musicslayer.cryptobuddy.api.chart;

import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.asset.fiat.Fiat;
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
        Fiat priceFiat = cryptoChart.fiat;
        String priceFiatName = priceFiat.getCoinGeckoID();

        Coin coin = (Coin)cryptoChart.crypto;
        String coinString = coin.getCoinGeckoID();

        ArrayList<PricePoint> pricePointArrayList = new ArrayList<>();

        // 24H
        String priceDataCoin24JSON = WebUtil.get("https://api.coingecko.com/api/v3/coins/" + coinString + "/market_chart?vs_currency=" + priceFiatName + "&days=1&interval=hourly");

        if(priceDataCoin24JSON != null) {
            try {
                // Prices, Market Caps, and Volumes all have the same times and are in corresponding order.
                JSONObject json = new JSONObject(priceDataCoin24JSON);
                JSONArray prices = json.getJSONArray("prices");
                JSONArray marketCaps = json.getJSONArray("market_caps");
                JSONArray volumes = json.getJSONArray("total_volumes");
                for(int i = 0; i < prices.length(); i++) {
                    JSONArray price = prices.getJSONArray(i);
                    JSONArray marketCap = marketCaps.getJSONArray(i);
                    JSONArray volume = volumes.getJSONArray(i);

                    // All times match, so just take first one.
                    String timeString = price.getString(0);
                    Date date = new Date(new BigDecimal(timeString).longValue());

                    String priceString = price.getString(1);
                    String marketCapString = marketCap.getString(1);
                    String volumeString = volume.getString(1);

                    pricePointArrayList.add(new PricePoint("24H", new Timestamp(date), new BigDecimal(priceString), new BigDecimal(marketCapString), new BigDecimal(volumeString)));
                }
            }
            catch(Exception e) {
                // Ignore error and return null.
                // Even though some entries were filled, something went wrong so we assume the data may be suspect.
                ThrowableUtil.processThrowable(e);
                return null;
            }
        }

        // 30D
        String priceDataCoin30JSON = WebUtil.get("https://api.coingecko.com/api/v3/coins/" + coinString + "/market_chart?vs_currency=" + priceFiatName + "&days=30&interval=daily");

        if(priceDataCoin30JSON != null) {
            try {
                JSONObject json = new JSONObject(priceDataCoin30JSON);
                JSONArray prices = json.getJSONArray("prices");
                JSONArray marketCaps = json.getJSONArray("market_caps");
                JSONArray volumes = json.getJSONArray("total_volumes");
                for(int i = 0; i < prices.length(); i++) {
                    JSONArray price = prices.getJSONArray(i);
                    JSONArray marketCap = marketCaps.getJSONArray(i);
                    JSONArray volume = volumes.getJSONArray(i);

                    // All times match, so just take first one.
                    String timeString = price.getString(0);
                    Date date = new Date(new BigDecimal(timeString).longValue());

                    String priceString = price.getString(1);
                    String marketCapString = marketCap.getString(1);
                    String volumeString = volume.getString(1);

                    pricePointArrayList.add(new PricePoint("30D", new Timestamp(date), new BigDecimal(priceString), new BigDecimal(marketCapString), new BigDecimal(volumeString)));
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
        Fiat priceFiat = cryptoChart.fiat;
        String priceFiatName = priceFiat.getCoinGeckoID();

        Coin coin = (Coin)cryptoChart.crypto;
        String coinString = coin.getCoinGeckoID();

        ArrayList<Candle> candleArrayList = new ArrayList<>();

        // 24H
        String priceDataCoin24JSON = WebUtil.get("https://api.coingecko.com/api/v3/coins/" + coinString + "/ohlc?vs_currency=" + priceFiatName + "&days=1");

        if(priceDataCoin24JSON != null) {
            try {
                // For each hour, we get 2 candles, so we have to combine them.
                int numCombinedCandles = 2;
                JSONArray candles = new JSONArray(priceDataCoin24JSON);
                for(int i = 0; i < candles.length(); i += numCombinedCandles) {
                    ArrayList<Candle> tempCandleArrayList = new ArrayList<>();

                    for(int j = i; j < i + numCombinedCandles && j < candles.length(); j++) {
                        JSONArray candle = candles.getJSONArray(j);

                        String timeString = candle.getString(0);
                        Date date = new Date(new BigDecimal(timeString).longValue());

                        String openPriceString = candle.getString(1);
                        String highPriceString = candle.getString(2);
                        String lowPriceString = candle.getString(3);
                        String closePriceString = candle.getString(4);

                        tempCandleArrayList.add(new Candle("24H", new Timestamp(date), new BigDecimal(openPriceString), new BigDecimal(highPriceString), new BigDecimal(lowPriceString), new BigDecimal(closePriceString)));
                    }

                    candleArrayList.add(Candle.combine(tempCandleArrayList));
                }
            }
            catch(Exception e) {
                // Ignore error and return null.
                // Even though some entries were filled, something went wrong so we assume the data may be suspect.
                ThrowableUtil.processThrowable(e);
                return null;
            }
        }

        // 30D
        String priceDataCoin30JSON = WebUtil.get("https://api.coingecko.com/api/v3/coins/" + coinString + "/ohlc?vs_currency=" + priceFiatName + "&days=30");

        if(priceDataCoin30JSON != null) {
            try {
                // For each day, we get 6 candles, so we have to combine them.
                int numCombinedCandles = 6;
                JSONArray candles = new JSONArray(priceDataCoin30JSON);
                for(int i = 0; i < candles.length(); i += numCombinedCandles) {
                    ArrayList<Candle> tempCandleArrayList = new ArrayList<>();

                    for(int j = i; j < i + numCombinedCandles && j < candles.length(); j++) {
                        JSONArray candle = candles.getJSONArray(j);

                        String timeString = candle.getString(0);
                        Date date = new Date(new BigDecimal(timeString).longValue());

                        String openPriceString = candle.getString(1);
                        String highPriceString = candle.getString(2);
                        String lowPriceString = candle.getString(3);
                        String closePriceString = candle.getString(4);

                        tempCandleArrayList.add(new Candle("30D", new Timestamp(date), new BigDecimal(openPriceString), new BigDecimal(highPriceString), new BigDecimal(lowPriceString), new BigDecimal(closePriceString)));
                    }

                    candleArrayList.add(Candle.combine(tempCandleArrayList));
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

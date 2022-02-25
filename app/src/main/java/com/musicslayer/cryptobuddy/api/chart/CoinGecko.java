package com.musicslayer.cryptobuddy.api.chart;

import com.musicslayer.cryptobuddy.transaction.AssetQuantity;

import java.util.ArrayList;

public class CoinGecko extends ChartAPI {
    public String getName() { return "CoinGecko"; }
    public String getDisplayName() { return "CoinGecko API V3"; }

    public boolean isSupported(CryptoChart cryptoChart) {
        // Right now, everything is supported.
        return true;
    }

    public ArrayList<AssetQuantity> getPricePoints(CryptoChart cryptoChart) {
        return null;
        /*
        Fiat priceFiat = cryptoPrice.fiat;
        String priceFiatName = priceFiat.getCoinGeckoID();

        // Separate assetArrayList into fiat, coins, and tokens.
        // Further separate tokens by blockchain.
        ArrayList<Fiat> fiatArrayList = new ArrayList<>();
        ArrayList<Coin> coinArrayList = new ArrayList<>();
        HashMap<String, ArrayList<Token>> blockchainHashMap = new HashMap<>();

        for(Asset asset : cryptoPrice.assetArrayList) {
            if(asset instanceof Fiat) {
                // All Fiats can be looked up.
                Fiat fiat = (Fiat)asset;
                if(!"?".equals(fiat.getCoinGeckoID())) {
                    fiatArrayList.add(fiat);
                }
            }
            else if(asset instanceof Coin) {
                Coin coin = (Coin)asset;
                if(!"?".equals(coin.getCoinGeckoID())) {
                    coinArrayList.add(coin);
                }
            }
            else if(asset instanceof Token) {
                Token token = (Token)asset;

                if(!"?".equals(token.getCoinGeckoID())) {
                    ArrayList<Token> tokenArrayList = blockchainHashMap.get(token.getCoinGeckoBlockchainID());
                    if(tokenArrayList == null) {
                        tokenArrayList = new ArrayList<>();
                    }

                    tokenArrayList.add(token);
                    blockchainHashMap.put(token.getCoinGeckoBlockchainID(), tokenArrayList);
                }
            }
        }

        HashMap<Asset, AssetQuantity> priceHashMap = new HashMap<>();

        // Tokens
        for(String blockchainID : blockchainHashMap.keySet()) {
            ArrayList<Token> tokenArrayList = blockchainHashMap.get(blockchainID);
            if(tokenArrayList == null) { continue; }

            if(!tokenArrayList.isEmpty()) {
                StringBuilder tokenString = new StringBuilder();
                for(int i = 0; i < tokenArrayList.size(); i++) {
                    tokenString.append(tokenArrayList.get(i).getCoinGeckoID());
                    if(i < tokenArrayList.size() - 1) {
                        tokenString.append(",");
                    }
                }

                ProgressDialogFragment.updateProgressSubtitle("Processing Tokens...");
                String priceDataTokenJSON = WebUtil.get("https://api.coingecko.com/api/v3/simple/token_price/" + blockchainID + "?contract_addresses=" + tokenString + "&vs_currencies=" + priceFiatName + "&include_market_cap=false&include_last_updated_at=true");
                if(priceDataTokenJSON != null) {
                    try {
                        JSONObject json = new JSONObject(priceDataTokenJSON);

                        for(Token token : tokenArrayList) {
                            if(!json.has(token.getCoinGeckoID())) { continue; }

                            JSONObject json2 = json.getJSONObject(token.getCoinGeckoID());
                            BigDecimal d = new BigDecimal(json2.getString(priceFiatName));
                            priceHashMap.put(token, new AssetQuantity(d.toPlainString(), priceFiat));
                        }
                    }
                    catch(Exception e) {
                        // Ignore error and return null.
                        // Even though some entries were filled, something went wrong so we assume the data may be suspect.
                        ThrowableUtil.processThrowable(e);
                        return null;
                    }
                }
            }
        }

        // Coins
        if(!coinArrayList.isEmpty()) {
            StringBuilder coinString = new StringBuilder();
            for(int i = 0; i < coinArrayList.size(); i++) {
                coinString.append(coinArrayList.get(i).getCoinGeckoID());
                if(i < coinArrayList.size() - 1) {
                    coinString.append(",");
                }
            }

            ProgressDialogFragment.updateProgressSubtitle("Processing Coins...");
            String priceDataCoinJSON = WebUtil.get("https://api.coingecko.com/api/v3/simple/price?ids=" + coinString + "&vs_currencies=" + priceFiatName + "&include_market_cap=false&include_last_updated_at=true");
            if(priceDataCoinJSON != null) {
                try {
                    JSONObject json = new JSONObject(priceDataCoinJSON);

                    for(Coin coin : coinArrayList) {
                        if(!json.has(coin.getCoinGeckoID())) { continue; }

                        JSONObject json2 = json.getJSONObject(coin.getCoinGeckoID());
                        BigDecimal d = new BigDecimal(json2.getString(priceFiatName));
                        priceHashMap.put(coin, new AssetQuantity(d.toPlainString(), priceFiat));
                    }
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

         */
    }

    public ArrayList<AssetQuantity> getCandles(CryptoChart cryptoChart) {
        return null;
    }
}

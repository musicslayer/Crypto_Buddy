package com.musicslayer.cryptobuddy.api.price;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.asset.crypto.token.Token;
import com.musicslayer.cryptobuddy.asset.fiat.USD;
import com.musicslayer.cryptobuddy.dialog.ProgressDialogFragment;
import com.musicslayer.cryptobuddy.transaction.AssetQuantity;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;
import com.musicslayer.cryptobuddy.util.WebUtil;

import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;

// CoinGecko doesn't give prices for these tokens:
// ADA
// ALGO
// ATOM
// BNBc - BEP2 ??
// BNBc - BEP8 ??
// CLO - ERC20
// KAVA
// XRP

// ADA - Not sure what format is exactly...
/*
  {
    "id": "yayswap",
    "symbol": "yay",
    "name": "YaySwap",
    "platforms": {
      "cardano": "57684adcb032c8dbc40179841bed987d8dee7472617a0e5c25ef4140.59617953776170"
    }
  },

    {
    "id": "adax",
    "symbol": "adax",
    "name": "ADAX",
    "platforms": {
      "cardano": "0c78f619e54a5d00e143f66181a2c500d0c394b38a10e86cd1a23c5f41444158"
    }
  },
  */


public class CoinGecko extends PriceAPI {
    public String getName() { return "CoinGecko"; }
    public String getDisplayName() { return "CoinGecko API V3"; }

    public boolean isSupported(Crypto crypto) {
        // Right now, everything is supported.
        return true;
    }

    public HashMap<Crypto, AssetQuantity> getBulkPrice(ArrayList<Crypto> cryptoArrayList) {
        // Separate cryptoArrayList into coins and tokens.
        // Further separate tokens by blockchain.
        HashMap<String, ArrayList<Crypto>> blockchainHashMap = new HashMap<>();
        ArrayList<Crypto> coinArrayList = new ArrayList<>();
        for(Crypto crypto : cryptoArrayList) {
            if(crypto instanceof Coin) {
                coinArrayList.add(crypto);
            }
            else if(crypto instanceof Token && !"?".equals(crypto.getID())) {
                Token token = (Token)crypto;

                ArrayList<Crypto> tokenArrayList = blockchainHashMap.get(token.getBlockchainID());
                if(tokenArrayList == null) {
                    tokenArrayList = new ArrayList<>();
                }

                tokenArrayList.add(crypto);
                blockchainHashMap.put(token.getBlockchainID(), tokenArrayList);
            }
        }

        HashMap<Crypto, AssetQuantity> priceHashMap = new HashMap<>();

        // Tokens
        for(String blockchainID : blockchainHashMap.keySet()) {
            ArrayList<Crypto> tokenArrayList = blockchainHashMap.get(blockchainID);
            if(tokenArrayList == null) { continue; }

            StringBuilder tokenString = new StringBuilder();
            for(int i = 0; i < tokenArrayList.size(); i++) {
                tokenString.append(tokenArrayList.get(i).getID());
                if(i < tokenArrayList.size() - 1) {
                    tokenString.append(",");
                }
            }

            ProgressDialogFragment.updateProgressSubtitle("Processing Tokens...");
            String priceDataTokenJSON = WebUtil.get("https://api.coingecko.com/api/v3/simple/token_price/" + blockchainID + "?contract_addresses=" + tokenString + "&vs_currencies=usd&include_market_cap=true&include_last_updated_at=true");
            if(priceDataTokenJSON != null) {
                try {
                    JSONObject json = new JSONObject(priceDataTokenJSON);

                    for(Crypto token : tokenArrayList) {
                        if(!json.has(token.getID())) { continue; }

                        JSONObject json2 = json.getJSONObject(token.getID());

                        BigDecimal d = new BigDecimal(json2.getString("usd"));

                        // For now, just use USD.
                        priceHashMap.put(token, new AssetQuantity(d.toPlainString(), new USD()));
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

        // Coins
        StringBuilder coinString = new StringBuilder();
        for(int i = 0; i < coinArrayList.size(); i++) {
            coinString.append(coinArrayList.get(i).getID());
            if(i < coinArrayList.size() - 1) {
                coinString.append(",");
            }
        }

        ProgressDialogFragment.updateProgressSubtitle("Processing Coins...");
        String priceDataCoinJSON = WebUtil.get("https://api.coingecko.com/api/v3/simple/price?ids=" + coinString.toString() + "&vs_currencies=usd&include_market_cap=true&include_last_updated_at=true");
        if(priceDataCoinJSON != null) {
            try {
                JSONObject json = new JSONObject(priceDataCoinJSON);

                for(Crypto coin : coinArrayList) {
                    if(!json.has(coin.getID())) { continue; }

                    JSONObject json2 = json.getJSONObject(coin.getID());

                    BigDecimal d = new BigDecimal(json2.getString("usd"));

                    // For now, just use USD.
                    priceHashMap.put(coin, new AssetQuantity(d.toPlainString(), new USD()));
                }
            }
            catch(Exception e) {
                // Ignore error and return null.
                // Even though some entries were filled, something went wrong so we assume the data may be suspect.
                ThrowableUtil.processThrowable(e);
                return null;
            }
        }

        return priceHashMap;
    }

    public AssetQuantity getPrice(Crypto crypto) {
        AssetQuantity price = null;

        String priceDataJSON = null;
        if(crypto instanceof Token && !"?".equals(crypto.getID())) {
            priceDataJSON = WebUtil.get("https://api.coingecko.com/api/v3/simple/token_price/" + ((Token)crypto).getBlockchainID() + "?contract_addresses=" + crypto.getID() + "&vs_currencies=usd&include_market_cap=true&include_last_updated_at=true");
        }
        else if(crypto instanceof Coin) {
            priceDataJSON = WebUtil.get("https://api.coingecko.com/api/v3/simple/price?ids=" + crypto.getID() + "&vs_currencies=usd&include_market_cap=true&include_last_updated_at=true");
        }

        if(priceDataJSON != null) {
            try {
                JSONObject json = new JSONObject(priceDataJSON);
                if(json.has(crypto.getID())) {
                    JSONObject json2 = json.getJSONObject(crypto.getID());

                    BigDecimal d = new BigDecimal(json2.getString("usd"));

                    // For now, just use USD.
                    price = new AssetQuantity(d.toPlainString(), new USD());
                }
            }
            catch(Exception e) {
                // Ignore error and return null.
                ThrowableUtil.processThrowable(e);
                return null;
            }
        }

        return price;
    }

    public HashMap<Crypto, AssetQuantity> getBulkMarketCap(ArrayList<Crypto> cryptoArrayList) {
        // Separate cryptoArrayList into coins and tokens.
        // Further separate tokens by blockchain.
        HashMap<String, ArrayList<Crypto>> blockchainHashMap = new HashMap<>();
        ArrayList<Crypto> coinArrayList = new ArrayList<>();
        for(Crypto crypto : cryptoArrayList) {
            if(crypto instanceof Coin) {
                coinArrayList.add(crypto);
            }
            else if(crypto instanceof Token && !"?".equals(crypto.getID())) {
                Token token = (Token)crypto;

                ArrayList<Crypto> tokenArrayList = blockchainHashMap.get(token.getBlockchainID());
                if(tokenArrayList == null) {
                    tokenArrayList = new ArrayList<>();
                }

                tokenArrayList.add(crypto);
                blockchainHashMap.put(token.getBlockchainID(), tokenArrayList);
            }
        }

        HashMap<Crypto, AssetQuantity> priceHashMap = new HashMap<>();

        // Tokens
        for(String blockchainID : blockchainHashMap.keySet()) {
            ArrayList<Crypto> tokenArrayList = blockchainHashMap.get(blockchainID);
            if(tokenArrayList == null) { continue; }

            StringBuilder tokenString = new StringBuilder();
            for(int i = 0; i < tokenArrayList.size(); i++) {
                tokenString.append(tokenArrayList.get(i).getID());
                if(i < tokenArrayList.size() - 1) {
                    tokenString.append(",");
                }
            }

            String priceDataTokenJSON = WebUtil.get("https://api.coingecko.com/api/v3/simple/token_price/" + blockchainID + "?contract_addresses=" + tokenString + "&vs_currencies=usd&include_market_cap=true&include_last_updated_at=true");
            if(priceDataTokenJSON != null) {
                try {
                    JSONObject json = new JSONObject(priceDataTokenJSON);

                    for(Crypto token : tokenArrayList) {
                        if(!json.has(token.getID())) { continue; }

                        JSONObject json2 = json.getJSONObject(token.getID());

                        BigDecimal d = new BigDecimal(json2.getString("usd_market_cap"));

                        // For now, just use USD.
                        priceHashMap.put(token, new AssetQuantity(d.toPlainString(), new USD()));
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

        // Coins
        StringBuilder coinString = new StringBuilder();
        for(int i = 0; i < coinArrayList.size(); i++) {
            coinString.append(coinArrayList.get(i).getID());
            if(i < coinArrayList.size() - 1) {
                coinString.append(",");
            }
        }

        String priceDataCoinJSON = WebUtil.get("https://api.coingecko.com/api/v3/simple/price?ids=" + coinString.toString() + "&vs_currencies=usd&include_market_cap=true&include_last_updated_at=true");
        if(priceDataCoinJSON != null) {
            try {
                JSONObject json = new JSONObject(priceDataCoinJSON);

                for(Crypto coin : coinArrayList) {
                    if(!json.has(coin.getID())) { continue; }

                    JSONObject json2 = json.getJSONObject(coin.getID());

                    BigDecimal d = new BigDecimal(json2.getString("usd_market_cap"));

                    // For now, just use USD.
                    priceHashMap.put(coin, new AssetQuantity(d.toPlainString(), new USD()));
                }
            }
            catch(Exception e) {
                // Ignore error and return null.
                // Even though some entries were filled, something went wrong so we assume the data may be suspect.
                ThrowableUtil.processThrowable(e);
                return null;
            }
        }

        return priceHashMap;
    }

    public AssetQuantity getMarketCap(Crypto crypto) {
        AssetQuantity marketCap = null;

        String priceDataJSON = null;

        if(crypto instanceof Token && !"?".equals(crypto.getID())) {
            priceDataJSON = WebUtil.get("https://api.coingecko.com/api/v3/simple/token_price/" + ((Token)crypto).getBlockchainID() + "?contract_addresses=" + crypto.getID() + "&vs_currencies=usd&include_market_cap=true&include_last_updated_at=true");
        }
        else if(crypto instanceof Coin) {
            priceDataJSON = WebUtil.get("https://api.coingecko.com/api/v3/simple/price?ids=" + crypto.getID() + "&vs_currencies=usd&include_market_cap=true&include_last_updated_at=true");
        }

        if(priceDataJSON != null) {
            try {
                JSONObject json = new JSONObject(priceDataJSON);
                if(json.has(crypto.getID())) {
                    JSONObject json2 = json.getJSONObject(crypto.getID());

                    BigDecimal d = new BigDecimal(json2.getString("usd_market_cap"));

                    // For now, just use USD.
                    marketCap = new AssetQuantity(d.toPlainString(), new USD());
                }
            }
            catch(Exception e) {
                // Ignore error and return null.
                ThrowableUtil.processThrowable(e);
                return null;
            }
        }

        return marketCap;
    }
}

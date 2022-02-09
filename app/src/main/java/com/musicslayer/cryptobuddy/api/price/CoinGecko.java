package com.musicslayer.cryptobuddy.api.price;

import com.musicslayer.cryptobuddy.asset.Asset;
import com.musicslayer.cryptobuddy.asset.coinmanager.CoinManager;
import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.asset.crypto.token.Token;
import com.musicslayer.cryptobuddy.asset.fiat.Fiat;
import com.musicslayer.cryptobuddy.dialog.ProgressDialogFragment;
import com.musicslayer.cryptobuddy.transaction.AssetPrice;
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

    public boolean isSupported(CryptoPrice cryptoPrice) {
        // Right now, everything is supported.
        return true;
    }

    public HashMap<Asset, AssetQuantity> getPrice(CryptoPrice cryptoPrice) {
        Fiat priceFiat = cryptoPrice.fiat;
        String priceFiatName = priceFiat.getID();

        // Separate assetArrayList into fiat, coins, and tokens.
        // Further separate tokens by blockchain.
        ArrayList<Fiat> fiatArrayList = new ArrayList<>();
        ArrayList<Coin> coinArrayList = new ArrayList<>();
        HashMap<String, ArrayList<Token>> blockchainHashMap = new HashMap<>();

        for(Asset asset : cryptoPrice.assetArrayList) {
            if(asset instanceof Fiat) {
                // All Fiats can be looked up.
                Fiat fiat = (Fiat)asset;
                fiatArrayList.add(fiat);
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

        // Fiats
        // The strategy is to get the price of Bitcoin in all our fiats, and then use that for the conversion.
        if(!fiatArrayList.isEmpty()) {
            StringBuilder fiatString = new StringBuilder();
            for(int i = 0; i < fiatArrayList.size(); i++) {
                fiatString.append(fiatArrayList.get(i).getID());
                fiatString.append(",");
            }
            fiatString.append(priceFiatName);

            Crypto conversionCrypto = CoinManager.getDefaultCoinManager().getHardcodedCoin("BTC");

            ProgressDialogFragment.updateProgressSubtitle("Processing Fiats...");
            String priceDataFiatJSON = WebUtil.get("https://api.coingecko.com/api/v3/simple/price?ids=" + conversionCrypto.getCoinGeckoID() + "&vs_currencies=" + fiatString + "&include_market_cap=false&include_last_updated_at=true");
            if(priceDataFiatJSON != null) {
                try {
                    JSONObject json = new JSONObject(priceDataFiatJSON);
                    JSONObject json2 = json.getJSONObject(conversionCrypto.getCoinGeckoID());

                    BigDecimal dPrice = new BigDecimal(json2.getString(priceFiatName));

                    for(Fiat fiat : fiatArrayList) {
                        BigDecimal dFiat = new BigDecimal(json2.getString(fiat.getID()));

                        AssetQuantity fiatAssetQuantity = new AssetQuantity("1", fiat);
                        AssetPrice fiatAssetPrice = new AssetPrice(new AssetQuantity(dFiat.toPlainString(), fiat), new AssetQuantity("1", conversionCrypto));
                        AssetPrice priceAssetPrice = new AssetPrice(new AssetQuantity(dPrice.toPlainString(), priceFiat), new AssetQuantity("1", conversionCrypto));
                        AssetQuantity priceAssetQuantity = fiatAssetQuantity.convert(fiatAssetPrice).convert(priceAssetPrice.reverseAssetPrice());

                        priceHashMap.put(fiat, priceAssetQuantity);
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
    }

    public HashMap<Asset, AssetQuantity> getMarketCap(CryptoPrice cryptoPrice) {
        // Note that only cryptos have a market cap. This should never be called with fiats.
        Fiat priceFiat = cryptoPrice.fiat;
        String priceFiatName = priceFiat.getID();

        // Separate assetArrayList into coins and tokens (there should be no fiats).
        // Further separate tokens by blockchain.
        ArrayList<Coin> coinArrayList = new ArrayList<>();
        HashMap<String, ArrayList<Token>> blockchainHashMap = new HashMap<>();
        for(Asset asset : cryptoPrice.assetArrayList) {
            if(asset instanceof Coin) {
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
                String priceDataTokenJSON = WebUtil.get("https://api.coingecko.com/api/v3/simple/token_price/" + blockchainID + "?contract_addresses=" + tokenString + "&vs_currencies=" + priceFiatName + "&include_market_cap=true&include_last_updated_at=true");
                if(priceDataTokenJSON != null) {
                    try {
                        JSONObject json = new JSONObject(priceDataTokenJSON);

                        for(Token token : tokenArrayList) {
                            if(!json.has(token.getCoinGeckoID())) { continue; }

                            JSONObject json2 = json.getJSONObject(token.getCoinGeckoID());
                            BigDecimal d = new BigDecimal(json2.getString(priceFiatName + "_market_cap"));
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
            String priceDataCoinJSON = WebUtil.get("https://api.coingecko.com/api/v3/simple/price?ids=" + coinString + "&vs_currencies=" + priceFiatName + "&include_market_cap=true&include_last_updated_at=true");
            if(priceDataCoinJSON != null) {
                try {
                    JSONObject json = new JSONObject(priceDataCoinJSON);

                    for(Coin coin : coinArrayList) {
                        if(!json.has(coin.getCoinGeckoID())) { continue; }

                        JSONObject json2 = json.getJSONObject(coin.getCoinGeckoID());
                        BigDecimal d = new BigDecimal(json2.getString(priceFiatName + "_market_cap"));
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
    }
}

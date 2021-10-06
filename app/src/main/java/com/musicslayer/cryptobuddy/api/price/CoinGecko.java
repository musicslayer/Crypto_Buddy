package com.musicslayer.cryptobuddy.api.price;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.asset.crypto.token.Token;
import com.musicslayer.cryptobuddy.asset.fiat.USD;
import com.musicslayer.cryptobuddy.transaction.AssetQuantity;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;
import com.musicslayer.cryptobuddy.util.RESTUtil;

import org.json.JSONObject;

import java.math.BigDecimal;

// TODO -> CoinGecko doesn't give prices for:
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


// TODO we can get multiple prices at once...
//https://api.coingecko.com/api/v3/simple/price?ids=bitcoin,dogecoin&vs_currencies=usd&include_market_cap=true&include_last_updated_at=true

public class CoinGecko extends PriceAPI {
    public String getName() { return "CoinGecko"; }
    public String getDisplayName() { return "CoinGecko API V3"; }

    public boolean isSupported(Crypto crypto) {
        // Right now, everything is supported.
        return true;
    }

    public AssetQuantity getPrice(Crypto crypto) {
        AssetQuantity price = null;

        String priceDataJSON = null;
        if(crypto instanceof Token && !"?".equals(crypto.getID())) {
            priceDataJSON = RESTUtil.get("https://api.coingecko.com/api/v3/simple/token_price/" + ((Token)crypto).getBlockchainID() + "?contract_addresses=" + crypto.getID() + "&vs_currencies=usd&include_market_cap=true&include_last_updated_at=true");
        }
        else if(crypto instanceof Coin) {
            priceDataJSON = RESTUtil.get("https://api.coingecko.com/api/v3/simple/price?ids=" + crypto.getID() + "&vs_currencies=usd&include_market_cap=true&include_last_updated_at=true");
        }

        if(priceDataJSON != null) {
            try {
                JSONObject json = new JSONObject(priceDataJSON);
                JSONObject json2 = json.getJSONObject(crypto.getID());

                BigDecimal d = new BigDecimal(json2.getString("usd"));

                // For now, just use USD.
                price = new AssetQuantity(d.toPlainString(), new USD());
            }
            catch(Exception e) {
                // This may be ignorable. This happens if the website can't lookup the price of something.
                ThrowableUtil.processThrowable(e);
            }
        }

        return price;
    }

    public AssetQuantity getMarketCap(Crypto crypto) {
        AssetQuantity marketCap = null;

        String priceDataJSON = null;

        if(crypto instanceof Token && !"?".equals(crypto.getID())) {
            priceDataJSON = RESTUtil.get("https://api.coingecko.com/api/v3/simple/token_price/" + ((Token)crypto).getBlockchainID() + "?contract_addresses=" + crypto.getID() + "&vs_currencies=usd&include_market_cap=true&include_last_updated_at=true");
        }
        else if(crypto instanceof Coin) {
            priceDataJSON = RESTUtil.get("https://api.coingecko.com/api/v3/simple/price?ids=" + crypto.getID() + "&vs_currencies=usd&include_market_cap=true&include_last_updated_at=true");
        }

        if(priceDataJSON != null) {
            try {
                JSONObject json = new JSONObject(priceDataJSON);
                JSONObject json2 = json.getJSONObject(crypto.getID());

                BigDecimal d = new BigDecimal(json2.getString("usd_market_cap"));

                // For now, just use USD.
                marketCap = new AssetQuantity(d.toPlainString(), new USD());
            }
            catch(Exception e) {
                // This may be ignorable. This happens if the website can't lookup the market cap of something.
                ThrowableUtil.processThrowable(e);
            }
        }

        return marketCap;
    }
}

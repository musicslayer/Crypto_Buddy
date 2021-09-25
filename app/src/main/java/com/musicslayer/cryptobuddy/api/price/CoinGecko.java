package com.musicslayer.cryptobuddy.api.price;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.asset.crypto.token.Token;
import com.musicslayer.cryptobuddy.util.Exception;
import com.musicslayer.cryptobuddy.util.REST;

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

    public String getUSDPrice(Crypto crypto) {
        String usdPrice = null;

        String priceDataJSON = null;

        if(crypto instanceof Token && !"?".equals(crypto.getID())) {
            priceDataJSON = REST.get("https://api.coingecko.com/api/v3/simple/token_price/" + ((Token)crypto).getBlockchainID() + "?contract_addresses=" + crypto.getID() + "&vs_currencies=usd&include_market_cap=true&include_last_updated_at=true");
        }
        else if(crypto instanceof Coin) {
            priceDataJSON = REST.get("https://api.coingecko.com/api/v3/simple/price?ids=" + crypto.getID() + "&vs_currencies=usd&include_market_cap=true&include_last_updated_at=true");
        }

        if(priceDataJSON != null) {
            try {
                JSONObject json = new JSONObject(priceDataJSON);
                JSONObject json2 = json.getJSONObject(crypto.getID());

                usdPrice = json2.getString("usd");
            }
            catch(java.lang.Exception e) {
                Exception.processException(e);
            }
        }

        return usdPrice;
    }

    public String getUSDMarketCap(Crypto crypto) {
        String usdMarketCap = null;

        String priceDataJSON = null;

        if(crypto instanceof Token && !"?".equals(crypto.getID())) {
            priceDataJSON = REST.get("https://api.coingecko.com/api/v3/simple/token_price/" + ((Token)crypto).getBlockchainID() + "?contract_addresses=" + crypto.getID() + "&vs_currencies=usd&include_market_cap=true&include_last_updated_at=true");
        }
        else if(crypto instanceof Coin) {
            priceDataJSON = REST.get("https://api.coingecko.com/api/v3/simple/price?ids=" + crypto.getID() + "&vs_currencies=usd&include_market_cap=true&include_last_updated_at=true");
        }

        if(priceDataJSON != null) {
            try {
                JSONObject json = new JSONObject(priceDataJSON);
                JSONObject json2 = json.getJSONObject(crypto.getID());

                BigDecimal cap = new BigDecimal(json2.getString("usd_market_cap"));
                usdMarketCap = cap.toPlainString();
            }
            catch(java.lang.Exception e) {
                Exception.processException(e);
            }
        }

        return usdMarketCap;
    }
}

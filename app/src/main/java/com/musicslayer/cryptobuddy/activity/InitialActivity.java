package com.musicslayer.cryptobuddy.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.musicslayer.cryptobuddy.api.address.AddressAPI;
import com.musicslayer.cryptobuddy.api.exchange.ExchangeAPI;
import com.musicslayer.cryptobuddy.api.price.PriceAPI;
import com.musicslayer.cryptobuddy.app.App;
import com.musicslayer.cryptobuddy.asset.coinmanager.CoinManager;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.asset.exchange.Exchange;
import com.musicslayer.cryptobuddy.asset.fiat.Fiat;
import com.musicslayer.cryptobuddy.asset.fiatmanager.FiatManager;
import com.musicslayer.cryptobuddy.asset.network.Network;
import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;
import com.musicslayer.cryptobuddy.i18n.TimeZoneManager;
import com.musicslayer.cryptobuddy.monetization.InAppPurchase;
import com.musicslayer.cryptobuddy.persistence.AddressHistory;
import com.musicslayer.cryptobuddy.persistence.AddressPortfolio;
import com.musicslayer.cryptobuddy.persistence.ExchangePortfolio;
import com.musicslayer.cryptobuddy.persistence.Policy;
import com.musicslayer.cryptobuddy.persistence.Purchases;
import com.musicslayer.cryptobuddy.persistence.Review;
import com.musicslayer.cryptobuddy.persistence.TokenManagerList;
import com.musicslayer.cryptobuddy.persistence.TransactionPortfolio;
import com.musicslayer.cryptobuddy.settings.category.SettingsCategory;
import com.musicslayer.cryptobuddy.settings.setting.Setting;
import com.musicslayer.cryptobuddy.util.ToastUtil;

import java.util.Date;

// TODO Long Term Items
//  Data Import/Export
//  NFT Viewer
//  Use TradeView API
//  "Tax" View (i.e. Calculate cost basis of transactions)
//  Create collection of bridges (and separate classes) to get access to those transactions (for example: MATIC Proof of Stake Bridge).
//  Chart Explorer, Chart Portfolio

// TODO Actually implement Coinbase/Gemini API.
// TODO Merge isLoss with BigDecimal math.
// TODO Finish the getSingleAllData Implementations.

// TODO Hardcoded coins, should we allow found/custom to be used instead?

// This Activity class only exists for initialization code, not to be seen by the user.
// Unlike App.java, this class can show CrashReporterDialog if there is a problem.
public class InitialActivity extends BaseActivity {
    @Override
    public int getAdLayoutViewID() {
        return -1;
    }

    @Override
    public void createLayout(Bundle savedInstanceState) {
        // Don't actually show anything. Just do initialization code and then launch MainActivity.
        Context applicationContext = getApplicationContext();

        // Set time zone base date.
        TimeZoneManager.nowInstant = new Date().toInstant();

        // Purchases should be initialized first, as others may depend on this.
        Purchases.loadAllPurchases(applicationContext);

        FiatManager.initialize(applicationContext);
        CoinManager.initialize(applicationContext);
        TokenManager.initialize(applicationContext);
        Exchange.initialize(applicationContext);
        Network.initialize(applicationContext); // Requires CoinManagers for display names.
        AddressAPI.initialize(applicationContext);
        ExchangeAPI.initialize(applicationContext);
        PriceAPI.initialize(applicationContext);
        InAppPurchase.initialize(applicationContext); // Requires Purchases
        SettingsCategory.initialize(applicationContext);
        ToastUtil.loadAllToasts(applicationContext);
        AddressHistory.loadAllData(applicationContext);
        AddressPortfolio.loadAllData(applicationContext);
        ExchangePortfolio.loadAllData(applicationContext);
        TransactionPortfolio.loadAllData(applicationContext);
        Policy.loadAllData(applicationContext);
        Review.loadAllData(applicationContext);

        // Settings should be initialized last, as this could theoretically depend on anything.
        Setting.initialize(applicationContext);

        App.isAppInitialized = true;

        // TODO FIX
        /*
        // Initialize or override the hardcoded assets here.
        FiatManager fiatManager = FiatManager.getDefaultFiatManager();
        fiatManager.resetHardcodedFiats();
        fiatManager.addHardcodedFiat(Fiat.fiats);

        CoinManager coinManager = CoinManager.getDefaultCoinManager();
        coinManager.resetHardcodedCoins();
        coinManager.addHardcodedCoin(Coin.coins);

         */

        // Initialize or override the hardcoded assets here.
        FiatManager fiatManager = FiatManager.getDefaultFiatManager();
        fiatManager.resetHardcodedFiats();
        fiatManager.addHardcodedFiat(new Fiat("AED", "AED", "United Arab Emirates Dirham", 2, fiatManager.getFiatType()));
        fiatManager.addHardcodedFiat(new Fiat("ARS", "ARS", "Argentine Peso", 2, fiatManager.getFiatType()));
        fiatManager.addHardcodedFiat(new Fiat("AUD", "AUD", "Australian Dollar", 2, fiatManager.getFiatType()));
        fiatManager.addHardcodedFiat(new Fiat("BDT", "BDT", "Bangladeshi Taka", 2, fiatManager.getFiatType()));
        fiatManager.addHardcodedFiat(new Fiat("BHD", "BHD", "Bahraini Dinar", 2, fiatManager.getFiatType()));
        fiatManager.addHardcodedFiat(new Fiat("BMD", "BMD", "Bermudan Dollar", 2, fiatManager.getFiatType()));
        fiatManager.addHardcodedFiat(new Fiat("BRL", "BRL", "Brazilian Real", 2, fiatManager.getFiatType()));
        fiatManager.addHardcodedFiat(new Fiat("CAD", "CAD", "Canadian Dollar", 2, fiatManager.getFiatType()));
        fiatManager.addHardcodedFiat(new Fiat("CHF", "CHF", "Swiss Franc", 2, fiatManager.getFiatType()));
        fiatManager.addHardcodedFiat(new Fiat("CLP", "CLP", "Chilean Peso", 2, fiatManager.getFiatType()));
        fiatManager.addHardcodedFiat(new Fiat("CNY", "CNY", "Chinese Yuan", 2, fiatManager.getFiatType()));
        fiatManager.addHardcodedFiat(new Fiat("CZK", "CZK", "Czech Koruna", 2, fiatManager.getFiatType()));
        fiatManager.addHardcodedFiat(new Fiat("DKK", "DKK", "Danish Krone", 2, fiatManager.getFiatType()));
        fiatManager.addHardcodedFiat(new Fiat("EUR", "EUR", "Euro", 2, fiatManager.getFiatType()));
        fiatManager.addHardcodedFiat(new Fiat("GBP", "GBP", "British Pound Sterling", 2, fiatManager.getFiatType()));
        fiatManager.addHardcodedFiat(new Fiat("HKD", "HKD", "Hong Kong Dollar", 2, fiatManager.getFiatType()));
        fiatManager.addHardcodedFiat(new Fiat("HUF", "HUF", "Hungarian Forint", 2, fiatManager.getFiatType()));
        fiatManager.addHardcodedFiat(new Fiat("IDR", "IDR", "Indonesian Rupiah", 2, fiatManager.getFiatType()));
        fiatManager.addHardcodedFiat(new Fiat("ILS", "ILS", "Israeli New Shekel", 2, fiatManager.getFiatType()));
        fiatManager.addHardcodedFiat(new Fiat("INR", "INR", "Indian Rupee", 2, fiatManager.getFiatType()));
        fiatManager.addHardcodedFiat(new Fiat("JPY", "JPY", "Japanese Yen", 2, fiatManager.getFiatType()));
        fiatManager.addHardcodedFiat(new Fiat("KRW", "KRW", "South Korean Won", 2, fiatManager.getFiatType()));
        fiatManager.addHardcodedFiat(new Fiat("KWD", "KWD", "Kuwaiti Dinar", 2, fiatManager.getFiatType()));
        fiatManager.addHardcodedFiat(new Fiat("LKR", "LKR", "Sri Lankan Rupee", 2, fiatManager.getFiatType()));
        fiatManager.addHardcodedFiat(new Fiat("MMK", "MMK", "Myanmar Kyat", 2, fiatManager.getFiatType()));
        fiatManager.addHardcodedFiat(new Fiat("MXN", "MXN", "Mexican Peso", 2, fiatManager.getFiatType()));
        fiatManager.addHardcodedFiat(new Fiat("MYR", "MYR", "Malaysian Ringgit", 2, fiatManager.getFiatType()));
        fiatManager.addHardcodedFiat(new Fiat("NGN", "NGN", "Nigerian Naira", 2, fiatManager.getFiatType()));
        fiatManager.addHardcodedFiat(new Fiat("NOK", "NOK", "Norwegian Krone", 2, fiatManager.getFiatType()));
        fiatManager.addHardcodedFiat(new Fiat("NZD", "NZD", "New Zealand Dollar", 2, fiatManager.getFiatType()));
        fiatManager.addHardcodedFiat(new Fiat("PHP", "PHP", "Philippine Peso", 2, fiatManager.getFiatType()));
        fiatManager.addHardcodedFiat(new Fiat("PKR", "PKR", "Pakistani Rupee", 2, fiatManager.getFiatType()));
        fiatManager.addHardcodedFiat(new Fiat("PLN", "PLN", "Polish Zloty", 2, fiatManager.getFiatType()));
        fiatManager.addHardcodedFiat(new Fiat("RUB", "RUB", "Russian Ruble", 2, fiatManager.getFiatType()));
        fiatManager.addHardcodedFiat(new Fiat("SAR", "SAR", "Saudi Arabian Riyal", 2, fiatManager.getFiatType()));
        fiatManager.addHardcodedFiat(new Fiat("SEK", "SEK", "Swedish Krona", 2, fiatManager.getFiatType()));
        fiatManager.addHardcodedFiat(new Fiat("SGD", "SGD", "Singapore Dollar", 2, fiatManager.getFiatType()));
        fiatManager.addHardcodedFiat(new Fiat("THB", "THB", "Thai Baht", 2, fiatManager.getFiatType()));
        fiatManager.addHardcodedFiat(new Fiat("TRY", "TRY", "Turkish Lira", 2, fiatManager.getFiatType()));
        fiatManager.addHardcodedFiat(new Fiat("TWD", "TWD", "New Taiwan Dollar", 2, fiatManager.getFiatType()));
        fiatManager.addHardcodedFiat(new Fiat("UAH", "UAH", "Ukrainian Hryvnia", 2, fiatManager.getFiatType()));
        fiatManager.addHardcodedFiat(new Fiat("USD", "USD", "United States Dollar", 2, fiatManager.getFiatType()));
        fiatManager.addHardcodedFiat(new Fiat("VEF", "VEF", "Venezuelan Bolívar", 2, fiatManager.getFiatType()));
        fiatManager.addHardcodedFiat(new Fiat("VND", "VND", "Vietnamese Dong", 2, fiatManager.getFiatType()));
        fiatManager.addHardcodedFiat(new Fiat("XAG", "XAG", "Silver Ounce", 2, fiatManager.getFiatType()));
        fiatManager.addHardcodedFiat(new Fiat("XAU", "XAU", "Gold Ounce", 2, fiatManager.getFiatType()));
        fiatManager.addHardcodedFiat(new Fiat("XDR", "XDR", "IMF Special Drawing Rights", 2, fiatManager.getFiatType()));
        fiatManager.addHardcodedFiat(new Fiat("ZAR", "ZAR", "South African Rand", 2, fiatManager.getFiatType()));

        CoinManager coinManager = CoinManager.getDefaultCoinManager();
        coinManager.resetHardcodedCoins();
        coinManager.addHardcodedCoin(new Coin("ADA", "ADA", "Cardano", 6, "cardano", coinManager.getCoinType()));
        coinManager.addHardcodedCoin(new Coin("ALGO", "ALGO", "Algorand", 6, "algorand", coinManager.getCoinType()));
        coinManager.addHardcodedCoin(new Coin("ATOM", "ATOM", "Cosmos", 6, "cosmos", coinManager.getCoinType()));
        coinManager.addHardcodedCoin(new Coin("BCH", "BCH", "Bitcoin Cash", 8, "bitcoin-cash", coinManager.getCoinType()));
        coinManager.addHardcodedCoin(new Coin("BNBc", "BNBc", "Binance Coin", 8, "binancecoin", coinManager.getCoinType()));
        coinManager.addHardcodedCoin(new Coin("BNBs", "BNBs", "Binance Coin (Smart Chain)", 18, "binancecoin", coinManager.getCoinType()));
        coinManager.addHardcodedCoin(new Coin("BTC", "BTC", "Bitcoin", 8, "bitcoin", coinManager.getCoinType()));
        coinManager.addHardcodedCoin(new Coin("CLO", "CLO", "Callisto", 18, "callisto", coinManager.getCoinType()));
        coinManager.addHardcodedCoin(new Coin("DASH", "DASH", "Dash", 8, "dash", coinManager.getCoinType()));
        coinManager.addHardcodedCoin(new Coin("DOGE", "DOGE", "Dogecoin", 8, "dogecoin", coinManager.getCoinType()));
        coinManager.addHardcodedCoin(new Coin("ETC", "ETC", "Ethereum Classic", 8, "ethereum-classic", coinManager.getCoinType()));
        coinManager.addHardcodedCoin(new Coin("ETH", "ETH", "Ethereum", 18, "ethereum", coinManager.getCoinType()));
        coinManager.addHardcodedCoin(new Coin("KAVA", "KAVA", "Kava", 6, "kava", coinManager.getCoinType()));
        coinManager.addHardcodedCoin(new Coin("LTC", "LTC", "Litecoin", 8, "litecoin", coinManager.getCoinType()));
        coinManager.addHardcodedCoin(new Coin("MATIC", "MATIC", "Polygon", 18, "matic-network", coinManager.getCoinType()));
        coinManager.addHardcodedCoin(new Coin("SOL", "SOL", "Solana", 9, "solana", coinManager.getCoinType()));
        coinManager.addHardcodedCoin(new Coin("TOMO", "TOMO", "TomoChain", 18, "tomochain", coinManager.getCoinType()));
        coinManager.addHardcodedCoin(new Coin("TRX", "TRX", "Tron", 6, "tron", coinManager.getCoinType()));
        coinManager.addHardcodedCoin(new Coin("VET", "VET", "VeChain", 18, "vechain", coinManager.getCoinType()));
        coinManager.addHardcodedCoin(new Coin("VTHO", "VTHO", "VeThor", 18, "vethor-token", coinManager.getCoinType()));
        coinManager.addHardcodedCoin(new Coin("WAVES", "WAVES", "Waves", 8, "waves", coinManager.getCoinType()));
        coinManager.addHardcodedCoin(new Coin("XLM", "XLM", "Stellar Lumens", 7, "stellar", coinManager.getCoinType()));
        coinManager.addHardcodedCoin(new Coin("XRP", "XRP", "XRP", 6, "ripple", coinManager.getCoinType()));
        coinManager.addHardcodedCoin(new Coin("XTZ", "XTZ", "Tezos", 6, "tezos", coinManager.getCoinType()));
        coinManager.addHardcodedCoin(new Coin("ZEC", "ZEC", "Zcash", 8, "zcash", coinManager.getCoinType()));

        // If the user has not purchased (or has refunded) "Unlock Tokens", we reset the token lists.
        if(!Purchases.isUnlockTokensPurchased()) {
            TokenManager.resetAllTokens();
            TokenManagerList.resetAllData(applicationContext);
        }

        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
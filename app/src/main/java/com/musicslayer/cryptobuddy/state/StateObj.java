package com.musicslayer.cryptobuddy.state;

import com.musicslayer.cryptobuddy.api.address.AddressData;
import com.musicslayer.cryptobuddy.api.address.CryptoAddress;
import com.musicslayer.cryptobuddy.api.exchange.CryptoExchange;
import com.musicslayer.cryptobuddy.api.exchange.ExchangeData;
import com.musicslayer.cryptobuddy.api.price.PriceData;
import com.musicslayer.cryptobuddy.asset.Asset;
import com.musicslayer.cryptobuddy.persistence.AddressPortfolioObj;
import com.musicslayer.cryptobuddy.persistence.ExchangePortfolioObj;
import com.musicslayer.cryptobuddy.persistence.TransactionPortfolioObj;
import com.musicslayer.cryptobuddy.transaction.Transaction;
import com.musicslayer.cryptobuddy.transaction.TransactionData;

import java.util.ArrayList;
import java.util.HashMap;

// This class stores data that would be to expensive to recalculate upon recreation.
// This should be reset at appropriate moments to prevent memory leaks.
public class StateObj {
    public static HashMap<CryptoAddress, AddressData> addressDataMap = new HashMap<>();
    public static HashMap<CryptoAddress, AddressData> addressDataFilterMap = new HashMap<>();

    public static HashMap<CryptoExchange, ExchangeData> exchangeDataMap = new HashMap<>();
    public static HashMap<CryptoExchange, ExchangeData> exchangeDataFilterMap = new HashMap<>();

    public static PriceData priceData;
    public static TransactionData transactionData;

    public static AddressPortfolioObj addressPortfolioObj;
    public static TransactionPortfolioObj transactionPortfolioObj;
    public static ExchangePortfolioObj exchangePortfolioObj;

    public static ArrayList<Asset> search_options_assets = new ArrayList<>();
    public static ArrayList<String> search_options_asset_names = new ArrayList<>();
    public static ArrayList<String> search_options_asset_display_names = new ArrayList<>();

    public static ArrayList<Transaction> transactionArrayList = new ArrayList<>();
    public static ArrayList<Transaction> filteredTransactionArrayList = new ArrayList<>();

    public static String tableInfo;
    public static String filterInfo;

    public static void resetState() {
        addressDataMap = new HashMap<>();
        addressDataFilterMap = new HashMap<>();
        exchangeDataMap = new HashMap<>();
        exchangeDataFilterMap = new HashMap<>();
        priceData = null;
        transactionData = null;
        addressPortfolioObj = null;
        transactionPortfolioObj = null;
        exchangePortfolioObj = null;
        search_options_assets = new ArrayList<>();
        search_options_asset_names = new ArrayList<>();
        search_options_asset_display_names = new ArrayList<>();
        transactionArrayList = new ArrayList<>();
        tableInfo = null;
        filterInfo = null;
    }
}

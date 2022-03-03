package com.musicslayer.cryptobuddy.state;

import com.musicslayer.cryptobuddy.api.address.AddressData;
import com.musicslayer.cryptobuddy.api.address.CryptoAddress;
import com.musicslayer.cryptobuddy.api.chart.ChartData;
import com.musicslayer.cryptobuddy.api.chart.CryptoChart;
import com.musicslayer.cryptobuddy.api.exchange.CryptoExchange;
import com.musicslayer.cryptobuddy.api.exchange.ExchangeData;
import com.musicslayer.cryptobuddy.api.price.PriceData;
import com.musicslayer.cryptobuddy.asset.Asset;
import com.musicslayer.cryptobuddy.data.persistent.user.AddressPortfolioObj;
import com.musicslayer.cryptobuddy.data.persistent.user.ChartPortfolioObj;
import com.musicslayer.cryptobuddy.data.persistent.user.ExchangePortfolioObj;
import com.musicslayer.cryptobuddy.data.persistent.user.TransactionPortfolioObj;
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

    public static HashMap<CryptoChart, ChartData> chartDataMap = new HashMap<>();
    public static HashMap<CryptoChart, ChartData> chartDataFilterMap = new HashMap<>();

    public static PriceData priceData;
    public static TransactionData transactionData;

    public static TransactionPortfolioObj transactionPortfolioObj;
    public static AddressPortfolioObj addressPortfolioObj;
    public static ExchangePortfolioObj exchangePortfolioObj;
    public static ChartPortfolioObj chartPortfolioObj;

    public static ArrayList<Asset> search_options_assets = new ArrayList<>();
    public static ArrayList<String> search_options_asset_names = new ArrayList<>();
    public static ArrayList<String> search_options_asset_display_names = new ArrayList<>();

    public static ArrayList<Transaction> transactionArrayList = new ArrayList<>();
    public static ArrayList<Transaction> filteredTransactionArrayList = new ArrayList<>();

    public static String tableInfo;
    public static String chartInfo;
    public static String filterInfo;

    public static void resetState() {
        addressDataMap = new HashMap<>();
        addressDataFilterMap = new HashMap<>();
        exchangeDataMap = new HashMap<>();
        exchangeDataFilterMap = new HashMap<>();
        chartDataMap = new HashMap<>();
        chartDataFilterMap = new HashMap<>();
        priceData = null;
        transactionData = null;
        transactionPortfolioObj = null;
        addressPortfolioObj = null;
        exchangePortfolioObj = null;
        chartPortfolioObj = null;
        search_options_assets = new ArrayList<>();
        search_options_asset_names = new ArrayList<>();
        search_options_asset_display_names = new ArrayList<>();
        transactionArrayList = new ArrayList<>();
        filteredTransactionArrayList = new ArrayList<>();
        tableInfo = null;
        chartInfo = null;
        filterInfo = null;
    }
}

package com.musicslayer.cryptobuddy.state;

import com.musicslayer.cryptobuddy.api.address.AddressData;
import com.musicslayer.cryptobuddy.api.address.CryptoAddress;
import com.musicslayer.cryptobuddy.api.exchange.ExchangeAPI;
import com.musicslayer.cryptobuddy.api.exchange.ExchangeData;
import com.musicslayer.cryptobuddy.asset.Asset;
import com.musicslayer.cryptobuddy.asset.exchange.Exchange;
import com.musicslayer.cryptobuddy.persistence.AddressPortfolioObj;
import com.musicslayer.cryptobuddy.persistence.ExchangePortfolioObj;
import com.musicslayer.cryptobuddy.persistence.TransactionPortfolioObj;
import com.musicslayer.cryptobuddy.transaction.Transaction;

import java.util.ArrayList;
import java.util.HashMap;

// This class stores data that would be to expensive to recalculate upon recreation.
// This should be reset at appropriate moments to prevent memory leaks.
public class StateObj {
    public static HashMap<CryptoAddress, AddressData> addressDataMap = new HashMap<>();
    public static HashMap<CryptoAddress, AddressData> addressDataFilterMap = new HashMap<>();

    public static HashMap<Exchange, ExchangeData> exchangeDataMap = new HashMap<>();
    public static HashMap<Exchange, ExchangeAPI> exchangeAPIMap = new HashMap<>();
    public static HashMap<Exchange, ExchangeData> exchangeDataFilterMap = new HashMap<>();

    public static AddressPortfolioObj addressPortfolioObj;
    public static TransactionPortfolioObj transactionPortfolioObj;
    public static ExchangePortfolioObj exchangePortfolioObj;

    public static ArrayList<Asset> assetArrayList = new ArrayList<>();
    public static ArrayList<String> options_symbols = new ArrayList<>();
    public static ArrayList<String> options_names = new ArrayList<>();

    public static ArrayList<Transaction> transactionArrayList = new ArrayList<>();
    public static ArrayList<Transaction> maskedTransactionArrayList = new ArrayList<>();
    public static ArrayList<Transaction> filteredMaskedTransactionArrayList = new ArrayList<>();

    public static String tableInfo;

    public static void resetState() {
        addressDataMap = new HashMap<>();
        addressDataFilterMap = new HashMap<>();
        exchangeDataMap = new HashMap<>();
        exchangeAPIMap = new HashMap<>();
        exchangeDataFilterMap = new HashMap<>();
        addressPortfolioObj = null;
        transactionPortfolioObj = null;
        exchangePortfolioObj = null;
        assetArrayList = new ArrayList<>();
        options_symbols = new ArrayList<>();
        options_names = new ArrayList<>();
        transactionArrayList = new ArrayList<>();
        maskedTransactionArrayList = new ArrayList<>();
        tableInfo = null;
    }
}

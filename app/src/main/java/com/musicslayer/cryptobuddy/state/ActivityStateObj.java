package com.musicslayer.cryptobuddy.state;

import com.musicslayer.cryptobuddy.api.address.AddressData;
import com.musicslayer.cryptobuddy.api.address.CryptoAddress;
import com.musicslayer.cryptobuddy.api.exchange.ExchangeAPI;
import com.musicslayer.cryptobuddy.api.exchange.ExchangeData;
import com.musicslayer.cryptobuddy.asset.exchange.Exchange;
import com.musicslayer.cryptobuddy.persistence.AddressPortfolioObj;
import com.musicslayer.cryptobuddy.persistence.ExchangePortfolioObj;
import com.musicslayer.cryptobuddy.persistence.TransactionPortfolioObj;

import java.util.HashMap;

// TODO more comprehensive solution to state of large data.

// Currently used in a small number of cases, but may become more widely used later.
public class ActivityStateObj {
    public HashMap<CryptoAddress, AddressData> addressDataMap = new HashMap<>();
    public HashMap<CryptoAddress, AddressData> addressDataFilterMap = new HashMap<>();

    public HashMap<Exchange, ExchangeData> exchangeDataMap = new HashMap<>();
    public HashMap<Exchange, ExchangeAPI> exchangeAPIMap = new HashMap<>();
    public HashMap<Exchange, ExchangeData> exchangeDataFilterMap = new HashMap<>();

    public AddressPortfolioObj addressPortfolioObj;
    public TransactionPortfolioObj transactionPortfolioObj;
    public ExchangePortfolioObj exchangePortfolioObj;
}

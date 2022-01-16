package com.musicslayer.cryptobuddy.state;

import com.musicslayer.cryptobuddy.api.address.AddressData;
import com.musicslayer.cryptobuddy.api.address.CryptoAddress;
import com.musicslayer.cryptobuddy.api.exchange.ExchangeData;
import com.musicslayer.cryptobuddy.persistence.AddressPortfolioObj;
import com.musicslayer.cryptobuddy.persistence.TransactionPortfolioObj;

import java.util.HashMap;

// TODO more comprehensive solution to state of large data.

// Currently used in a small number of cases, but may become more widely used later.
public class ActivityStateObj {
    public HashMap<CryptoAddress, AddressData> addressDataMap = new HashMap<>();
    public HashMap<CryptoAddress, AddressData> addressDataFilterMap = new HashMap<>();
    public AddressPortfolioObj addressPortfolioObj;
    public TransactionPortfolioObj transactionPortfolioObj;

    public HashMap<String, ExchangeData> exchangeDataMap = new HashMap<>();
    public HashMap<String, ExchangeData> exchangeDataFilterMap = new HashMap<>();
}

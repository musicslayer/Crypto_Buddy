package com.musicslayer.cryptobuddy.state;

import com.musicslayer.cryptobuddy.transaction.Transaction;
import com.musicslayer.cryptobuddy.view.table.Table;

import java.util.ArrayList;

// Currently used in a small number of cases, but may become more widely used later.
public class TableStateObj {
    public Table table;
    public ArrayList<Transaction> transactionArrayList = new ArrayList<>();
    public ArrayList<Transaction> maskedTransactionArrayList = new ArrayList<>();
}

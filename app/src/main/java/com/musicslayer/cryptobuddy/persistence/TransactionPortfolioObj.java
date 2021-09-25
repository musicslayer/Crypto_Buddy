package com.musicslayer.cryptobuddy.persistence;

import com.musicslayer.cryptobuddy.asset.Asset;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.asset.crypto.token.Token;
import com.musicslayer.cryptobuddy.asset.fiat.Fiat;
import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;
import com.musicslayer.cryptobuddy.transaction.Action;
import com.musicslayer.cryptobuddy.transaction.AssetAmount;
import com.musicslayer.cryptobuddy.transaction.AssetQuantity;
import com.musicslayer.cryptobuddy.transaction.Timestamp;
import com.musicslayer.cryptobuddy.transaction.Transaction;
import com.musicslayer.cryptobuddy.util.DateTime;

import java.util.ArrayList;

public class TransactionPortfolioObj {
    public String name;
    public ArrayList<Transaction> transactionArrayList = new ArrayList<>();

    public TransactionPortfolioObj(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object other) {
        return (other instanceof TransactionPortfolioObj) && name.equals(((TransactionPortfolioObj)other).name);
    }

    public void addData(Transaction transaction) {
        transactionArrayList.add(transaction);
    }

    public static String getTokenType(Asset asset) {
        if(asset instanceof Fiat) {
            return "!FIAT!";
        }
        else if(asset instanceof Coin) {
            return "!COIN!";
        }
        else {
            return ((Token)asset).getTokenType();
        }
    }

    public static Asset getAsset(String tokenType, String key) {
        if("!FIAT!".equals(tokenType)) {
            return Fiat.getFiatFromKey(key);
        }
        else if("!COIN!".equals(tokenType)) {
            return Coin.getCoinFromKey(key);
        }
        else {
            return TokenManager.getTokenManagerFromTokenType(tokenType).getToken(key, null, null, 0, null);
        }
    }

    public String serialize() {
        StringBuilder s = new StringBuilder();
        s.append(name);

        for(Transaction transaction : transactionArrayList) {
            String actionString = transaction.action.toString();
            String actionedAssetTokenTypeString = getTokenType(transaction.actionedAssetQuantity.asset);
            String actionedAssetKeyString = transaction.actionedAssetQuantity.asset.getKey();
            String actionedAssetAmountString = transaction.actionedAssetQuantity.assetAmount.amount.toString();

            String otherAssetTokenTypeString;
            String otherAssetKeyString;
            String otherAssetAmountString;
            if(transaction.otherAssetQuantity == null) {
                otherAssetTokenTypeString = "";
                otherAssetKeyString = "";
                otherAssetAmountString = "";
            }
            else {
                otherAssetTokenTypeString = getTokenType(transaction.otherAssetQuantity.asset);
                otherAssetKeyString = transaction.otherAssetQuantity.asset.getKey();
                otherAssetAmountString = transaction.otherAssetQuantity.assetAmount.amount.toString();
            }

            String timestampString = DateTime.serialize(transaction.timestamp.date);
            String infoString = transaction.info;

            s.append("\n")
                .append(actionString).append("|")
                .append(actionedAssetTokenTypeString).append("|")
                .append(actionedAssetKeyString).append("|")
                .append(actionedAssetAmountString).append("|")
                .append(otherAssetTokenTypeString).append("|")
                .append(otherAssetKeyString).append("|")
                .append(otherAssetAmountString).append("|")
                .append(timestampString).append("|")
                .append(infoString).append("|").append("END_MARKER");
        }

        return s.toString();
    }

    public static TransactionPortfolioObj deserialize(String s) {
        String[] sArray = s.split("\n");

        String name = sArray[0];
        TransactionPortfolioObj transactionPortfolioObj = new TransactionPortfolioObj(name);

        // A crypto may no longer exist, especially if it is a token.
        for(int i = 1; i < sArray.length; i++) {
            String[] transactionStringArray = sArray[i].split("\\|");

            Action action = new Action(transactionStringArray[0]);

            Asset asset = getAsset(transactionStringArray[1], transactionStringArray[2]);
            AssetQuantity actionedAssetQuantity = new AssetQuantity(new AssetAmount(transactionStringArray[3]), asset);

            AssetQuantity otherAssetQuantity;
            if(transactionStringArray[4].isEmpty() && transactionStringArray[5].isEmpty()) {
                otherAssetQuantity = null;
            }
            else {
                Asset otherAsset = getAsset(transactionStringArray[4], transactionStringArray[5]);
                otherAssetQuantity = new AssetQuantity(new AssetAmount(transactionStringArray[6]), otherAsset);
            }

            Timestamp timestamp = new Timestamp(DateTime.deserialize(transactionStringArray[7]));
            String info = transactionStringArray[8];

            transactionPortfolioObj.addData(new Transaction(action, actionedAssetQuantity, otherAssetQuantity, timestamp, info));
        }

        return transactionPortfolioObj;
    }
}

package com.musicslayer.cryptobuddy.api.address;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.crypto.coin.BNBc;
import com.musicslayer.cryptobuddy.asset.crypto.token.UnknownToken;
import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;
import com.musicslayer.cryptobuddy.transaction.Action;
import com.musicslayer.cryptobuddy.transaction.AssetQuantity;
import com.musicslayer.cryptobuddy.transaction.Timestamp;
import com.musicslayer.cryptobuddy.transaction.Transaction;
import com.musicslayer.cryptobuddy.util.ExceptionLogger;
import com.musicslayer.cryptobuddy.util.REST;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Bitquery extends AddressAPI {
    public String getName() { return "Bitquery"; }
    public String getDisplayName() { return "Binance Chain & Bitquery HTTP APIs"; }

    public boolean isSupported(CryptoAddress cryptoAddress) {
        return "BNBc".equals(cryptoAddress.getCrypto().getName());
    }

    public ArrayList<AssetQuantity> getCurrentBalance(CryptoAddress cryptoAddress) {
        ArrayList<AssetQuantity> currentBalanceArrayList = new ArrayList<>();

        String baseURL;
        if(cryptoAddress.network.isMainnet()) {
            baseURL = "https://dex.binance.org";
        }
        else {
            baseURL = "https://testnet-dex.binance.org";
        }

        // Bitquery doesn't give us this for binance, so use other API.
        String addressDataJSON = REST.get(baseURL + "/api/v1/account/" + cryptoAddress.address);
        if(addressDataJSON != null) {
            try {
                JSONObject json = new JSONObject(addressDataJSON);
                JSONArray jsonArray = json.getJSONArray("balances");

                for(int j = 0; j < jsonArray.length(); j++) {
                    JSONObject o = jsonArray.getJSONObject(j);

                    String cryptoName = o.getString("symbol");
                    Crypto crypto;
                    if("BNB".equals(cryptoName)) {
                        crypto = cryptoAddress.getCrypto();
                    }
                    else {
                        if(!shouldIncludeTokens(cryptoAddress)) {
                            continue;
                        }

                        crypto = TokenManager.getTokenManagerFromKey("BinanceChainTokenManager").getToken(cryptoName, "?", "?", 0, "?");
                        if(crypto instanceof UnknownToken) {
                            // Try BEP8
                            crypto = TokenManager.getTokenManagerFromKey("BinanceChainMiniTokenManager").getToken(cryptoName, "?", "?", 0, "?");
                        }
                        if(crypto instanceof UnknownToken) {
                            // new UnknownToken type.
                            crypto = UnknownToken.createUnknownToken(cryptoName, cryptoName, cryptoName, 0, null, null, "BNBc");
                        }
                    }

                    BigDecimal freeBalance = new BigDecimal(o.getString("free"));
                    BigDecimal frozenBalance = new BigDecimal(o.getString("frozen"));
                    BigDecimal lockedBalance = new BigDecimal(o.getString("locked"));

                    currentBalanceArrayList.add(new AssetQuantity(freeBalance.add(frozenBalance).add(lockedBalance).toPlainString(), crypto));
                }
            }
            catch(Exception e) {
                ExceptionLogger.processException(e);
                return null;
            }
        }
        else {
            currentBalanceArrayList.add(new AssetQuantity("0", new BNBc()));
        }

        return currentBalanceArrayList;
    }

    // There is no flag for transactions that failed/errored.

    public ArrayList<Transaction> getTransactions(CryptoAddress cryptoAddress) {
        ArrayList<Transaction> transactionArrayList = new ArrayList<>();

        if(!cryptoAddress.network.isMainnet()) {
            // Return 0 transactions. We tell the user to expect this.
            return transactionArrayList;
        }

        String bodyR = "{\"query\" : \"" +
            "query{" +
            "  binance{" +
            "    transfers(options: {desc: \\\"block.timestamp.time\\\"}, receiver: {is: \\\"" + cryptoAddress.address + "\\\"}) {" +
            "      block{" +
            "        timestamp{" +
            "          time(format: \\\"%Y-%m-%d %H:%M:%S\\\")" +
            "        }" +
            "        height" +
            "      }" +
            "      currency{" +
            "        address " +
            "        symbol " +
            "        tokenId " +
            "        name " +
            "      }" +
            "      amount " +
            "      transaction{" +
            "        hash" +
            "      }" +
            "      transferType" +
            "    }" +
            "  }" +
            "}" +
            "\"\n}";

        String bodyS = "{\"query\" : \"" +
                "query{" +
                "  binance{" +
                "    transfers(options: {desc: \\\"block.timestamp.time\\\"}, sender: {is: \\\"" + cryptoAddress.address + "\\\"}) {" +
                "      block{" +
                "        timestamp{" +
                "          time(format: \\\"%Y-%m-%d %H:%M:%S\\\")" +
                "        }" +
                "        height" +
                "      }" +
                "      currency{" +
                "        address " +
                "        symbol " +
                "        tokenId " +
                "        name " +
                "      }" +
                "      amount " +
                "      transaction{" +
                "        hash" +
                "      }" +
                "      transferType" +
                "    }" +
                "  }" +
                "}" +
                "\"\n}";

        String APIKEYNAME = "X-API-KEY";
        String APIKEY = "BQYLR11ACrzwoU3N6iTNHKtZfgoNdWfI";

        String addressDataJSONReceive = REST.postWithKey("https://graphql.bitquery.io", bodyR, APIKEYNAME, APIKEY);
        String addressDataJSONSend = REST.postWithKey("https://graphql.bitquery.io", bodyS, APIKEYNAME, APIKEY);

        if(addressDataJSONReceive == null || addressDataJSONSend == null) {
            return null;
        }

        try {
            // Receive
            JSONObject jsonR = new JSONObject(addressDataJSONReceive);
            JSONArray jsonArrayR = jsonR.getJSONObject("data").getJSONObject("binance").getJSONArray("transfers");

            for(int j = 0; j < jsonArrayR.length(); j++) {
                JSONObject o = jsonArrayR.getJSONObject(j);

                BigDecimal balance_diff_d;
                String txType = o.getString("transferType");

                balance_diff_d = new BigDecimal(o.getString("amount"));

                String action = "Receive";
                String balance_diff_s = balance_diff_d.toPlainString();

                String block_time = o.getJSONObject("block").getJSONObject("timestamp").getString("time");

                DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
                format.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date block_time_date = format.parse(block_time);

                String cryptoName = o.getJSONObject("currency").getString("tokenId");
                Crypto crypto;
                if("BNB".equals(cryptoName)) {
                    crypto = cryptoAddress.getCrypto();
                }
                else {
                    if(!shouldIncludeTokens(cryptoAddress)) {
                        continue;
                    }

                    crypto = TokenManager.getTokenManagerFromKey("BinanceChainTokenManager").getToken(cryptoName, "?", "?", 0, "?");
                    if(crypto instanceof UnknownToken) {
                        // Try BEP8
                        crypto = TokenManager.getTokenManagerFromKey("BinanceChainMiniTokenManager").getToken(cryptoName, "?", "?", 0, "?");
                    }
                    if(crypto instanceof UnknownToken) {
                        // new UnknownToken type.
                        crypto = UnknownToken.createUnknownToken(cryptoName, cryptoName, cryptoName, 0, null, null, "BNBc");
                    }
                }

                transactionArrayList.add(new Transaction(new Action(action), new AssetQuantity(balance_diff_s, crypto), null, new Timestamp(block_time_date), "Transaction (" + txType + ")"));
                if(transactionArrayList.size() == getMaxTransactions()) { return transactionArrayList; }
            }

            // Send
            JSONObject jsonS = new JSONObject(addressDataJSONSend);
            JSONArray jsonArrayS = jsonS.getJSONObject("data").getJSONObject("binance").getJSONArray("transfers");

            for(int j = 0; j < jsonArrayS.length(); j++) {
                JSONObject o = jsonArrayS.getJSONObject(j);

                BigDecimal balance_diff_d;
                String txType = o.getString("transferType");

                balance_diff_d = new BigDecimal(o.getString("amount"));

                String action;

                if("TX_FEE".equals(txType)) {
                    action = "Fee";
                }
                else {
                    action = "Send";
                }

                // "Crosschain transfer out" has a fixed network fee of 0.004, separate from the normal fee.
                BigDecimal network_fee = BigDecimal.ZERO;
                if("TRANSFER_OUT".equals(txType)) {
                    network_fee = new BigDecimal("0.004");
                }

                String network_fee_s = network_fee.toPlainString();
                String balance_diff_s = balance_diff_d.toPlainString();

                String block_time = o.getJSONObject("block").getJSONObject("timestamp").getString("time");

                DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
                format.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date block_time_date = format.parse(block_time);

                String cryptoName = o.getJSONObject("currency").getString("tokenId");
                Crypto crypto;
                if("BNB".equals(cryptoName)) {
                    crypto = cryptoAddress.getCrypto();
                }
                else {
                    if(!shouldIncludeTokens(cryptoAddress)) {
                        continue;
                    }

                    crypto = TokenManager.getTokenManagerFromKey("BinanceChainTokenManager").getToken(cryptoName, "?", "?", 0, "?");
                    if(crypto instanceof UnknownToken) {
                        // Try BEP8
                        crypto = TokenManager.getTokenManagerFromKey("BinanceChainMiniTokenManager").getToken(cryptoName, "?", "?", 0, "?");
                    }
                    if(crypto instanceof UnknownToken) {
                        // new UnknownToken type.
                        crypto = UnknownToken.createUnknownToken(cryptoName, cryptoName, cryptoName, 0, null, null, "BNBc");
                    }
                }

                transactionArrayList.add(new Transaction(new Action(action), new AssetQuantity(balance_diff_s, crypto), null, new Timestamp(block_time_date), "Transaction (" + txType + ")"));
                if(transactionArrayList.size() == getMaxTransactions()) { return transactionArrayList; }

                if(network_fee.compareTo(BigDecimal.ZERO) > 0) {
                    transactionArrayList.add(new Transaction(new Action("Fee"), new AssetQuantity(network_fee_s, crypto), null, new Timestamp(block_time_date),  "Binance Cross-Chain Network Fee"));
                    if(transactionArrayList.size() == getMaxTransactions()) { return transactionArrayList; }
                }
            }
        }
        catch(Exception e) {
            ExceptionLogger.processException(e);
            return null;
        }

        // Staking rewards from validators.
        // TODO use the "total" field to know when to stop.
        // TODO if this is null, should we return the above transactions, return null, stop the loop?
        for(int offsetNum = 0; offsetNum <= 400000; offsetNum += 100) {
            // Bitquery doesn't give us this so use other API.
            String addressDataRewardJSON = REST.get("https://api.binance.org/v1/staking/chains/bsc/delegators/" + cryptoAddress.address + "/rewards?limit=100&offset=" + offsetNum);

            if(addressDataRewardJSON != null) {
                try {
                    JSONObject jsonRewards = new JSONObject(addressDataRewardJSON);
                    JSONArray jsonRewardsArray = jsonRewards.getJSONArray("rewardDetails");

                    if(jsonRewardsArray.length() == 0) { break; }

                    for(int j = 0; j < jsonRewardsArray.length(); j++) {
                        JSONObject oRewards = jsonRewardsArray.getJSONObject(j);

                        String balance_diff_s = oRewards.getString("reward");

                        String block_time = oRewards.getString("rewardTime");

                        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'00:00:00.000+00:00", Locale.ENGLISH);
                        format.setTimeZone(TimeZone.getTimeZone("UTC"));
                        Date block_time_date = format.parse(block_time);

                        String validatorName = oRewards.getString("valName");

                        transactionArrayList.add(new Transaction(new Action("Receive"), new AssetQuantity(balance_diff_s, cryptoAddress.getCrypto()), null, new Timestamp(block_time_date), "Staking Reward (" + validatorName + ")"));
                        if(transactionArrayList.size() == getMaxTransactions()) { return transactionArrayList; }
                    }
                }
                catch(Exception e) {
                    ExceptionLogger.processException(e);
                    return null;
                }
            }
        }

        return transactionArrayList;
    }
}

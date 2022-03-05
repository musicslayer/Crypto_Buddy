package com.musicslayer.cryptobuddy.api.address;

import com.musicslayer.cryptobuddy.asset.crypto.token.Token;
import com.musicslayer.cryptobuddy.asset.network.ETH_Testnet_Goerli;
import com.musicslayer.cryptobuddy.asset.network.ETH_Testnet_Kovan;
import com.musicslayer.cryptobuddy.asset.network.ETH_Testnet_Rinkeby;
import com.musicslayer.cryptobuddy.asset.network.ETH_Testnet_Ropsten;
import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;
import com.musicslayer.cryptobuddy.transaction.Action;
import com.musicslayer.cryptobuddy.transaction.AssetQuantity;
import com.musicslayer.cryptobuddy.transaction.Timestamp;
import com.musicslayer.cryptobuddy.transaction.Transaction;
import com.musicslayer.cryptobuddy.util.DateTimeUtil;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;
import com.musicslayer.cryptobuddy.util.WebUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;

// Use Etherscan for transactions, but use Ethplorer for balance information so we can see token balances.
// Unfortunately, between the two APIs, display names of tokens are mismatched.

// Etherscan has no pagination on its results.

public class Etherscan extends AddressAPI {
    public final String APIKEY_etherscan = "ZHZ4Y7XKI9JD6XT8HV9HDZJMA8RHY7Y6DP";
    public final String APIKEY_ethplorer = "freekey";

    public String getName() { return "Etherscan"; }
    public String getDisplayName() { return "Etherscan & Ethplorer REST APIs"; }

    public boolean isSupported(CryptoAddress cryptoAddress) {
        return "ETH".equals(cryptoAddress.getPrimaryCoin().getKey());
    }

    public ArrayList<AssetQuantity> getCurrentBalance(CryptoAddress cryptoAddress) {
        if(cryptoAddress.network.isMainnet()) {
            return getCurrentBalanceMain(cryptoAddress);
        }
        else {
            return getCurrentBalanceTest(cryptoAddress);
        }
    }

    public ArrayList<AssetQuantity> getCurrentBalanceMain(CryptoAddress cryptoAddress) {
        ArrayList<AssetQuantity> currentBalanceArrayList = new ArrayList<>();

        String baseURL = "https://api.ethplorer.io";
        String addressDataJSON = WebUtil.get(baseURL + "/getAddressInfo/" + cryptoAddress.address + "?apiKey=" + APIKEY_ethplorer);
        if(addressDataJSON == null) {
            return null;
        }

        try {
            JSONObject json = new JSONObject(addressDataJSON);

            // ETH
            BigDecimal b = new BigDecimal(json.getJSONObject("ETH").getString("rawBalance"));
            b = b.movePointLeft(cryptoAddress.getPrimaryCoin().getScale());
            currentBalanceArrayList.add(new AssetQuantity(b.toPlainString(), cryptoAddress.getPrimaryCoin()));

            if(shouldIncludeTokens(cryptoAddress)) {
                // Tokens
                if(json.has("tokens")) {
                    JSONArray tokenArrayData = json.getJSONArray("tokens");
                    for(int i = 0; i < tokenArrayData.length(); i++) {
                        JSONObject tokenData = tokenArrayData.getJSONObject(i);
                        JSONObject tokenInfo = tokenData.getJSONObject("tokenInfo");

                        String name = tokenInfo.getString("symbol");
                        String display_name = tokenInfo.getString("name");
                        int scale = tokenInfo.getInt("decimals");
                        String key = tokenInfo.getString("address");
                        String id = key.toLowerCase();

                        Token token = TokenManager.getTokenManagerFromKey("EthereumTokenManager").getToken(cryptoAddress, key, name, display_name, scale, id);

                        BigDecimal tokenB = new BigDecimal(tokenData.getString("rawBalance"));
                        tokenB = tokenB.movePointLeft(token.getScale());
                        currentBalanceArrayList.add(new AssetQuantity(tokenB.toPlainString(), token));
                    }
                }
            }
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            return null;
        }

        return currentBalanceArrayList;
    }

    public ArrayList<AssetQuantity> getCurrentBalanceTest(CryptoAddress cryptoAddress) {
        // We can only give ETH balance here.
        ArrayList<AssetQuantity> currentBalanceArrayList = new ArrayList<>();

        String baseURL;
        if(cryptoAddress.network.isMainnet()) {
            baseURL = "https://api.etherscan.io";
        }
        else if(cryptoAddress.network instanceof ETH_Testnet_Goerli) {
            baseURL = "https://api-goerli.etherscan.io";
        }
        else if(cryptoAddress.network instanceof ETH_Testnet_Kovan) {
            baseURL = "https://api-kovan.etherscan.io";
        }
        else if(cryptoAddress.network instanceof ETH_Testnet_Rinkeby) {
            baseURL = "https://api-rinkeby.etherscan.io";
        }
        else if(cryptoAddress.network instanceof ETH_Testnet_Ropsten) {
            baseURL = "https://api-ropsten.etherscan.io";
        }
        else {
            return null;
        }

        String addressDataJSON = WebUtil.get(baseURL + "/api?module=account&action=balance&address=" + cryptoAddress.address + "&apikey=" + APIKEY_etherscan);
        if(addressDataJSON == null) {
            return null;
        }

        try {
            JSONObject json = new JSONObject(addressDataJSON);
            String amount = new BigDecimal(json.getString("result")).movePointLeft(cryptoAddress.getPrimaryCoin().getScale()).toPlainString();
            currentBalanceArrayList.add(new AssetQuantity(amount, cryptoAddress.getPrimaryCoin()));
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            return null;
        }

        return currentBalanceArrayList;
    }

    public ArrayList<Transaction> getTransactions(CryptoAddress cryptoAddress) {
        ArrayList<Transaction> transactionNormalArrayList = new ArrayList<>();
        ArrayList<Transaction> transactionInternalArrayList = new ArrayList<>();
        ArrayList<Transaction> transactionTokenArrayList = new ArrayList<>();

        String baseURL;
        if(cryptoAddress.network.isMainnet()) {
            baseURL = "https://api.etherscan.io";
        }
        else if(cryptoAddress.network instanceof ETH_Testnet_Goerli) {
            baseURL = "https://api-goerli.etherscan.io";
        }
        else if(cryptoAddress.network instanceof ETH_Testnet_Kovan) {
            baseURL = "https://api-kovan.etherscan.io";
        }
        else if(cryptoAddress.network instanceof ETH_Testnet_Rinkeby) {
            baseURL = "https://api-rinkeby.etherscan.io";
        }
        else if(cryptoAddress.network instanceof ETH_Testnet_Ropsten) {
            baseURL = "https://api-ropsten.etherscan.io";
        }
        else {
            return null;
        }

        // Normal Transactions - These are all ETH
        String addressDataJSON = WebUtil.get(baseURL + "/api?module=account&action=txlist&address=" + cryptoAddress.address + "&startblock=1&endblock=99999999&sort=asc&apikey=" + APIKEY_etherscan);

        // Internal Transactions - These are all ETH
        String addressDataInternalJSON = WebUtil.get(baseURL + "/api?module=account&action=txlistinternal&address=" + cryptoAddress.address + "&startblock=1&endblock=99999999&sort=asc&apikey=" + APIKEY_etherscan);

        if(addressDataJSON == null || addressDataInternalJSON == null) {
            return null;
        }

        try {
            // Normal
            JSONObject json = new JSONObject(addressDataJSON);
            JSONArray jsonArray = json.getJSONArray("result");

            for(int j = 0; j < jsonArray.length(); j++) {
                JSONObject o = jsonArray.getJSONObject(j);

                Date block_time_date = null;

                BigInteger confirmations = new BigInteger(o.getString("confirmations"));
                if(confirmations.compareTo(BigInteger.valueOf(0)) > 0) {
                    String block_time = o.getString("timeStamp");
                    block_time_date = DateTimeUtil.parseSeconds(block_time);
                }

                String from = o.getString("from");
                String to = o.getString("to");

                String action;
                BigDecimal fee;

                if(cryptoAddress.matchesAddress(from)) {
                    // We are sending crypto away.
                    action = "Send";

                    // We also have to add in the fee to the amount sent.
                    BigDecimal gasAmount = new BigDecimal(o.getString("gasUsed"));
                    BigDecimal gasPrice = new BigDecimal(o.getString("gasPrice"));
                    fee = gasAmount.multiply(gasPrice);
                }
                else if(cryptoAddress.matchesAddress(to)) {
                    // We are receiving crypto. No fee.
                    action = "Receive";
                    fee = BigDecimal.ZERO;
                }
                else {
                    // We shouldn't get here...
                    continue;
                }

                fee = fee.movePointLeft(cryptoAddress.getFeeCoin().getScale());

                if(fee.compareTo(BigDecimal.ZERO) > 0) {
                    transactionNormalArrayList.add(new Transaction(new Action("Fee"), new AssetQuantity(fee.toPlainString(), cryptoAddress.getFeeCoin()), null, new Timestamp(block_time_date), "Transaction Fee"));
                    if(transactionNormalArrayList.size() == getMaxTransactions()) { break; }
                }

                // If I send something to myself, just reject it!
                if(cryptoAddress.network.matchesAddress(from, to)) { continue; }

                // If this has an error, skip the rest.
                if("1".equals(o.getString("isError"))) {
                    continue;
                }

                BigInteger balance_diff = new BigInteger(o.getString("value"));
                BigDecimal balance_diff_d = new BigDecimal(balance_diff);
                balance_diff_d = balance_diff_d.movePointLeft(cryptoAddress.getPrimaryCoin().getScale());
                String balance_diff_s = balance_diff_d.toPlainString();

                transactionNormalArrayList.add(new Transaction(new Action(action), new AssetQuantity(balance_diff_s, cryptoAddress.getPrimaryCoin()), null, new Timestamp(block_time_date), "Transaction"));
                if(transactionNormalArrayList.size() == getMaxTransactions()) { break; }
            }

            // Internal - Fees are already counted elsewhere.
            JSONObject jsonInternal = new JSONObject(addressDataInternalJSON);
            JSONArray jsonInternalArray = jsonInternal.getJSONArray("result");

            for(int j = 0; j < jsonInternalArray.length(); j++) {
                JSONObject oI = jsonInternalArray.getJSONObject(j);

                // Internal transfers do not use confirmations.
                String block_time = oI.getString("timeStamp");
                Date block_time_date = DateTimeUtil.parseSeconds(block_time);

                String from = oI.getString("from");
                String to = oI.getString("to");

                String action;

                if(cryptoAddress.matchesAddress(from)) {
                    // We are sending crypto away.
                    action = "Send";
                }
                else if(cryptoAddress.matchesAddress(to)) {
                    // We are receiving crypto. No fee.
                    action = "Receive";
                }
                else {
                    // We shouldn't get here...
                    continue;
                }

                // If I send something to myself, just reject it!
                if(cryptoAddress.network.matchesAddress(from, to)) { continue; }

                // If this has an error, skip the rest.
                if("1".equals(oI.getString("isError"))) {
                    continue;
                }

                BigInteger balance_diff = new BigInteger(oI.getString("value"));
                BigDecimal balance_diff_d = new BigDecimal(balance_diff);
                balance_diff_d = balance_diff_d.movePointLeft(cryptoAddress.getPrimaryCoin().getScale());
                String balance_diff_s = balance_diff_d.toPlainString();

                transactionInternalArrayList.add(new Transaction(new Action(action), new AssetQuantity(balance_diff_s, cryptoAddress.getPrimaryCoin()), null, new Timestamp(block_time_date), "Internal Transaction"));
                if(transactionInternalArrayList.size() == getMaxTransactions()) { break; }
            }
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            return null;
        }

        if(shouldIncludeTokens(cryptoAddress)) {
            // ERC-20 Transactions - Various Tokens
            String addressDataTokenJSON = WebUtil.get(baseURL + "/api?module=account&action=tokentx&address=" + cryptoAddress.address + "&startblock=1&endblock=99999999&sort=asc&apikey=" + APIKEY_etherscan);

            if(addressDataTokenJSON == null) {
                return null;
            }

            try {
                // Tokens
                JSONObject jsonToken = new JSONObject(addressDataTokenJSON);
                JSONArray jsonTokenArray = jsonToken.getJSONArray("result");

                for(int j = 0; j < jsonTokenArray.length(); j++) {
                    JSONObject oT = jsonTokenArray.getJSONObject(j);

                    // Token transactions don't have an error flag or a fee.

                    BigInteger balance_diff = new BigInteger(oT.getString("value"));

                    String from = oT.getString("from");
                    String to = oT.getString("to");

                    // If I send something to myself, just reject it!
                    if(cryptoAddress.network.matchesAddress(from, to)) { continue; }

                    String action;
                    if(cryptoAddress.matchesAddress(from)) {
                        // We are sending crypto away.
                        action = "Send";
                    }
                    else if(cryptoAddress.matchesAddress(to)) {
                        // We are receiving crypto. No fee.
                        action = "Receive";
                    }
                    else {
                        // We shouldn't get here...
                        continue;
                    }

                    BigDecimal balance_diff_d = new BigDecimal(balance_diff);

                    // Shift by token decimal
                    BigInteger tokenDecimal = new BigInteger(oT.getString("tokenDecimal"));
                    balance_diff_d = balance_diff_d.movePointLeft(tokenDecimal.intValue());
                    String balance_diff_s = balance_diff_d.toPlainString();

                    Date block_time_date = null;

                    BigInteger confirmations = new BigInteger(oT.getString("confirmations"));
                    if(confirmations.compareTo(BigInteger.valueOf(0)) > 0) {
                        String block_time = oT.getString("timeStamp");
                        block_time_date = DateTimeUtil.parseSeconds(block_time);
                    }

                    String name = oT.getString("tokenSymbol");
                    String display_name = oT.getString("tokenName");
                    int scale = tokenDecimal.intValue();
                    String key = oT.getString("contractAddress");
                    String id = key.toLowerCase();

                    Token token = TokenManager.getTokenManagerFromKey("EthereumTokenManager").getToken(cryptoAddress, key, name, display_name, scale, id);

                    transactionTokenArrayList.add(new Transaction(new Action(action), new AssetQuantity(balance_diff_s, token), null, new Timestamp(block_time_date), "Token Transaction"));
                    if(transactionTokenArrayList.size() == getMaxTransactions()) { break; }
                }
            }
            catch(Exception e) {
                ThrowableUtil.processThrowable(e);
                return null;
            }
        }

        ArrayList<Transaction> transactionArrayList = new ArrayList<>();

        // Roughly split max transactions between each type (rounding is OK).
        int splitNum = shouldIncludeTokens(cryptoAddress) ? 3 : 2;
        int splitMax = getMaxTransactions()/splitNum;

        transactionArrayList.addAll(transactionNormalArrayList.subList(0, Math.min(splitMax, transactionNormalArrayList.size())));
        transactionArrayList.addAll(transactionInternalArrayList.subList(0, Math.min(splitMax, transactionInternalArrayList.size())));
        transactionArrayList.addAll(transactionTokenArrayList.subList(0, Math.min(splitMax, transactionTokenArrayList.size())));

        transactionNormalArrayList.subList(0, Math.min(splitMax, transactionNormalArrayList.size())).clear();
        transactionInternalArrayList.subList(0, Math.min(splitMax, transactionInternalArrayList.size())).clear();
        transactionTokenArrayList.subList(0, Math.min(splitMax, transactionTokenArrayList.size())).clear();

        while(transactionNormalArrayList.size() + transactionInternalArrayList.size() + transactionTokenArrayList.size() > 0) {
            if(transactionNormalArrayList.size() > 0) {
                transactionArrayList.add(transactionNormalArrayList.get(0));
                transactionNormalArrayList.remove(0);
            }
            if(transactionArrayList.size() == getMaxTransactions()) { break; }

            if(transactionInternalArrayList.size() > 0) {
                transactionArrayList.add(transactionInternalArrayList.get(0));
                transactionInternalArrayList.remove(0);
            }
            if(transactionArrayList.size() == getMaxTransactions()) { break; }

            if(transactionTokenArrayList.size() > 0) {
                transactionArrayList.add(transactionTokenArrayList.get(0));
                transactionTokenArrayList.remove(0);
            }
            if(transactionArrayList.size() == getMaxTransactions()) { break; }
        }

        return transactionArrayList;
    }
}

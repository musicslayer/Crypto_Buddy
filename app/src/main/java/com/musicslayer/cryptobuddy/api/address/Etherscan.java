package com.musicslayer.cryptobuddy.api.address;

import com.musicslayer.cryptobuddy.asset.crypto.coin.ETH;
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
import com.musicslayer.cryptobuddy.util.ThrowableUtil;
import com.musicslayer.cryptobuddy.util.RESTUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;

// Use Etherscan for transactions, but use Ethplorer for balance information so we can see token balances.
// Unfortunately, between the two APIs, display names of tokens are mismatched.

public class Etherscan extends AddressAPI {
    public final String APIKEY_etherscan = "ZHZ4Y7XKI9JD6XT8HV9HDZJMA8RHY7Y6DP";
    public final String APIKEY_ethplorer = "freekey";

    public String getName() { return "Etherscan"; }
    public String getDisplayName() { return "Etherscan & Ethplorer REST APIs"; }

    public boolean isSupported(CryptoAddress cryptoAddress) {
        return "ETH".equals(cryptoAddress.getCrypto().getName());
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
        String addressDataJSON = RESTUtil.get(baseURL + "/getAddressInfo/" + cryptoAddress.address + "?apiKey=" + APIKEY_ethplorer);
        if(addressDataJSON == null) {
            return null;
        }

        try {
            JSONObject json = new JSONObject(addressDataJSON);

            // ETH
            BigDecimal b = new BigDecimal(json.getJSONObject("ETH").getString("rawBalance"));
            b = b.movePointLeft(cryptoAddress.getCrypto().getScale());
            currentBalanceArrayList.add(new AssetQuantity(b.toPlainString(), new ETH()));

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

                        Token token = TokenManager.getTokenManagerFromKey("EthereumTokenManager").getOrCreateToken(key, name, display_name, scale, id);

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

        String addressDataJSON = RESTUtil.get(baseURL + "/api?module=account&action=balance&address=" + cryptoAddress.address + "&apikey=" + APIKEY_etherscan);
        if(addressDataJSON == null) {
            return null;
        }

        try {
            JSONObject json = new JSONObject(addressDataJSON);
            String amount = new BigDecimal(json.getString("result")).movePointLeft(cryptoAddress.getCrypto().getScale()).toPlainString();
            currentBalanceArrayList.add(new AssetQuantity(amount, new ETH()));
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            return null;
        }

        return currentBalanceArrayList;
    }

    public ArrayList<Transaction> getTransactions(CryptoAddress cryptoAddress) {
        ArrayList<Transaction> transactionArrayList = new ArrayList<>();

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
        String addressDataJSON = RESTUtil.get(baseURL + "/api?module=account&action=txlist&address=" + cryptoAddress.address + "&startblock=1&endblock=99999999&sort=asc&apikey=" + APIKEY_etherscan);

        // Internal Transactions - These are all ETH
        String addressDataInternalJSON = RESTUtil.get(baseURL + "/api?module=account&action=txlistinternal&address=" + cryptoAddress.address + "&startblock=1&endblock=99999999&sort=asc&apikey=" + APIKEY_etherscan);

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
                    BigInteger block_time = new BigInteger(o.getString("timeStamp"));
                    double block_time_d = block_time.doubleValue() * 1000;
                    block_time_date = new Date((long)block_time_d);
                }

                String from = o.getString("from");
                String to = o.getString("to");

                String action;
                BigDecimal fee;

                if(cryptoAddress.address.equalsIgnoreCase(from)) {
                    // We are sending crypto away.
                    action = "Send";

                    // We also have to add in the fee to the amount sent.
                    BigDecimal gasAmount = new BigDecimal(o.getString("gasUsed"));
                    BigDecimal gasPrice = new BigDecimal(o.getString("gasPrice"));
                    fee = gasAmount.multiply(gasPrice);
                }
                else if(cryptoAddress.address.equalsIgnoreCase(to)) {
                    // We are receiving crypto. No fee.
                    action = "Receive";
                    fee = BigDecimal.ZERO;
                }
                else {
                    // We shouldn't get here...
                    continue;
                }

                fee = fee.movePointLeft(cryptoAddress.getCrypto().getScale());

                if(fee.compareTo(BigDecimal.ZERO) > 0) {
                    transactionArrayList.add(new Transaction(new Action("Fee"), new AssetQuantity(fee.toPlainString(), cryptoAddress.getCrypto()), null, new Timestamp(block_time_date), "Transaction Fee"));
                    if(transactionArrayList.size() == getMaxTransactions()) { return transactionArrayList; }
                }

                // If I send something to myself, just reject it!
                if(from.equals(to)) { continue; }

                // If this has an error, skip the rest.
                if("1".equals(o.getString("isError"))) {
                    continue;
                }

                BigInteger balance_diff = new BigInteger(o.getString("value"));
                BigDecimal balance_diff_d = new BigDecimal(balance_diff);
                balance_diff_d = balance_diff_d.movePointLeft(cryptoAddress.getCrypto().getScale());
                String balance_diff_s = balance_diff_d.toPlainString();

                transactionArrayList.add(new Transaction(new Action(action), new AssetQuantity(balance_diff_s, cryptoAddress.getCrypto()), null, new Timestamp(block_time_date), "Transaction"));
                if(transactionArrayList.size() == getMaxTransactions()) { return transactionArrayList; }
            }

            // Internal
            JSONObject jsonInternal = new JSONObject(addressDataInternalJSON);
            JSONArray jsonInternalArray = jsonInternal.getJSONArray("result");

            for(int j = 0; j < jsonInternalArray.length(); j++) {
                JSONObject oI = jsonInternalArray.getJSONObject(j);

                // Internal transfers do not use confirmations.
                BigInteger block_time = new BigInteger(oI.getString("timeStamp"));
                double block_time_d = block_time.doubleValue() * 1000;
                Date block_time_date = new Date((long)block_time_d);

                String from = oI.getString("from");
                String to = oI.getString("to");

                String action;
                BigDecimal fee;

                if(cryptoAddress.address.equalsIgnoreCase(from)) {
                    // We are sending crypto away.
                    action = "Send";

                    // We also have to add in the fee to the amount sent.
                    BigDecimal gasAmount = new BigDecimal(oI.getString("gasUsed"));
                    BigDecimal gasPrice = new BigDecimal(oI.getString("gasPrice"));
                    fee = gasAmount.multiply(gasPrice);
                }
                else if(cryptoAddress.address.equalsIgnoreCase(to)) {
                    // We are receiving crypto. No fee.
                    action = "Receive";
                    fee = BigDecimal.ZERO;
                }
                else {
                    // We shouldn't get here...
                    continue;
                }

                fee = fee.movePointLeft(cryptoAddress.getCrypto().getScale());

                if(fee.compareTo(BigDecimal.ZERO) > 0) {
                    transactionArrayList.add(new Transaction(new Action("Fee"), new AssetQuantity(fee.toPlainString(), cryptoAddress.getCrypto()), null, new Timestamp(block_time_date), "Internal Transaction Fee"));
                    if(transactionArrayList.size() == getMaxTransactions()) { return transactionArrayList; }
                }

                // If I send something to myself, just reject it!
                if(from.equals(to)) { continue; }

                // If this has an error, skip the rest.
                if("1".equals(oI.getString("isError"))) {
                    continue;
                }

                BigInteger balance_diff = new BigInteger(oI.getString("value"));
                BigDecimal balance_diff_d = new BigDecimal(balance_diff);
                balance_diff_d = balance_diff_d.movePointLeft(cryptoAddress.getCrypto().getScale());
                String balance_diff_s = balance_diff_d.toPlainString();

                transactionArrayList.add(new Transaction(new Action(action), new AssetQuantity(balance_diff_s, cryptoAddress.getCrypto()), null, new Timestamp(block_time_date), "Internal Transaction"));
                if(transactionArrayList.size() == getMaxTransactions()) { return transactionArrayList; }
            }
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            return null;
        }

        if(shouldIncludeTokens(cryptoAddress)) {
            // ERC-20 Transactions - Various Tokens
            String addressDataTokenJSON = RESTUtil.get(baseURL + "/api?module=account&action=tokentx&address=" + cryptoAddress.address + "&startblock=1&endblock=99999999&sort=asc&apikey=" + APIKEY_etherscan);

            if(addressDataTokenJSON == null) {
                return null;
            }

            try {
                // Tokens
                JSONObject jsonToken = new JSONObject(addressDataTokenJSON);
                JSONArray jsonTokenArray = jsonToken.getJSONArray("result");

                for(int j = 0; j < jsonTokenArray.length(); j++) {
                    JSONObject oT = jsonTokenArray.getJSONObject(j);

                    // Token transactions don't have an error flag.

                    BigInteger balance_diff = new BigInteger(oT.getString("value"));

                    String from = oT.getString("from");
                    String to = oT.getString("to");

                    // If I send something to myself, just reject it!
                    if(from.equals(to)) { continue; }

                    String action;
                    if(cryptoAddress.address.equalsIgnoreCase(from)) {
                        // We are sending crypto away.
                        action = "Send";
                    }
                    else if(cryptoAddress.address.equalsIgnoreCase(to)) {
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
                        BigInteger block_time = new BigInteger(oT.getString("timeStamp"));
                        double block_time_d = block_time.doubleValue() * 1000;
                        block_time_date = new Date((long)block_time_d);
                    }

                    String name = oT.getString("tokenSymbol");
                    String display_name = oT.getString("tokenName");
                    int scale = tokenDecimal.intValue();
                    String key = oT.getString("contractAddress");
                    String id = key.toLowerCase();

                    Token token = TokenManager.getTokenManagerFromKey("EthereumTokenManager").getOrCreateToken(key, name, display_name, scale, id);

                    transactionArrayList.add(new Transaction(new Action(action), new AssetQuantity(balance_diff_s, token), null, new Timestamp(block_time_date), "Token Transaction"));
                    if(transactionArrayList.size() == getMaxTransactions()) { return transactionArrayList; }
                }
            }
            catch(Exception e) {
                ThrowableUtil.processThrowable(e);
                return null;
            }
        }

        return transactionArrayList;
    }
}

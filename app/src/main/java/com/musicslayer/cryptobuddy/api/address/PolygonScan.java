package com.musicslayer.cryptobuddy.api.address;

import com.musicslayer.cryptobuddy.asset.crypto.token.Token;
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

// TODO Include Proof of Stake Bridge.
// TODO Bridge could also carry tokens.

// PolygonScan has no pagination on its results.

public class PolygonScan extends AddressAPI {
    public final String APIKEY_polygonscan = "P1JCB9EWXNTQGAN4ZSC5Z3CF8444DC3BMH";

    public String getName() { return "PolygonScan"; }
    public String getDisplayName() { return "PolygonScan REST API"; }

    public boolean isSupported(CryptoAddress cryptoAddress) {
        return "MATIC".equals(cryptoAddress.getCrypto().getName());
    }

    public ArrayList<AssetQuantity> getCurrentBalance(CryptoAddress cryptoAddress) {
        ArrayList<AssetQuantity> currentBalanceArrayList = new ArrayList<>();

        String chainID;
        if(cryptoAddress.network.isMainnet()) {
            chainID = "137";
        }
        else {
            chainID = "80001";
        }

        String addressDataJSON = RESTUtil.get("https://api.covalenthq.com/v1/" + chainID + "/address/" + cryptoAddress.address + "/balances_v2/?key=ckey_65336bbeda304020862b0459dae");
        if(addressDataJSON == null) {
            return null;
        }

        try {
            JSONObject json = new JSONObject(addressDataJSON);
            JSONArray tokenArray = json.getJSONObject("data").getJSONArray("items");
            for(int i = 0; i < tokenArray.length(); i++) {
                JSONObject tokenData = tokenArray.getJSONObject(i);

                if("0x0000000000000000000000000000000000001010".equals(tokenData.getString("contract_address"))) {
                    // MATIC
                    BigDecimal value = new BigDecimal(tokenData.getString("balance"));
                    value = value.movePointLeft(cryptoAddress.getCrypto().getScale());
                    String amount = value.toPlainString();
                    currentBalanceArrayList.add(new AssetQuantity(amount, cryptoAddress.getCrypto()));
                }
                else {
                    // Token
                    String name = tokenData.getString("contract_ticker_symbol");
                    String display_name = tokenData.getString("contract_name");
                    int scale = tokenData.getInt("contract_decimals");
                    String key = tokenData.getString("contract_address");
                    String id = key.toLowerCase();

                    Token token = TokenManager.getTokenManagerFromKey("PolygonTokenManager").getOrCreateToken(key, name, display_name, scale, id);

                    BigDecimal value = new BigDecimal(tokenData.getString("balance"));
                    value = value.movePointLeft(scale);
                    String amount = value.toPlainString();

                    currentBalanceArrayList.add(new AssetQuantity(amount, token));
                }
            }
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
        ArrayList<Transaction> transactionPlasmaArrayList = new ArrayList<>();
        ArrayList<Transaction> transactionTokenArrayList = new ArrayList<>();

        String baseURL;
        if(cryptoAddress.network.isMainnet()) {
            baseURL = "https://api.polygonscan.com";
        }
        else {
            baseURL = "https://api-testnet.polygonscan.com";
        }

        // Normal Transactions - These are all ETH
        String addressDataJSON = RESTUtil.get(baseURL + "/api?module=account&action=txlist&address=" + cryptoAddress.address + "&startblock=1&endblock=99999999&sort=asc&apikey=" + APIKEY_polygonscan);

        // Internal Transactions - These are all ETH
        String addressDataInternalJSON = RESTUtil.get(baseURL + "/api?module=account&action=txlistinternal&address=" + cryptoAddress.address + "&startblock=1&endblock=99999999&sort=asc&apikey=" + APIKEY_polygonscan);

        // Use Etherscan to figure out how much MATIC was sent to the Plasma Bridge.
        // This is only for the mainnet.
        String addressDataPlasmaJSON;
        if(cryptoAddress.network.isMainnet()) {
            addressDataPlasmaJSON = RESTUtil.get("https://api.etherscan.io/api?module=account&action=tokentx&address=" + cryptoAddress.address + "&startblock=1&endblock=99999999&sort=asc&apikey=ZHZ4Y7XKI9JD6XT8HV9HDZJMA8RHY7Y6DP");
        }
        else {
            addressDataPlasmaJSON = "{}";
        }

        if(addressDataJSON == null || addressDataInternalJSON == null || addressDataPlasmaJSON == null) {
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
                    transactionNormalArrayList.add(new Transaction(new Action("Fee"), new AssetQuantity(fee.toPlainString(), cryptoAddress.getCrypto()), null, new Timestamp(block_time_date), "Transaction Fee"));
                    if(transactionNormalArrayList.size() == getMaxTransactions()) { break; }
                }

                // If I send something to myself, just reject it!
                if(from.equals(to)) { continue; }

                // If this has an error, skip it.
                if("1".equals(o.getString("isError"))) {
                    continue;
                }

                BigInteger balance_diff = new BigInteger(o.getString("value"));
                BigDecimal balance_diff_d = new BigDecimal(balance_diff);
                balance_diff_d = balance_diff_d.movePointLeft(cryptoAddress.getCrypto().getScale());
                String balance_diff_s = balance_diff_d.toPlainString();

                transactionNormalArrayList.add(new Transaction(new Action(action), new AssetQuantity(balance_diff_s, cryptoAddress.getCrypto()), null, new Timestamp(block_time_date), "Transaction"));
                if(transactionNormalArrayList.size() == getMaxTransactions()) { break; }
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

                // If I send something to myself, just reject it!
                if(from.equals(to)) { continue; }

                // If this has an error, skip it.
                if("1".equals(oI.getString("isError"))) {
                    continue;
                }

                fee = fee.movePointLeft(cryptoAddress.getCrypto().getScale());

                if(fee.compareTo(BigDecimal.ZERO) > 0) {
                    transactionInternalArrayList.add(new Transaction(new Action("Fee"), new AssetQuantity(fee.toPlainString(), cryptoAddress.getCrypto()), null, new Timestamp(block_time_date), "Internal Transaction Fee"));
                    if(transactionInternalArrayList.size() == getMaxTransactions()) { break; }
                }

                BigInteger balance_diff = new BigInteger(oI.getString("value"));
                BigDecimal balance_diff_d = new BigDecimal(balance_diff);
                balance_diff_d = balance_diff_d.movePointLeft(cryptoAddress.getCrypto().getScale());
                String balance_diff_s = balance_diff_d.toPlainString();

                transactionInternalArrayList.add(new Transaction(new Action(action), new AssetQuantity(balance_diff_s, cryptoAddress.getCrypto()), null, new Timestamp(block_time_date), "Internal Transaction"));
                if(transactionInternalArrayList.size() == getMaxTransactions()) { break; }
            }

            // Plasma
            if(cryptoAddress.network.isMainnet()) {
                JSONObject jsonPlasma = new JSONObject(addressDataPlasmaJSON);
                JSONArray jsonPlasmaArray = jsonPlasma.getJSONArray("result");
                for(int j = 0; j < jsonPlasmaArray.length(); j++) {
                    JSONObject oP = jsonPlasmaArray.getJSONObject(j);
                    String from = oP.getString("from");
                    String to = oP.getString("to");

                    // If I send something to myself, just reject it!
                    if(from.equals(to)) { continue; }

                    if(!(cryptoAddress.address.equalsIgnoreCase(from) && "0x401F6c983eA34274ec46f84D70b31C151321188b".equalsIgnoreCase(to))) {
                        continue;
                    }

                    Date block_time_date = null;

                    BigInteger confirmations = new BigInteger(oP.getString("confirmations"));
                    if(confirmations.compareTo(BigInteger.valueOf(0)) > 0) {
                        BigInteger block_time = new BigInteger(oP.getString("timeStamp"));
                        double block_time_d = block_time.doubleValue() * 1000;
                        block_time_date = new Date((long)block_time_d);
                    }

                    BigDecimal b = new BigDecimal(oP.getString("value"));
                    b = b.movePointLeft(cryptoAddress.getCrypto().getScale());
                    String amount = b.toPlainString();

                    // Assume this is a Receive of MATIC.
                    transactionPlasmaArrayList.add(new Transaction(new Action("Receive"), new AssetQuantity(amount, cryptoAddress.getCrypto()), null, new Timestamp(block_time_date),"Plasma Bridge Deposit"));
                    if(transactionPlasmaArrayList.size() == getMaxTransactions()) { break; }
                }
            }
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            return null;
        }

        if(shouldIncludeTokens(cryptoAddress)) {
            // ERC-20 Transactions - Various Tokens
            String addressDataTokenJSON = RESTUtil.get(baseURL + "/api?module=account&action=tokentx&address=" + cryptoAddress.address + "&startblock=1&endblock=99999999&sort=asc&apikey=" + APIKEY_polygonscan);

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

                    Token token = TokenManager.getTokenManagerFromKey("PolygonTokenManager").getOrCreateToken(key, name, display_name, scale, id);

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
        splitNum += cryptoAddress.network.isMainnet() ? 1 : 0;
        int splitMax = getMaxTransactions()/splitNum;

        transactionArrayList.addAll(transactionNormalArrayList.subList(0, Math.min(splitMax, transactionNormalArrayList.size())));
        transactionArrayList.addAll(transactionInternalArrayList.subList(0, Math.min(splitMax, transactionInternalArrayList.size())));
        transactionArrayList.addAll(transactionPlasmaArrayList.subList(0, Math.min(splitMax, transactionPlasmaArrayList.size())));
        transactionArrayList.addAll(transactionTokenArrayList.subList(0, Math.min(splitMax, transactionTokenArrayList.size())));

        transactionNormalArrayList.subList(0, Math.min(splitMax, transactionNormalArrayList.size())).clear();
        transactionInternalArrayList.subList(0, Math.min(splitMax, transactionInternalArrayList.size())).clear();
        transactionPlasmaArrayList.subList(0, Math.min(splitMax, transactionPlasmaArrayList.size())).clear();
        transactionTokenArrayList.subList(0, Math.min(splitMax, transactionTokenArrayList.size())).clear();

        while(transactionNormalArrayList.size() + transactionInternalArrayList.size() + transactionPlasmaArrayList.size() + transactionTokenArrayList.size() > 0) {
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

            if(transactionPlasmaArrayList.size() > 0) {
                transactionArrayList.add(transactionPlasmaArrayList.get(0));
                transactionPlasmaArrayList.remove(0);
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

package com.musicslayer.cryptobuddy.api.address;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.crypto.token.Token;
import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;
import com.musicslayer.cryptobuddy.transaction.Action;
import com.musicslayer.cryptobuddy.transaction.AssetQuantity;
import com.musicslayer.cryptobuddy.transaction.Timestamp;
import com.musicslayer.cryptobuddy.transaction.Transaction;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;
import com.musicslayer.cryptobuddy.util.WebUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

public class CardanoExplorer extends AddressAPI {
    public String getName() { return "CardanoExplorer"; }
    public String getDisplayName() { return "Cardano Explorer API"; }

    public boolean isSupported(CryptoAddress cryptoAddress) {
        return "ADA".equals(cryptoAddress.getPrimaryCoin().getName());
    }

    public ArrayList<AssetQuantity> getCurrentBalance(CryptoAddress cryptoAddress) {
        ArrayList<AssetQuantity> currentBalanceArrayList = new ArrayList<>();

        String baseURL;
        if(cryptoAddress.network.isMainnet()) {
            baseURL = "https://explorer.cardano.org/graphql";
        }
        else {
            baseURL = "https://explorer.cardano-testnet.iohkdev.io/graphql";
        }

        String body = "{" +
                "\"query\": \"query searchForPaymentAddress($address: String!) {\\n  transactions_aggregate(where: {_or: [{inputs: {address: {_eq: $address}}}, {outputs: {address: {_eq: $address}}}]}) {\\n    aggregate {\\n      count\\n    }\\n  }\\n  paymentAddresses(addresses: [$address]) {\\n    summary {\\n      assetBalances {\\n        asset {\\n          assetName\\n          decimals\\n          description\\n          fingerprint\\n          name\\n          policyId\\n          ticker\\n        }\\n        quantity\\n      }\\n    }\\n  }\\n}\\n\"," +
                "\"variables\": \"{\\\"address\\\": \\\"" + cryptoAddress.address + "\\\"}\"" +
                "}";
        String addressDataJSON = WebUtil.post(baseURL, body);

        if(addressDataJSON == null) {
            return null;
        }

        try {
            boolean hasNativeCoin = false;

            JSONObject json = new JSONObject(addressDataJSON);
            JSONArray balances = json.getJSONObject("data").getJSONArray("paymentAddresses").getJSONObject(0).getJSONObject("summary").getJSONArray("assetBalances");
            for(int i = 0; i < balances.length(); i++) {
                JSONObject balance = balances.getJSONObject(i);
                JSONObject asset = balance.getJSONObject("asset");

                Crypto crypto;

                String policyID = asset.getString("policyId");
                if("ada".equals(policyID)) {
                    crypto = cryptoAddress.getPrimaryCoin();
                    hasNativeCoin = true;
                }
                else {
                    if(!shouldIncludeTokens(cryptoAddress)) {
                        continue;
                    }

                    String symbol = asset.getString("assetName");

                    String id = policyID + symbol;
                    String key = id;
                    String name = new String(new BigInteger(symbol, 16).toByteArray());
                    String display_name = name;
                    int scale;
                    if("null".equals(asset.getString("decimals"))) {
                        scale = 0;
                    }
                    else {
                        scale = asset.getInt("decimals");
                    }

                    crypto = TokenManager.getTokenManagerFromKey("ADATokenManager").getToken(cryptoAddress, key, name, display_name, scale, id);
                }

                BigDecimal b = new BigDecimal(balance.getString("quantity"));
                b = b.movePointLeft(crypto.getScale());
                String amount = b.toPlainString();

                currentBalanceArrayList.add(new AssetQuantity(amount, crypto));
            }

            if(!hasNativeCoin) {
                // Always show a zero balance of the native coin.
                currentBalanceArrayList.add(new AssetQuantity("0", cryptoAddress.getPrimaryCoin()));
            }
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            return null;
        }

        return currentBalanceArrayList;
    }

    // There is no flag for transactions that failed/errored.

    public ArrayList<Transaction> getTransactions(CryptoAddress cryptoAddress) {
        ArrayList<Transaction> transactionArrayList = new ArrayList<>();

        String baseURL;
        if(cryptoAddress.network.isMainnet()) {
            baseURL = "https://explorer.cardano.org/graphql";
        }
        else {
            baseURL = "https://explorer.cardano-testnet.iohkdev.io/graphql";
        }

        for(int offset = 0; ; offset+=100) {
            String body = "{" +
                "\"query\": \"query getPaymentAddressTransactions($address: String!, $offset: Int!, $limit: Int!) {\\n  transactions(where: {_or: [{inputs: {address: {_eq: $address}}}, {outputs: {address: {_eq: $address}}}]}, offset: $offset, limit: $limit) {\\n    ...TransactionDetails\\n  }\\n}\\n\\nfragment TransactionDetails on Transaction {\\n  block {\\n    epochNo\\n    hash\\n    number\\n    slotNo\\n  }\\n  deposit\\n  fee\\n  hash\\n  includedAt\\n  mint {\\n    asset {\\n      assetName\\n      decimals\\n      description\\n      fingerprint\\n      name\\n      policyId\\n      ticker\\n    }\\n    quantity\\n  }\\n  inputs {\\n    address\\n    sourceTxHash\\n    sourceTxIndex\\n    value\\n    tokens {\\n      asset {\\n        assetName\\n        decimals\\n        description\\n        fingerprint\\n        name\\n        policyId\\n        ticker\\n      }\\n      quantity\\n    }\\n  }\\n  metadata {\\n    key\\n    value\\n  }\\n  outputs {\\n    address\\n    index\\n    value\\n    tokens {\\n      asset {\\n        assetName\\n        decimals\\n        description\\n        fingerprint\\n        name\\n        policyId\\n        ticker\\n      }\\n      quantity\\n    }\\n  }\\n  totalOutput\\n  withdrawals {\\n    address\\n    amount\\n  }\\n}\\n\"," +
                "\"variables\": \"{\\\"offset\\\":" + offset + ", \\\"limit\\\":100, \\\"address\\\": \\\"" + cryptoAddress.address + "\\\"}\"" +
                "}";

            String status = process(baseURL, body, cryptoAddress, transactionArrayList);

            if(ERROR.equals(status)) {
                return null;
            }
            else if(DONE.equals(status)) {
                break;
            }
        }

        return transactionArrayList;
    }

    // Return null for error/no data, DONE to stop and any other non-null string to keep going.
    private String process(String url, String body, CryptoAddress cryptoAddress, ArrayList<Transaction> transactionArrayList) {
        String addressDataJSON = WebUtil.post(url, body);
        if(addressDataJSON == null) {
            return ERROR;
        }

        try {
            String status = DONE;

            JSONObject jsonData = new JSONObject(addressDataJSON);
            JSONArray jsonArray = jsonData.getJSONObject("data").getJSONArray("transactions");
            for(int i = 0; i < jsonArray.length(); i++) {
                // If there is anything to process, we may not be done yet.
                status = NOTDONE;

                JSONObject tx = jsonArray.getJSONObject(i);

                String block_time = tx.getString("includedAt");

                DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
                format.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date block_time_date = format.parse(block_time);

                boolean fee_enabled = false;
                BigDecimal fee = new BigDecimal(tx.getString("fee"));
                fee = fee.movePointLeft(cryptoAddress.getFeeCoin().getScale());

                HashMap<Crypto, BigDecimal> amountMap = new HashMap<>();
                amountMap.put(cryptoAddress.getPrimaryCoin(), BigDecimal.ZERO);

                // Add all output amounts for our address (this includes withdrawn staking rewards)
                JSONArray outputs = tx.getJSONArray("outputs");
                for(int j = 0; j < outputs.length(); j++) {
                    JSONObject output = outputs.getJSONObject(j);

                    if(!cryptoAddress.matchesAddress(output.getString("address"))) {
                        continue;
                    }

                    // First part is always ADA.
                    BigDecimal amount = new BigDecimal(output.getString("value"));
                    amount = amount.movePointLeft(cryptoAddress.getPrimaryCoin().getScale());
                    BigDecimal newAmount = amountMap.get(cryptoAddress.getPrimaryCoin()).add(amount);
                    amountMap.put(cryptoAddress.getPrimaryCoin(), newAmount);

                    if(shouldIncludeTokens(cryptoAddress)) {
                        // Tokens
                        JSONArray tokens = output.getJSONArray("tokens");
                        for(int k = 0; k < tokens.length(); k++) {
                            JSONObject tokenData = tokens.getJSONObject(k);
                            JSONObject asset = tokenData.getJSONObject("asset");

                            String symbol = asset.getString("assetName");

                            String id = asset.getString("policyId") + symbol;
                            String key = id;
                            String name = new String(new BigInteger(symbol, 16).toByteArray());
                            String display_name = name;
                            int scale;
                            if("null".equals(asset.getString("decimals"))) {
                                scale = 0;
                            }
                            else {
                                scale = asset.getInt("decimals");
                            }

                            Token token = TokenManager.getTokenManagerFromKey("ADATokenManager").getToken(cryptoAddress, key, name, display_name, scale, id);

                            if(!amountMap.containsKey(token)) {
                                amountMap.put(token, BigDecimal.ZERO);
                            }

                            BigDecimal tokenAmount = new BigDecimal(tokenData.getString("quantity"));
                            tokenAmount = tokenAmount.movePointLeft(token.getScale());
                            BigDecimal newTokenAmount = amountMap.get(token).add(tokenAmount);
                            amountMap.put(token, newTokenAmount);
                        }
                    }
                }

                // Subtract all input amounts for our address
                JSONArray inputs = tx.getJSONArray("inputs");
                for(int j = 0; j < inputs.length(); j++) {
                    JSONObject input = inputs.getJSONObject(j);

                    if(!cryptoAddress.matchesAddress(input.getString("address"))) {
                        continue;
                    }

                    fee_enabled = true;

                    // First part is always ADA.
                    BigDecimal amount = new BigDecimal(input.getString("value"));
                    amount = amount.movePointLeft(cryptoAddress.getPrimaryCoin().getScale());

                    BigDecimal newAmount = amountMap.get(cryptoAddress.getPrimaryCoin()).subtract(amount);
                    amountMap.put(cryptoAddress.getPrimaryCoin(), newAmount);

                    if(shouldIncludeTokens(cryptoAddress)) {
                        // Tokens
                        JSONArray tokens = input.getJSONArray("tokens");
                        for(int k = 0; k < tokens.length(); k++) {
                            JSONObject tokenData = tokens.getJSONObject(k);
                            JSONObject asset = tokenData.getJSONObject("asset");

                            String symbol = asset.getString("assetName");

                            String id = asset.getString("policyId") + symbol;
                            String key = id;
                            String name = new String(new BigInteger(symbol, 16).toByteArray());
                            String display_name = name;
                            int scale;
                            if("null".equals(asset.getString("decimals"))) {
                                scale = 0;
                            }
                            else {
                                scale = asset.getInt("decimals");
                            }

                            Token token = TokenManager.getTokenManagerFromKey("ADATokenManager").getToken(cryptoAddress, key, name, display_name, scale, id);

                            if(!amountMap.containsKey(token)) {
                                amountMap.put(token, BigDecimal.ZERO);
                            }

                            BigDecimal tokenAmount = new BigDecimal(tokenData.getString("quantity"));
                            tokenAmount = tokenAmount.movePointLeft(token.getScale());
                            BigDecimal newTokenAmount = amountMap.get(token).subtract(tokenAmount);
                            amountMap.put(token, newTokenAmount);
                        }
                    }
                }

                // Add in fee. We need to subtract this from the amount if we sent something.
                if(fee_enabled && fee.compareTo(BigDecimal.ZERO) > 0) {
                    // The amount of ADA includes the fee, but we wish to count that separately.
                    BigDecimal amount = amountMap.get(cryptoAddress.getPrimaryCoin());
                    amount = amount.add(fee);
                    amountMap.put(cryptoAddress.getPrimaryCoin(), amount);

                    transactionArrayList.add(new Transaction(new Action("Fee"), new AssetQuantity(fee.toPlainString(), cryptoAddress.getFeeCoin()), null, new Timestamp(block_time_date), "Transaction Fee"));
                    if(transactionArrayList.size() == getMaxTransactions()) { return DONE; }
                }

                // Resolve the map to add all transactions.
                for(Crypto crypto : amountMap.keySet()) {
                    BigDecimal amount = amountMap.get(crypto);

                    String action;
                    if(amount.compareTo(BigDecimal.ZERO) < 0) {
                        action = "Send";
                        amount = amount.negate();
                    }
                    else if(amount.compareTo(BigDecimal.ZERO) > 0) {
                        action = "Receive";
                    }
                    else {
                        // Don't include anything that perfectly cancels out.
                        continue;
                    }

                    transactionArrayList.add(new Transaction(new Action(action), new AssetQuantity(amount.toPlainString(), crypto), null, new Timestamp(block_time_date), "Transaction"));
                    if(transactionArrayList.size() == getMaxTransactions()) { return DONE; }
                }
            }

            return status;
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            return ERROR;
        }
    }
}

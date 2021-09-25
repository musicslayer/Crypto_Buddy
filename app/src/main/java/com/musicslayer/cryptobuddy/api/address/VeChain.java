package com.musicslayer.cryptobuddy.api.address;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.crypto.coin.VET;
import com.musicslayer.cryptobuddy.asset.crypto.coin.VTHO;
import com.musicslayer.cryptobuddy.asset.crypto.token.Token;
import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;
import com.musicslayer.cryptobuddy.transaction.Action;
import com.musicslayer.cryptobuddy.transaction.AssetQuantity;
import com.musicslayer.cryptobuddy.transaction.Timestamp;
import com.musicslayer.cryptobuddy.transaction.Transaction;
import com.musicslayer.cryptobuddy.util.Exception;
import com.musicslayer.cryptobuddy.util.REST;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;

// Energy/Gas on this blockchain is VeThor.

// Each VET will create 0.00000005 VTHO with every block that is created
// (a new block gets added to the chain 10 seconds).
// This translates to a generation rate of 0.000432 VTHO generated per VET per day.

public class VeChain extends AddressAPI {
    public String getName() { return "VeChain"; }
    public String getDisplayName() { return "VeChain Explorer API"; }

    public boolean isSupported(CryptoAddress cryptoAddress) {
        return "VET".equals(cryptoAddress.getCrypto().getName());
    }

    public ArrayList<AssetQuantity> getCurrentBalance(CryptoAddress cryptoAddress) {
        ArrayList<AssetQuantity> currentBalanceArrayList = new ArrayList<>();

        String baseURL;
        if(cryptoAddress.network.isMainnet()) {
            baseURL = "https://explore.vechain.org";
        }
        else {
            baseURL = "https://explore-testnet.vechain.org";
        }

        String addressDataJSON = REST.get(baseURL + "/api/accounts/" + cryptoAddress.address.toLowerCase());
        if(addressDataJSON == null) {
            return null;
        }

        try {
            JSONObject json = new JSONObject(addressDataJSON);
            JSONObject account = json.getJSONObject("account");

            // Values are in HEX and in WEI and need to be converted.

            // VET and VTHO
            Crypto crypto_main = new VET();
            String vetHexBalance = account.getString("balance");
            BigDecimal b = new BigDecimal(new BigInteger(vetHexBalance.substring(2), 16).toString());
            b = b.movePointLeft(crypto_main.getScale());
            currentBalanceArrayList.add(new AssetQuantity(b.toString(), crypto_main));

            Crypto crypto_energy = new VTHO();
            String vethorHexBalance = account.getString("energy");
            BigDecimal b2 = new BigDecimal(new BigInteger(vethorHexBalance.substring(2), 16).toString());
            b2 = b2.movePointLeft(crypto_energy.getScale());
            currentBalanceArrayList.add(new AssetQuantity(b2.toString(), crypto_energy));

            if(shouldIncludeTokens(cryptoAddress)) {
                // Tokens
                JSONArray tokenArray = json.getJSONArray("tokens");
                for(int i = 0; i < tokenArray.length(); i++) {
                    JSONObject tokenData = tokenArray.getJSONObject(i);

                    Token token = TokenManager.getTokenManagerFromKey("VETTokenManager").getToken(tokenData.getString("symbol"), "?", "?", 18, "?");

                    String tokenHexBalance = tokenData.getString("balance");
                    BigDecimal bT = new BigDecimal(new BigInteger(tokenHexBalance.substring(2), 16).toString());
                    bT = bT.movePointLeft(token.getScale());
                    currentBalanceArrayList.add(new AssetQuantity(bT.toString(), token));
                }
            }
        }
        catch(java.lang.Exception e) {
            Exception.processException(e);
            return null;
        }

        return currentBalanceArrayList;
    }

    // There is no flag for transactions that failed/errored.

    public ArrayList<Transaction> getTransactions(CryptoAddress cryptoAddress) {
        ArrayList<Transaction> transactionArrayList = new ArrayList<>();

        String baseURL;
        if(cryptoAddress.network.isMainnet()) {
            baseURL = "https://explore.vechain.org";
        }
        else {
            baseURL = "https://explore-testnet.vechain.org";
        }

        // Transfers (VET, VTHO, and Tokens)
        String addressDataJSONTransfers = REST.get(baseURL + "/api/accounts/" + cryptoAddress.address.toLowerCase() + "/transfers?limit=50");
        String addressDataJSONTransfers2 = REST.get(baseURL + "/api/accounts/" + cryptoAddress.address.toLowerCase() + "/transfers?limit=50&offset=50");

        if(addressDataJSONTransfers == null || addressDataJSONTransfers2 == null) {
            return null;
        }

        try {
            JSONObject jsonAddress3 = new JSONObject(addressDataJSONTransfers);
            JSONArray transfers = jsonAddress3.getJSONArray("transfers");
            for(int j = 0; j < transfers.length(); j++)
            {
                JSONObject o = transfers.getJSONObject(j);

                JSONObject meta = o.getJSONObject("meta");
                BigInteger block_time = new BigInteger(meta.getString("blockTimestamp"));
                double block_time_d = block_time.doubleValue() * 1000;
                Date block_time_date = new Date((long)block_time_d);

                String from = o.getString("sender");
                String to = o.getString("recipient");

                String action;
                if(cryptoAddress.address.equalsIgnoreCase(from)) {
                    action = "Send";

                    // Get fee (in VeThor)
                    String txID = o.getString("txID");
                    String transactionJSON = REST.get(baseURL + "/api/transactions/" + txID);
                    JSONObject transactionData = new JSONObject(transactionJSON);

                    BigDecimal fee = new BigDecimal(new BigInteger(transactionData.getJSONObject("receipt").getString("paid").substring(2), 16).toString());
                    fee = fee.movePointLeft(new VTHO().getScale());
                    if(fee.compareTo(BigDecimal.ZERO) > 0) {
                        transactionArrayList.add(new Transaction(new Action("Fee"), new AssetQuantity(fee.toPlainString(), new VTHO()), null, new Timestamp(block_time_date),"Transaction Fee"));
                        if(transactionArrayList.size() == getMaxTransactions()) { return transactionArrayList; }
                    }
                }
                else if(cryptoAddress.address.equalsIgnoreCase(to)) {
                    action = "Receive";
                }
                else {
                    // Assume there is nothing to process here.
                    continue;
                }

                // If I send something to myself, just reject it!
                if(from.equals(to)) { continue; }

                Crypto crypto;

                String symbol = o.getString("symbol");
                if("VET".equals(symbol)) {
                    crypto = new VET();
                }
                else if("VTHO".equals(symbol)) {
                    // Treat VeThor like a coin.
                    crypto = new VTHO();
                }
                else {
                    if(!shouldIncludeTokens(cryptoAddress)) {
                        continue;
                    }

                    crypto = TokenManager.getTokenManagerFromKey("VETTokenManager").getToken(symbol, "?", "?", 18, "?");
                }

                // Value is in HEX and in WEI and needs to be converted.
                String hexBalance = o.getString("amount");
                BigDecimal b = new BigDecimal(new BigInteger(hexBalance.substring(2), 16).toString());
                b = b.movePointLeft(crypto.getScale());
                String currentBalance = b.toString();

                transactionArrayList.add(new Transaction(new Action(action), new AssetQuantity(currentBalance, crypto), null, new Timestamp(block_time_date),"Transaction"));
                if(transactionArrayList.size() == getMaxTransactions()) { return transactionArrayList; }
            }

            JSONObject jsonAddress4 = new JSONObject(addressDataJSONTransfers2);
            JSONArray transfers2 = jsonAddress4.getJSONArray("transfers");
            for(int j = 0; j < transfers2.length(); j++)
            {
                JSONObject o = transfers2.getJSONObject(j);

                JSONObject meta = o.getJSONObject("meta");
                BigInteger block_time = new BigInteger(meta.getString("blockTimestamp"));
                double block_time_d = block_time.doubleValue() * 1000;
                Date block_time_date = new Date((long)block_time_d);

                String from = o.getString("sender");
                String to = o.getString("recipient");

                String action;
                if(cryptoAddress.address.equalsIgnoreCase(from)) {
                    action = "Send";

                    // Get fee (in VeThor)
                    String txID = o.getString("txID");
                    String transactionJSON = REST.get(baseURL + "/api/transactions/" + txID);
                    JSONObject transactionData = new JSONObject(transactionJSON);

                    BigDecimal fee = new BigDecimal(new BigInteger(transactionData.getJSONObject("receipt").getString("paid").substring(2), 16).toString());
                    fee = fee.movePointLeft(new VTHO().getScale());
                    if(fee.compareTo(BigDecimal.ZERO) > 0) {
                        transactionArrayList.add(new Transaction(new Action("Fee"), new AssetQuantity(fee.toPlainString(), new VTHO()), null, new Timestamp(block_time_date),"Transaction Fee"));
                        if(transactionArrayList.size() == getMaxTransactions()) { return transactionArrayList; }
                    }
                }
                else if(cryptoAddress.address.equalsIgnoreCase(to)) {
                    action = "Receive";
                }
                else {
                    // Assume there is nothing to process here.
                    continue;
                }

                // If I send something to myself, just reject it!
                if(from.equals(to)) { continue; }

                Crypto crypto;

                String symbol = o.getString("symbol");
                if("VET".equals(symbol)) {
                    crypto = new VET();
                }
                else if("VTHO".equals(symbol)) {
                    // Treat VeThor like a coin.
                    crypto = new VTHO();
                }
                else {
                    if(!shouldIncludeTokens(cryptoAddress)) {
                        continue;
                    }

                    crypto = TokenManager.getTokenManagerFromKey("VETTokenManager").getToken(symbol, "?", "?", 18, "?");
                }

                // Value is in HEX and in WEI and needs to be converted.
                String hexBalance = o.getString("amount");
                BigDecimal b = new BigDecimal(new BigInteger(hexBalance.substring(2), 16).toString());
                b = b.movePointLeft(crypto.getScale());
                String currentBalance = b.toString();

                transactionArrayList.add(new Transaction(new Action(action), new AssetQuantity(currentBalance, crypto), null, new Timestamp(block_time_date),"Transaction"));
                if(transactionArrayList.size() == getMaxTransactions()) { return transactionArrayList; }
            }
        }
        catch(java.lang.Exception e) {
            Exception.processException(e);
            return null;
        }

        return transactionArrayList;
    }
}

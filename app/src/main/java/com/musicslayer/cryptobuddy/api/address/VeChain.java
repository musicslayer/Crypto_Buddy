package com.musicslayer.cryptobuddy.api.address;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.crypto.token.Token;
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

// Energy/Gas on this blockchain is VeThor.

// Each VET will create 0.00000005 VTHO with every block that is created
// (a new block gets added to the chain 10 seconds).
// This translates to a generation rate of 0.000432 VTHO generated per VET per day.

public class VeChain extends AddressAPI {
    public String getName() { return "VeChain"; }
    public String getDisplayName() { return "VeChain Explorer API"; }

    public boolean isSupported(CryptoAddress cryptoAddress) {
        return "VET".equals(cryptoAddress.getPrimaryCoin().getKey());
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

        String addressDataJSON = WebUtil.get(baseURL + "/api/accounts/" + cryptoAddress.address.toLowerCase());
        if(addressDataJSON == null) {
            return null;
        }

        try {
            JSONObject json = new JSONObject(addressDataJSON);
            JSONObject account = json.getJSONObject("account");

            // Values are in HEX and in WEI and need to be converted.

            // VET and VTHO
            Crypto crypto_main = cryptoAddress.getPrimaryCoin();
            String vetHexBalance = account.getString("balance");
            BigDecimal b = new BigDecimal(new BigInteger(vetHexBalance.substring(2), 16).toString());
            b = b.movePointLeft(crypto_main.getScale());
            currentBalanceArrayList.add(new AssetQuantity(b.toString(), crypto_main));

            Crypto crypto_energy = cryptoAddress.getFeeCoin();
            String vethorHexBalance = account.getString("energy");
            BigDecimal b2 = new BigDecimal(new BigInteger(vethorHexBalance.substring(2), 16).toString());
            b2 = b2.movePointLeft(crypto_energy.getScale());
            currentBalanceArrayList.add(new AssetQuantity(b2.toString(), crypto_energy));

            if(shouldIncludeTokens(cryptoAddress)) {
                // Tokens
                JSONArray tokenArray = json.getJSONArray("tokens");
                for(int i = 0; i < tokenArray.length(); i++) {
                    JSONObject tokenData = tokenArray.getJSONObject(i);

                    Token token = TokenManager.getTokenManagerFromKey("VETTokenManager").getToken(cryptoAddress, tokenData.getString("symbol"), null, null, 18, null);

                    String tokenHexBalance = tokenData.getString("balance");
                    BigDecimal bT = new BigDecimal(new BigInteger(tokenHexBalance.substring(2), 16).toString());
                    bT = bT.movePointLeft(token.getScale());
                    currentBalanceArrayList.add(new AssetQuantity(bT.toString(), token));
                }
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
            baseURL = "https://explore.vechain.org";
        }
        else {
            baseURL = "https://explore-testnet.vechain.org";
        }

        for(int offset = 0; ; offset += 50) {
            String url = baseURL + "/api/accounts/" + cryptoAddress.address.toLowerCase() + "/transfers?limit=50&offset=" + offset;
            String status = processTransfers(url, cryptoAddress, transactionArrayList);

            if(ERROR.equals(status)) {
                return null;
            }
            else if(DONE.equals(status)) {
                break;
            }
        }

        return transactionArrayList;
    }

    public String processTransfers(String url, CryptoAddress cryptoAddress, ArrayList<Transaction> transactionArrayList) {
        // Transfers (VET, VTHO, and Tokens)
        String addressDataJSONTransfers = WebUtil.get(url);
        if(addressDataJSONTransfers == null) {
            return ERROR;
        }

        String baseURL;
        if(cryptoAddress.network.isMainnet()) {
            baseURL = "https://explore.vechain.org";
        }
        else {
            baseURL = "https://explore-testnet.vechain.org";
        }

        try {
            String status = DONE;

            JSONObject jsonAddress3 = new JSONObject(addressDataJSONTransfers);
            JSONArray transfers = jsonAddress3.getJSONArray("transfers");
            for(int j = 0; j < transfers.length(); j++)
            {
                // If there is anything to process, we may not be done yet.
                status = NOTDONE;

                JSONObject o = transfers.getJSONObject(j);

                JSONObject meta = o.getJSONObject("meta");

                String block_time = meta.getString("blockTimestamp");
                Date block_time_date = DateTimeUtil.parseSeconds(block_time);

                String from = o.getString("sender");
                String to = o.getString("recipient");

                String action;
                if(cryptoAddress.matchesAddress(from)) {
                    action = "Send";

                    // Get fee (in VeThor)
                    String txID = o.getString("txID");
                    String transactionJSON = WebUtil.get(baseURL + "/api/transactions/" + txID);
                    JSONObject transactionData = new JSONObject(transactionJSON);

                    BigDecimal fee = new BigDecimal(new BigInteger(transactionData.getJSONObject("receipt").getString("paid").substring(2), 16).toString());
                    fee = fee.movePointLeft(cryptoAddress.getFeeCoin().getScale());
                    if(fee.compareTo(BigDecimal.ZERO) > 0) {
                        transactionArrayList.add(new Transaction(new Action("Fee"), new AssetQuantity(fee.toPlainString(), cryptoAddress.getFeeCoin()), null, new Timestamp(block_time_date),"Transaction Fee"));
                        if(transactionArrayList.size() == getMaxTransactions()) { return DONE; }
                    }
                }
                else if(cryptoAddress.matchesAddress(to)) {
                    action = "Receive";
                }
                else {
                    // Assume there is nothing to process here.
                    continue;
                }

                // If I send something to myself, just reject it!
                if(cryptoAddress.network.matchesAddress(from, to)) { continue; }

                Crypto crypto;

                String symbol = o.getString("symbol");
                if("VET".equals(symbol)) {
                    crypto = cryptoAddress.getPrimaryCoin();
                }
                else if("VTHO".equals(symbol)) {
                    // Treat VeThor like a coin.
                    crypto = cryptoAddress.getFeeCoin();
                }
                else {
                    if(!shouldIncludeTokens(cryptoAddress)) {
                        continue;
                    }

                    crypto = TokenManager.getTokenManagerFromKey("VETTokenManager").getToken(cryptoAddress, symbol, null, null, 18, null);
                }

                // Value is in HEX and in WEI and needs to be converted.
                String hexBalance = o.getString("amount");
                BigDecimal b = new BigDecimal(new BigInteger(hexBalance.substring(2), 16).toString());
                b = b.movePointLeft(crypto.getScale());
                String currentBalance = b.toString();

                transactionArrayList.add(new Transaction(new Action(action), new AssetQuantity(currentBalance, crypto), null, new Timestamp(block_time_date),"Transaction"));
                if(transactionArrayList.size() == getMaxTransactions()) { return DONE; }
            }

            return status;
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            return ERROR;
        }
    }
}

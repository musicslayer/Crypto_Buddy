package com.musicslayer.cryptobuddy.api.address;

import com.musicslayer.cryptobuddy.asset.crypto.coin.SOL;
import com.musicslayer.cryptobuddy.asset.crypto.token.Token;
import com.musicslayer.cryptobuddy.asset.crypto.token.UnknownToken;
import com.musicslayer.cryptobuddy.asset.network.SOL_Devnet;
import com.musicslayer.cryptobuddy.asset.network.SOL_Testnet;
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
import java.util.ArrayList;
import java.util.Date;

// Alternatives:
// Solana Beach
// Mainnet Only
// https://prod-api.solana.surf/v1/account/zPHwJvSkp6XcqjuWZcbUrDKcMHaUcwcFCLbToRHMEXv/transactions?
// https://prod-api.solana.surf/v1/account/zPHwJvSkp6XcqjuWZcbUrDKcMHaUcwcFCLbToRHMEXv?
// https://prod-api.solana.surf/v1/account/zPHwJvSkp6XcqjuWZcbUrDKcMHaUcwcFCLbToRHMEXv/tokens?

// Solscan
// Mainnet
// https://api.solscan.io/account?address=zPHwJvSkp6XcqjuWZcbUrDKcMHaUcwcFCLbToRHMEXv
// https://api.solscan.io/account/tokens?address=zPHwJvSkp6XcqjuWZcbUrDKcMHaUcwcFCLbToRHMEXv
// https://api.solscan.io/account/transaction?address=zPHwJvSkp6XcqjuWZcbUrDKcMHaUcwcFCLbToRHMEXv
// https://api.solscan.io/account/token/txs?address=zPHwJvSkp6XcqjuWZcbUrDKcMHaUcwcFCLbToRHMEXv&offset=10&limit=10
// https://api.solscan.io/account/stake?address=zPHwJvSkp6XcqjuWZcbUrDKcMHaUcwcFCLbToRHMEXv
// https://api.solscan.io/transaction?tx=DCSFBktYM3vAo8ypBcdiPrU3z8ebCDciLdcSK6ydkBnin1vEAmJ2YjPMDnQUQVRLBn52pAgvKS3dBe1N5roYHqM");
// https://api.solscan.io/transaction?tx=DCSFBktYM3vAo8ypBcdiPrU3z8ebCDciLdcSK6ydkBnin1vEAmJ2YjPMDnQUQVRLBn52pAgvKS3dBe1N5roYHqM&before=DxmwHS9LB5oQJgFX7T3aeUz4YVTWUZqKYNjbzoPCor9srg3kTAVfR8fG5g6Mw5XphYHKee4AhKAcS8ABGtEtyfY");

// https://api-testnet.solscan.io
// https://api-devnet.solscan.io

public class Solscan extends AddressAPI {
    public String getName() { return "Solscan"; }
    public String getDisplayName() { return "Solscan API"; }

    public boolean isSupported(CryptoAddress cryptoAddress) {
        return "SOL".equals(cryptoAddress.getCrypto().getName());
    }

    public ArrayList<AssetQuantity> getCurrentBalance(CryptoAddress cryptoAddress) {
        ArrayList<AssetQuantity> currentBalanceArrayList = new ArrayList<>();

        String baseURL;
        if(cryptoAddress.network.isMainnet()) {
            baseURL = "https://api.solscan.io";
        }
        else if(cryptoAddress.network instanceof SOL_Testnet) {
            baseURL = "https://api-testnet.solscan.io";
        }
        else if(cryptoAddress.network instanceof SOL_Devnet) {
            baseURL = "https://api-devnet.solscan.io";
        }
        else {
            return null;
        }

        String addressDataJSON = REST.get(baseURL + "/account?address=" + cryptoAddress.address);
        if(addressDataJSON == null) {
            return null;
        }

        try {
            // SOL
            JSONObject json = new JSONObject(addressDataJSON);
            JSONObject data = json.getJSONObject("data");

            String currentBalance;
            if(data.has("lamports")) {
                BigDecimal balance = new BigDecimal(data.getString("lamports"));
                balance = balance.movePointLeft(cryptoAddress.getCrypto().getScale());
                currentBalance = balance.toPlainString();
            }
            else {
                currentBalance = "0";
            }

            currentBalanceArrayList.add(new AssetQuantity(currentBalance, new SOL()));
        }
        catch(java.lang.Exception e) {
            Exception.processException(e);
            return null;
        }

        if(shouldIncludeTokens(cryptoAddress)) {
            String addressTokenDataJSON = REST.get(baseURL + "/account/tokens?address=" + cryptoAddress.address);
            if(addressTokenDataJSON == null) {
                return null;
            }

            try {
                // Tokens
                JSONObject jsonToken = new JSONObject(addressTokenDataJSON);
                JSONArray jsonTokenArray = jsonToken.getJSONArray("data");
                for(int i = 0; i < jsonTokenArray.length(); i++) {
                    JSONObject tokenData = jsonTokenArray.getJSONObject(i);
                    JSONObject tokenAmount = tokenData.getJSONObject("tokenAmount");

                    int scale = tokenAmount.getInt("decimals");

                    Token token;
                    if(tokenData.has("tokenSymbol")) {
                        String name = tokenData.getString("tokenSymbol");
                        String display_name = tokenData.getString("tokenName");
                        String id = tokenData.getString("tokenAddress");
                        String key = id;
                        token = TokenManager.getTokenManagerFromKey("SPLTokenManager").getOrCreateToken(key, name, display_name, scale, id);
                    }
                    else {
                        token = UnknownToken.createUnknownToken(null, null, null, scale, null, null, "SPL");
                    }

                    BigDecimal b = new BigDecimal(tokenAmount.getString("amount"));
                    b = b.movePointLeft(token.getScale());
                    String amount = b.toPlainString();

                    currentBalanceArrayList.add(new AssetQuantity(amount, token));
                }
            }
            catch(java.lang.Exception e) {
                Exception.processException(e);
                return null;
            }
        }

        return currentBalanceArrayList;
    }

    public ArrayList<Transaction> getTransactions(CryptoAddress cryptoAddress) {
        ArrayList<Transaction> transactionArrayList = new ArrayList<>();

        String baseURL;
        if(cryptoAddress.network.isMainnet()) {
            baseURL = "https://api.solscan.io";
        }
        else if(cryptoAddress.network instanceof SOL_Testnet) {
            baseURL = "https://api-testnet.solscan.io";
        }
        else if(cryptoAddress.network instanceof SOL_Devnet) {
            baseURL = "https://api-devnet.solscan.io";
        }
        else {
            return null;
        }

        String addressDataJSON = REST.get(baseURL + "/account/transaction?address=" + cryptoAddress.address);
        if(addressDataJSON == null) {
            return null;
        }

        try {
            // SOL
            JSONObject json = new JSONObject(addressDataJSON);
            JSONArray jsonData = json.getJSONArray("data");
            for(int i = 0; i < jsonData.length(); i++) {
                JSONObject o = jsonData.getJSONObject(i);

                BigDecimal block_time = new BigDecimal(o.getString("blockTime"));
                block_time = block_time.multiply(new BigDecimal(1000));
                Date block_time_date = new Date(block_time.longValue());

                BigDecimal fee = new BigDecimal(o.getString("fee"));
                fee = fee.movePointLeft(cryptoAddress.getCrypto().getScale());
                if(fee.compareTo(BigDecimal.ZERO) > 0) {
                    transactionArrayList.add(new Transaction(new Action("Fee"), new AssetQuantity(fee.toPlainString(), cryptoAddress.getCrypto()), null, new Timestamp(block_time_date),"Transaction Fee"));
                    if(transactionArrayList.size() == getMaxTransactions()) { return transactionArrayList; }
                }

                // End here if the transaction has errored.
                if("Fail".equals(o.getString("status"))) {
                    continue;
                }

                // Look for SOL transfers.
                boolean isTransfer = false;
                JSONArray parsedInstruction = o.getJSONArray("parsedInstruction");
                for(int j = 0; j < parsedInstruction.length(); j++) {
                    JSONObject oP = parsedInstruction.getJSONObject(j);
                    if("sol-transfer".equals(oP.getString("type"))) {
                        isTransfer = true;
                        break;
                    }
                }

                if(isTransfer) {
                    String transactionJSON = REST.get(baseURL + "/transaction?tx=" + o.getString("txHash"));
                    JSONObject transactionData = new JSONObject(transactionJSON);
                    JSONArray sol_transfer_txs = transactionData.getJSONArray("sol_transfer_txs");
                    for(int j = 0; j < sol_transfer_txs.length(); j++) {
                        JSONObject oT = sol_transfer_txs.getJSONObject(j);

                        BigDecimal b = new BigDecimal(oT.getString("amount"));
                        b = b.movePointLeft(cryptoAddress.getCrypto().getScale());
                        String amount = b.toPlainString();

                        String from = oT.getString("source");
                        String to = oT.getString("destination");

                        // If I send something to myself, just reject it!
                        if(from.equals(to)) { continue; }

                        String action;
                        if(cryptoAddress.address.equalsIgnoreCase(to)) {
                            action = "Receive";
                        }
                        else if(cryptoAddress.address.equalsIgnoreCase(from)) {
                            action = "Send";
                        }
                        else {
                            continue;
                        }

                        transactionArrayList.add(new Transaction(new Action(action), new AssetQuantity(amount, cryptoAddress.getCrypto()), null, new Timestamp(block_time_date),"Transaction"));
                        if(transactionArrayList.size() == getMaxTransactions()) { return transactionArrayList; }
                    }
                }
            }

            if(shouldIncludeTokens(cryptoAddress)) {
                // Token JSON may be null if there are no token transfers.

                // Tokens
                String addressDataTokenJSON = REST.get(baseURL + "/account/token/txs?address=" + cryptoAddress.address + "&limit=10"); // &offset
                if(addressDataTokenJSON != null) {
                    JSONObject jsonToken = new JSONObject(addressDataTokenJSON);
                    JSONArray jsonTokenData = jsonToken.getJSONObject("data").getJSONObject("tx").getJSONArray("transactions");
                    for(int i = 0; i < jsonTokenData.length(); i++) {
                        JSONObject o = jsonTokenData.getJSONObject(i);

                        // End here if the transaction has errored.
                        if("Fail".equals(o.getString("status"))) {
                            continue;
                        }

                        BigDecimal block_time = new BigDecimal(o.getString("blockTime"));
                        block_time = block_time.multiply(new BigDecimal(1000));
                        Date block_time_date = new Date(block_time.longValue());

                        JSONObject change = o.getJSONObject("change");

                        String name = change.getString("symbol");
                        String display_name = change.getString("tokenName");
                        int scale = change.getInt("decimals");
                        String id = change.getString("tokenAddress");
                        String key = id;

                        Token token = TokenManager.getTokenManagerFromKey("SPLTokenManager").getOrCreateToken(key, name, display_name, scale, id);

                        BigDecimal b = new BigDecimal(change.getString("changeAmount"));

                        String action;
                        if(b.compareTo(BigDecimal.ZERO) > 0) {
                            action = "Receive";
                        }
                        else {
                            action = "Send";
                            b = b.negate();
                        }
                        b = b.movePointLeft(token.getScale());

                        String amount = b.toPlainString();
                        transactionArrayList.add(new Transaction(new Action(action), new AssetQuantity(amount, token), null, new Timestamp(block_time_date),"Transaction"));
                        if(transactionArrayList.size() == getMaxTransactions()) { return transactionArrayList; }
                    }
                }

                // Tokens2
                String addressDataTokenJSON2 = REST.get(baseURL + "/account/token/txs?address=" + cryptoAddress.address + "&limit=10&offset=10"); // &offset
                if(addressDataTokenJSON2 != null) {
                    JSONObject jsonToken2 = new JSONObject(addressDataTokenJSON2);
                    JSONArray jsonTokenData2 = jsonToken2.getJSONObject("data").getJSONObject("tx").getJSONArray("transactions");
                    for(int i = 0; i < jsonTokenData2.length(); i++) {
                        JSONObject o = jsonTokenData2.getJSONObject(i);

                        BigDecimal block_time = new BigDecimal(o.getString("blockTime"));
                        block_time = block_time.multiply(new BigDecimal(1000));
                        Date block_time_date = new Date(block_time.longValue());

                        JSONObject change = o.getJSONObject("change");

                        String name = change.getString("symbol");
                        String display_name = change.getString("tokenName");
                        int scale = change.getInt("decimals");
                        String id = change.getString("tokenAddress");
                        String key = id;

                        Token token = TokenManager.getTokenManagerFromKey("SPLTokenManager").getOrCreateToken(key, name, display_name, scale, id);

                        BigDecimal b = new BigDecimal(change.getString("changeAmount"));

                        String action;
                        if(b.compareTo(BigDecimal.ZERO) > 0) {
                            action = "Receive";
                        }
                        else {
                            action = "Send";
                            b = b.negate();
                        }
                        b = b.movePointLeft(token.getScale());

                        String amount = b.toPlainString();
                        transactionArrayList.add(new Transaction(new Action(action), new AssetQuantity(amount, token), null, new Timestamp(block_time_date),"Transaction"));
                        if(transactionArrayList.size() == getMaxTransactions()) { return transactionArrayList; }
                    }
                }
            }
        }
        catch(java.lang.Exception e) {
            Exception.processException(e);
            return null;
        }

        return transactionArrayList;
    }
}

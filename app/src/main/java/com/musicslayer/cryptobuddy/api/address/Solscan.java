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
import com.musicslayer.cryptobuddy.util.ThrowableUtil;
import com.musicslayer.cryptobuddy.util.RESTUtil;

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

// TODO There are a lot of discrepancies. There are other types of transactions other than "" that should be dealt with.
// Address: zPHwJvSkp6XcqjuWZcbUrDKcMHaUcwcFCLbToRHMEXv
// Transaction: https://api.solscan.io/transaction?tx=DxmwHS9LB5oQJgFX7T3aeUz4YVTWUZqKYNjbzoPCor9srg3kTAVfR8fG5g6Mw5XphYHKee4AhKAcS8ABGtEtyfY

// Address: AqR6BBUFG3NhC4bSAU31zhHNZ1JdSzPwM43PKSSG878N

// Also, the first transaction fee was paid by someone else.
// We need to check who actually paid the fee.

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

        String addressDataJSON = RESTUtil.get(baseURL + "/account?address=" + cryptoAddress.address);
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
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            return null;
        }

        if(shouldIncludeTokens(cryptoAddress)) {
            String addressTokenDataJSON = RESTUtil.get(baseURL + "/account/tokens?address=" + cryptoAddress.address);
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
            catch(Exception e) {
                ThrowableUtil.processThrowable(e);
                return null;
            }
        }

        return currentBalanceArrayList;
    }

    public ArrayList<Transaction> getTransactions(CryptoAddress cryptoAddress) {
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

        ArrayList<Transaction> transactionNormalArrayList = new ArrayList<>();
        ArrayList<Transaction> transactionTokensArrayList = new ArrayList<>();

        String lastID = "";
        for(;;) {
            String url = baseURL + "/account/transaction?address=" + cryptoAddress.address + "&before=" + lastID;
            lastID = processNormal(url, cryptoAddress, transactionNormalArrayList);

            if(lastID == null) {
                return null;
            }
            else if(DONE.equals(lastID)) {
                break;
            }
        }

        for(int offset = 0; ; offset += 10) {
            String url = baseURL + "/account/token/txs?address=" + cryptoAddress.address + "&limit=10&offset=" + offset;
            String status = processTokens(url, cryptoAddress, transactionTokensArrayList);

            if(status == null) {
                return null;
            }
            else if(DONE.equals(status)) {
                break;
            }
        }

        ArrayList<Transaction> transactionArrayList = new ArrayList<>();

        // Roughly split max transactions between each type (rounding is OK).
        int splitNum = cryptoAddress.network.isMainnet() ? 2 : 1;
        int splitMax = getMaxTransactions()/splitNum;

        transactionArrayList.addAll(transactionNormalArrayList.subList(0, Math.min(splitMax, transactionNormalArrayList.size())));
        transactionArrayList.addAll(transactionTokensArrayList.subList(0, Math.min(splitMax, transactionTokensArrayList.size())));

        transactionNormalArrayList.subList(0, Math.min(splitMax, transactionNormalArrayList.size())).clear();
        transactionTokensArrayList.subList(0, Math.min(splitMax, transactionTokensArrayList.size())).clear();

        while(transactionNormalArrayList.size() + transactionTokensArrayList.size() > 0) {
            if(transactionNormalArrayList.size() > 0) {
                transactionArrayList.add(transactionNormalArrayList.get(0));
                transactionNormalArrayList.remove(0);
            }
            if(transactionArrayList.size() == getMaxTransactions()) { break; }

            if(transactionTokensArrayList.size() > 0) {
                transactionArrayList.add(transactionTokensArrayList.get(0));
                transactionTokensArrayList.remove(0);
            }
            if(transactionArrayList.size() == getMaxTransactions()) { break; }
        }

        return transactionArrayList;
    }

    private String processNormal(String url, CryptoAddress cryptoAddress, ArrayList<Transaction> transactionNormalArrayList) {
        String addressDataJSON = RESTUtil.get(url);
        if(addressDataJSON == null) {
            return null;
        }

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

        try {
            String lastID = DONE;

            // SOL
            JSONObject json = new JSONObject(addressDataJSON);
            JSONArray jsonData = json.getJSONArray("data");
            for(int i = 0; i < jsonData.length(); i++) {
                JSONObject o = jsonData.getJSONObject(i);

                // Store the ID of the last thing we processed. The next call will use this and start at the element after this one.
                lastID = o.getString("txHash");

                BigDecimal block_time = new BigDecimal(o.getString("blockTime"));
                block_time = block_time.multiply(new BigDecimal(1000));
                Date block_time_date = new Date(block_time.longValue());

                BigDecimal fee = new BigDecimal(o.getString("fee"));
                fee = fee.movePointLeft(cryptoAddress.getCrypto().getScale());
                if(fee.compareTo(BigDecimal.ZERO) > 0) {
                    transactionNormalArrayList.add(new Transaction(new Action("Fee"), new AssetQuantity(fee.toPlainString(), cryptoAddress.getCrypto()), null, new Timestamp(block_time_date),"Transaction Fee"));
                    if(transactionNormalArrayList.size() == getMaxTransactions()) { return DONE; }
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
                    String transactionJSON = RESTUtil.get(baseURL + "/transaction?tx=" + o.getString("txHash"));
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

                        transactionNormalArrayList.add(new Transaction(new Action(action), new AssetQuantity(amount, cryptoAddress.getCrypto()), null, new Timestamp(block_time_date),"Transaction"));
                        if(transactionNormalArrayList.size() == getMaxTransactions()) { return DONE; }
                    }
                }
            }

            return lastID;
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            return null;
        }
    }

    private String processTokens(String url, CryptoAddress cryptoAddress, ArrayList<Transaction> transactionTokenArrayList) {
        if(!shouldIncludeTokens(cryptoAddress)) { return DONE; }

        String addressDataTokenJSON = RESTUtil.get(url);
        if(addressDataTokenJSON == null) {
            // Token JSON may be null if there are no token transfers.
            return DONE;
        }

        try {
            String status = DONE;

            // Tokens
            JSONObject jsonToken = new JSONObject(addressDataTokenJSON);
            JSONArray jsonTokenData = jsonToken.getJSONObject("data").getJSONObject("tx").getJSONArray("transactions");
            for(int i = 0; i < jsonTokenData.length(); i++) {
                // If there is anything to process, we may not be done yet.
                status = "NotDone";

                JSONObject o = jsonTokenData.getJSONObject(i);

                // End here if the transaction has errored.
                if("Fail".equals(o.getString("status"))) {
                    continue;
                }

                BigDecimal block_time = new BigDecimal(o.getString("blockTime"));
                block_time = block_time.multiply(new BigDecimal(1000));
                Date block_time_date = new Date(block_time.longValue());

                JSONObject change = o.getJSONObject("change");

                // Name and display name may or may not be present.
                String name;
                if(change.has("symbol")) {
                    name = change.getString("symbol");
                }
                else {
                    name = "?";
                }

                String display_name;
                if(change.has("tokenName")) {
                    display_name = change.getString("tokenName");
                }
                else {
                    display_name = "?";
                }

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
                transactionTokenArrayList.add(new Transaction(new Action(action), new AssetQuantity(amount, token), null, new Timestamp(block_time_date),"Transaction"));
                if(transactionTokenArrayList.size() == getMaxTransactions()) { return DONE; }
            }

            return status;
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            return null;
        }
    }
}

package com.musicslayer.cryptobuddy.api.address;

import com.musicslayer.cryptobuddy.asset.crypto.coin.SOL;
import com.musicslayer.cryptobuddy.asset.crypto.token.Token;
import com.musicslayer.cryptobuddy.asset.network.SOL_Devnet;
import com.musicslayer.cryptobuddy.asset.network.SOL_Testnet;
import com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager;
import com.musicslayer.cryptobuddy.transaction.Action;
import com.musicslayer.cryptobuddy.transaction.AssetQuantity;
import com.musicslayer.cryptobuddy.transaction.Timestamp;
import com.musicslayer.cryptobuddy.transaction.Transaction;
import com.musicslayer.cryptobuddy.util.RESTUtil;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

// TODO Currently, we cannot search for all rent payments.
// Bitquery has API, but it is broken - https://community.bitquery.io/t/solana-block-rewards-has-confusing-account-selector/558

// Address: AqR6BBUFG3NhC4bSAU31zhHNZ1JdSzPwM43PKSSG878N
// Address: zPHwJvSkp6XcqjuWZcbUrDKcMHaUcwcFCLbToRHMEXv
// Both have rent payments not tied to transactions.

/*
Rent per byte-year: 0.00000348 SOL
Rent per epoch: 0.000002439 SOL
Rent-exempt minimum: 0.00089088 SOL
 */

public class Solana extends AddressAPI {
    public String getName() { return "Solana"; }
    public String getDisplayName() { return "Solana JSON RPC API"; }

    public boolean isSupported(CryptoAddress cryptoAddress) {
        return "SOL".equals(cryptoAddress.getCrypto().getName());
    }

    public ArrayList<AssetQuantity> getCurrentBalance(CryptoAddress cryptoAddress) {
        ArrayList<AssetQuantity> currentBalanceArrayList = new ArrayList<>();

        String baseURL;
        if(cryptoAddress.network.isMainnet()) {
            baseURL = "https://api.mainnet-beta.solana.com";
        }
        else if(cryptoAddress.network instanceof SOL_Testnet) {
            baseURL = "https://api.testnet.solana.com";
        }
        else if(cryptoAddress.network instanceof SOL_Devnet) {
            baseURL = "https://api.devnet.solana.com";
        }
        else {
            return null;
        }

        String body =
            "{" +
            "  \"jsonrpc\": \"2.0\"," +
            "  \"id\": 1," +
            "  \"method\": \"getBalance\"," +
            "  \"params\": [" +
            "    \"" + cryptoAddress.address + "\"" +
            "  ]" +
            "}";

        String addressDataJSON = RESTUtil.post(baseURL, body);
        if(addressDataJSON == null) {
            return null;
        }

        try {
            // SOL
            JSONObject json = new JSONObject(addressDataJSON);

            BigDecimal balance = new BigDecimal(json.getJSONObject("result").getString("value"));
            balance = balance.movePointLeft(cryptoAddress.getCrypto().getScale());
            String currentBalance = balance.toPlainString();

            currentBalanceArrayList.add(new AssetQuantity(currentBalance, new SOL()));
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            return null;
        }

        if(shouldIncludeTokens(cryptoAddress)) {
            String tokenBody =
                "{" +
                "  \"jsonrpc\": \"2.0\"," +
                "  \"id\": 1," +
                "  \"method\": \"getTokenAccountsByOwner\"," +
                "  \"params\": [" +
                "    \"" + cryptoAddress.address + "\"," +
                "    {\"programId\": \"TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA\"}," +
                "    {\"encoding\": \"jsonParsed\"}" +
                "  ]" +
                "}";

            String addressTokenDataJSON = RESTUtil.post(baseURL, tokenBody);
            if(addressTokenDataJSON == null) {
                return null;
            }

            try {
                // Tokens
                JSONObject jsonToken = new JSONObject(addressTokenDataJSON);
                JSONArray jsonTokenArray = jsonToken.getJSONObject("result").getJSONArray("value");
                for(int i = 0; i < jsonTokenArray.length(); i++) {
                    JSONObject tokenData = jsonTokenArray.getJSONObject(i);
                    JSONObject account = tokenData.getJSONObject("account");
                    JSONObject data = account.getJSONObject("data");
                    JSONObject parsed = data.getJSONObject("parsed");

                    JSONObject info = parsed.getJSONObject("info");
                    JSONObject tokenAmount = info.getJSONObject("tokenAmount");

                    int scale = tokenAmount.getInt("decimals");
                    String id = info.getString("mint");
                    String key = id;
                    Token token = TokenManager.getTokenManagerFromKey("SPLTokenManager").getOrCreateToken(key, null, null, scale, id);

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
            baseURL = "https://api.mainnet-beta.solana.com";
        }
        else if(cryptoAddress.network instanceof SOL_Testnet) {
            baseURL = "https://api.testnet.solana.com";
        }
        else if(cryptoAddress.network instanceof SOL_Devnet) {
            baseURL = "https://api.devnet.solana.com";
        }
        else {
            return null;
        }

        // Create an ArrayList with all the accounts owned by this one (i.e. token accounts).
        String ownerBody =
            "{" +
            "  \"jsonrpc\": \"2.0\"," +
            "  \"id\": 1," +
            "  \"method\": \"getTokenAccountsByOwner\"," +
            "  \"params\": [" +
            "    \"" + cryptoAddress.address + "\"," +
            "    {\"programId\": \"TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA\"}," +
            "    {\"encoding\": \"jsonParsed\"}" +
            "  ]" +
            "}";

        String ownerDataJSON = RESTUtil.post(baseURL, ownerBody);
        if(ownerDataJSON == null) {
            return null;
        }

        ArrayList<String> ownerList = new ArrayList<>(); // Accounts owned by this one.
        HashMap<String, Token> tokenMap = new HashMap<>();

        try {
            // Put this address in ownerList but not tokenMap.
            ownerList.add(cryptoAddress.address);

            JSONObject jsonOwner = new JSONObject(ownerDataJSON);
            JSONArray jsonOwnerArray = jsonOwner.getJSONObject("result").getJSONArray("value");
            for(int i = 0; i < jsonOwnerArray.length(); i++) {
                JSONObject ownerData = jsonOwnerArray.getJSONObject(i);
                String type = ownerData.getJSONObject("account").getJSONObject("data").getJSONObject("parsed").getString("type");

                if("account".equals(type)) {
                    String pubkey = ownerData.getString("pubkey");
                    String mint = ownerData.getJSONObject("account").getJSONObject("data").getJSONObject("parsed").getJSONObject("info").getString("mint");

                    String id = mint;
                    String key = id;
                    Token token = TokenManager.getTokenManagerFromKey("SPLTokenManager").getOrCreateToken(key, null, null, 0, id);

                    ownerList.add(pubkey);
                    tokenMap.put(pubkey, token);
                }
            }
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            return null;
        }

        ArrayList<Transaction> transactionArrayList = new ArrayList<>();
        ArrayList<String> signatureList = new ArrayList<>();

        for(String address : ownerList) {
            String lastID = "";
            for(;;) {
                String lastID_s;
                if("".equals(lastID)) {
                    lastID_s = "null";
                }
                else {
                    lastID_s = "\"" + lastID + "\"";
                }

                String body =
                    "{" +
                    "  \"jsonrpc\": \"2.0\"," +
                    "  \"id\": 1," +
                    "  \"method\": \"getSignaturesForAddress\"," +
                    "  \"params\": [" +
                    "    \"" + address + "\"," +
                    "    {\"limit\": 1000, \"before\": " + lastID_s + "}" +
                    "  ]" +
                    "}";

                lastID = process(baseURL, body, cryptoAddress, ownerList, tokenMap, signatureList, transactionArrayList);

                if(ERROR.equals(lastID)) {
                    return null;
                }
                else if(DONE.equals(lastID)) {
                    break;
                }
            }
        }

        return transactionArrayList;
    }

    private String process(String url, String body, CryptoAddress cryptoAddress, ArrayList<String> ownerList, HashMap<String, Token> tokenMap, ArrayList<String> signatureList, ArrayList<Transaction> transactionArrayList) {
        String addressDataJSON = RESTUtil.post(url, body);
        if(addressDataJSON == null) {
            return ERROR;
        }

        String baseURL;
        if(cryptoAddress.network.isMainnet()) {
            baseURL = "https://api.mainnet-beta.solana.com";
        }
        else if(cryptoAddress.network instanceof SOL_Testnet) {
            baseURL = "https://api.testnet.solana.com";
        }
        else if(cryptoAddress.network instanceof SOL_Devnet) {
            baseURL = "https://api.devnet.solana.com";
        }
        else {
            return ERROR;
        }

        try {
            String lastID = DONE;

            // SOL
            JSONObject json = new JSONObject(addressDataJSON);
            JSONArray jsonData = json.getJSONArray("result");
            for(int i = 0; i < jsonData.length(); i++) {
                JSONObject o = jsonData.getJSONObject(i);

                // Store the ID of the last thing we processed. The next call will use this and start at the element after this one.
                lastID = o.getString("signature");

                if(signatureList.contains(lastID)) { continue; }
                signatureList.add(lastID);

                BigDecimal block_time = new BigDecimal(o.getString("blockTime"));
                block_time = block_time.multiply(new BigDecimal(1000));
                Date block_time_date = new Date(block_time.longValue());

                // Get the individual transaction info.
                String transactionBody =
                    "{" +
                    "  \"jsonrpc\": \"2.0\"," +
                    "  \"id\": 1," +
                    "  \"method\": \"getTransaction\"," +
                    "  \"params\": [" +
                    "    \"" + o.getString("signature") + "\"," +
                    "    \"jsonParsed\"" +
                    "  ]" +
                    "}";

                String transactionJSON = RESTUtil.post(baseURL, transactionBody);
                JSONObject transactionObj = new JSONObject(transactionJSON);

                // Search for fee. The first account in the list is the fee payer.
                String feePayer = transactionObj.getJSONObject("result").getJSONObject("transaction").getJSONObject("message").getJSONArray("accountKeys").getJSONObject(0).getString("pubkey");
                if(cryptoAddress.matchesAddress(feePayer)) {
                    BigDecimal fee = new BigDecimal(transactionObj.getJSONObject("result").getJSONObject("meta").getString("fee"));
                    fee = fee.movePointLeft(cryptoAddress.getCrypto().getScale());

                    if(fee.compareTo(BigDecimal.ZERO) > 0) {
                        transactionArrayList.add(new Transaction(new Action("Fee"), new AssetQuantity(fee.toPlainString(), cryptoAddress.getCrypto()), null, new Timestamp(block_time_date),"Transaction Fee"));
                        if(transactionArrayList.size() == getMaxTransactions()) { return DONE; }
                    }
                }

                // End here if the transaction has errored.
                if(!"null".equals(transactionObj.getJSONObject("result").getJSONObject("meta").getString("err"))) {
                    continue;
                }

                // This map only needs to store values within a single transaction.
                // Start by filling the map in with all the pre-balances.
                HashMap<String, BigDecimal> map = new HashMap<>();

                // These should be the same length.
                JSONArray preBalances = transactionObj.getJSONObject("result").getJSONObject("meta").getJSONArray("preBalances");
                JSONArray accountKeys = transactionObj.getJSONObject("result").getJSONObject("transaction").getJSONObject("message").getJSONArray("accountKeys");

                for(int j = 0; j < preBalances.length(); j++) {
                    BigDecimal preBalance = new BigDecimal(preBalances.getString(j));
                    preBalance = preBalance.movePointLeft(cryptoAddress.getCrypto().getScale());
                    String account = accountKeys.getJSONObject(j).getString("pubkey");
                    map.put(account, preBalance);
                }

                // Search for rewards/rent.
                JSONArray rewards = transactionObj.getJSONObject("result").getJSONObject("meta").getJSONArray("rewards");
                for(int j = 0; j < rewards.length(); j++) {
                    JSONObject reward = rewards.getJSONObject(j);
                    String pubkey = reward.getString("pubkey");
                    if(cryptoAddress.matchesAddress(pubkey)) {
                        String rewardType = reward.getString("rewardType");

                        BigDecimal b = new BigDecimal(reward.getString("lamports"));
                        b = b.movePointLeft(cryptoAddress.getCrypto().getScale());

                        String action;
                        if(b.compareTo(BigDecimal.ZERO) > 0) {
                            action = "Receive";
                        }
                        else if(b.compareTo(BigDecimal.ZERO) < 0) {
                            action = "Send";
                            b = b.negate();
                        }
                        else {
                            // Don't bother with a zero reward.
                            continue;
                        }

                        String amount = b.toPlainString();

                        transactionArrayList.add(new Transaction(new Action(action), new AssetQuantity(amount, cryptoAddress.getCrypto()), null, new Timestamp(block_time_date), rewardType));
                        if(transactionArrayList.size() == getMaxTransactions()) { return DONE; }
                    }
                }

                // Process inner instructions first, because some information here may be used later.
                JSONArray innerArray = transactionObj.getJSONObject("result").getJSONObject("meta").getJSONArray("innerInstructions");
                for(int j = 0; j < innerArray.length(); j++) {
                    JSONObject inner = innerArray.getJSONObject(j);

                    JSONArray instructions = inner.getJSONArray("instructions");
                    for(int jj = 0; jj < instructions.length(); jj++) {
                        JSONObject instruction = instructions.getJSONObject(jj);
                        JSONObject parsed = instruction.getJSONObject("parsed");

                        String type = parsed.getString("type");
                        // Only deal with transfers and ignore everything else.
                        if("transfer".equals(type)) {
                            JSONObject info = parsed.getJSONObject("info");

                            String from = info.getString("source");
                            String to = info.getString("destination");

                            boolean isFrom = ownerList.contains(info.getString("source"));
                            boolean isTo = ownerList.contains(info.getString("destination"));

                            if(!info.has("lamports")) {
                                // Tokens will have "amount" instead of lamports.
                                BigDecimal b2 = new BigDecimal(info.getString("amount"));
                                b2 = b2.movePointLeft(cryptoAddress.getCrypto().getScale());

                                Token token;
                                String action;
                                if (isTo) {
                                    action = "Receive";
                                    token = tokenMap.get(to);
                                    subtract(map, from, b2);
                                } else if (isFrom) {
                                    action = "Send";
                                    token = tokenMap.get(from);
                                    add(map, to, b2);
                                } else {
                                    // Our address is not directly involved.
                                    // Keep track of this in the hashmap in case one of those accounts is closed by our account later.
                                    // If we do use this later, it will only be for SOL.
                                    subtract(map, from, b2);
                                    add(map, to, b2);
                                    continue;
                                }

                                BigDecimal b = new BigDecimal(info.getString("amount"));
                                b = b.movePointLeft(token.getScale());
                                String amount = b.toPlainString();

                                transactionArrayList.add(new Transaction(new Action(action), new AssetQuantity(amount, token), null, new Timestamp(block_time_date), "Transaction"));
                                if (transactionArrayList.size() == getMaxTransactions()) { return DONE; }
                            }
                            else {
                                BigDecimal b = new BigDecimal(info.getString("lamports"));
                                b = b.movePointLeft(cryptoAddress.getCrypto().getScale());
                                String amount = b.toPlainString();

                                // If I send something to myself, just reject it!
                                if (cryptoAddress.network.matchesAddress(from, to)) {
                                    continue;
                                }

                                String action;
                                if (cryptoAddress.matchesAddress(to)) {
                                    action = "Receive";
                                    subtract(map, from, b);
                                } else if (cryptoAddress.matchesAddress(from)) {
                                    action = "Send";
                                    add(map, to, b);
                                } else {
                                    // Our address is not directly involved.
                                    // Keep track of this in the hashmap in case one of those accounts is closed by our account later.
                                    subtract(map, from, b);
                                    add(map, to, b);
                                    continue;
                                }

                                transactionArrayList.add(new Transaction(new Action(action), new AssetQuantity(amount, cryptoAddress.getCrypto()), null, new Timestamp(block_time_date), "Transaction"));
                                if (transactionArrayList.size() == getMaxTransactions()) { return DONE; }
                            }
                        }
                        else if("transferChecked".equals(type)) {
                            JSONObject info = parsed.getJSONObject("info");
                            JSONObject tokenAmount = info.getJSONObject("tokenAmount");

                            boolean isFrom = ownerList.contains(info.getString("source"));
                            boolean isTo = ownerList.contains(info.getString("destination"));

                            BigDecimal b = new BigDecimal(tokenAmount.getString("amount"));
                            b = b.movePointLeft(cryptoAddress.getCrypto().getScale());
                            String amount = b.toPlainString();

                            // If I send something to myself, just reject it!
                            if(isFrom && isTo) {
                                continue;
                            }

                            String action;
                            if (isTo) {
                                action = "Receive";
                            } else if (isFrom) {
                                action = "Send";
                            } else {
                                continue;
                            }

                            int scale = tokenAmount.getInt("decimals");
                            String id = info.getString("mint");
                            String key = id;
                            Token token = TokenManager.getTokenManagerFromKey("SPLTokenManager").getOrCreateToken(key, null, null, scale, id);

                            transactionArrayList.add(new Transaction(new Action(action), new AssetQuantity(amount, token), null, new Timestamp(block_time_date), "Transaction"));
                            if (transactionArrayList.size() == getMaxTransactions()) { return DONE; }
                        }
                        else if("createAccount".equals(type)) {
                            JSONObject info = parsed.getJSONObject("info");

                            String from = info.getString("source");
                            String to = info.getString("newAccount");

                            BigDecimal b = new BigDecimal(info.getString("lamports"));
                            b = b.movePointLeft(cryptoAddress.getCrypto().getScale());
                            String amount = b.toPlainString();

                            // If I send something to myself, just reject it!
                            if (cryptoAddress.network.matchesAddress(from, to)) {
                                continue;
                            }

                            String action;
                            if (cryptoAddress.matchesAddress(to)) {
                                action = "Receive";
                                subtract(map, from, b);
                            } else if (cryptoAddress.matchesAddress(from)) {
                                action = "Send";
                                add(map, to, b);
                            } else {
                                // Our address is not directly involved.
                                // Keep track of this in the hashmap in case one of those accounts is closed by our account later.
                                subtract(map, from, b);
                                add(map, to, b);
                                continue;
                            }

                            transactionArrayList.add(new Transaction(new Action(action), new AssetQuantity(amount, cryptoAddress.getCrypto()), null, new Timestamp(block_time_date), "Create Other Account"));
                            if (transactionArrayList.size() == getMaxTransactions()) { return DONE; }
                        }
                        else if("closeAccount".equals(type)) {
                            JSONObject info = parsed.getJSONObject("info");

                            String from = info.getString("account");
                            String to = info.getString("destination");

                            // When an account closes, all the SOL gets transferred over.
                            // There are two sources of SOL for the closed account. One is from within this transaction, and the other is the starting balance.
                            BigDecimal b = map.get(from); // Already Scaled
                            String amount = b.toPlainString();

                            // If I send something to myself, just reject it!
                            if (cryptoAddress.network.matchesAddress(from, to)) {
                                continue;
                            }

                            String action;
                            if (cryptoAddress.matchesAddress(to)) {
                                action = "Receive";
                                subtract(map, from, b);
                            } else if (cryptoAddress.matchesAddress(from)) {
                                action = "Send";
                                add(map, to, b);
                            } else {
                                // Our address is not directly involved.
                                // Keep track of this in the hashmap in case one of those accounts is closed by our account later.
                                subtract(map, from, b);
                                add(map, to, b);
                                continue;
                            }

                            transactionArrayList.add(new Transaction(new Action(action), new AssetQuantity(amount, cryptoAddress.getCrypto()), null, new Timestamp(block_time_date), "Close Other Account"));
                            if (transactionArrayList.size() == getMaxTransactions()) { return DONE; }
                        }
                    }
                }

                // Process regular instructions.
                JSONArray instructions = transactionObj.getJSONObject("result").getJSONObject("transaction").getJSONObject("message").getJSONArray("instructions");
                for(int j = 0; j < instructions.length(); j++) {
                    JSONObject instruction = instructions.getJSONObject(j);

                    if(!instruction.has("parsed")) {
                        // Skip these.
                        continue;
                    }

                    JSONObject parsed = instruction.getJSONObject("parsed");

                    String type = parsed.getString("type");

                    // Only process certain ones.
                    if("transfer".equals(type)) {
                        JSONObject info = parsed.getJSONObject("info");

                        String from = info.getString("source");
                        String to = info.getString("destination");

                        boolean isFrom = ownerList.contains(info.getString("source"));
                        boolean isTo = ownerList.contains(info.getString("destination"));

                        // If I send something to myself, just reject it!
                        if (cryptoAddress.network.matchesAddress(from, to)) {
                            continue;
                        }

                        if(!info.has("lamports")) {
                            // Tokens will have "amount" instead of lamports.
                            Token token;

                            String action;
                            if (isTo) {
                                action = "Receive";
                                token = tokenMap.get(to);
                            } else if (isFrom) {
                                action = "Send";
                                token = tokenMap.get(from);
                            } else {
                                continue;
                            }

                            BigDecimal b = new BigDecimal(info.getString("amount"));
                            b = b.movePointLeft(token.getScale());
                            String amount = b.toPlainString();

                            transactionArrayList.add(new Transaction(new Action(action), new AssetQuantity(amount, token), null, new Timestamp(block_time_date), "Transaction"));
                            if (transactionArrayList.size() == getMaxTransactions()) { return DONE; }
                        }
                        else {
                            BigDecimal b = new BigDecimal(info.getString("lamports"));
                            b = b.movePointLeft(cryptoAddress.getCrypto().getScale());
                            String amount = b.toPlainString();

                            String action;
                            if (cryptoAddress.matchesAddress(to)) {
                                action = "Receive";
                                subtract(map, from, b);
                            } else if (cryptoAddress.matchesAddress(from)) {
                                action = "Send";
                                add(map, to, b);
                            } else {
                                // Our address is not directly involved.
                                // Keep track of this in the hashmap in case one of those accounts is closed by our account later.
                                subtract(map, from, b);
                                add(map, to, b);
                                continue;
                            }

                            transactionArrayList.add(new Transaction(new Action(action), new AssetQuantity(amount, cryptoAddress.getCrypto()), null, new Timestamp(block_time_date), "Transaction"));
                            if (transactionArrayList.size() == getMaxTransactions()) { return DONE; }
                        }
                    }
                    else if("transferChecked".equals(type)) {
                        JSONObject info = parsed.getJSONObject("info");
                        JSONObject tokenAmount = info.getJSONObject("tokenAmount");

                        boolean isFrom = ownerList.contains(info.getString("source"));
                        boolean isTo = ownerList.contains(info.getString("destination"));

                        BigDecimal b = new BigDecimal(tokenAmount.getString("amount"));
                        b = b.movePointLeft(cryptoAddress.getCrypto().getScale());
                        String amount = b.toPlainString();

                        // If I send something to myself, just reject it!
                        if(isFrom && isTo) {
                            continue;
                        }

                        String action;
                        if (isTo) {
                            action = "Receive";
                        } else if (isFrom) {
                            action = "Send";
                        } else {
                            continue;
                        }

                        int scale = tokenAmount.getInt("decimals");
                        String id = info.getString("mint");
                        String key = id;
                        Token token = TokenManager.getTokenManagerFromKey("SPLTokenManager").getOrCreateToken(key, null, null, scale, id);

                        transactionArrayList.add(new Transaction(new Action(action), new AssetQuantity(amount, token), null, new Timestamp(block_time_date), "Transaction"));
                        if (transactionArrayList.size() == getMaxTransactions()) { return DONE; }
                    }
                    else if("createAccount".equals(type)) {
                        JSONObject info = parsed.getJSONObject("info");

                        String from = info.getString("source");
                        String to = info.getString("newAccount");

                        BigDecimal b = new BigDecimal(info.getString("lamports"));
                        b = b.movePointLeft(cryptoAddress.getCrypto().getScale());
                        String amount = b.toPlainString();

                        // If I send something to myself, just reject it!
                        if (cryptoAddress.network.matchesAddress(from, to)) {
                            continue;
                        }

                        String action;
                        if (cryptoAddress.matchesAddress(to)) {
                            action = "Receive";
                            subtract(map, from, b);
                        } else if (cryptoAddress.matchesAddress(from)) {
                            action = "Send";
                            add(map, to, b);
                        } else {
                            // Our address is not directly involved.
                            // Keep track of this in the hashmap in case one of those accounts is closed by our account later.
                            subtract(map, from, b);
                            add(map, to, b);
                            continue;
                        }

                        transactionArrayList.add(new Transaction(new Action(action), new AssetQuantity(amount, cryptoAddress.getCrypto()), null, new Timestamp(block_time_date), "Create Other Account"));
                        if (transactionArrayList.size() == getMaxTransactions()) { return DONE; }
                    }
                    else if("closeAccount".equals(type)) {
                        JSONObject info = parsed.getJSONObject("info");

                        String from = info.getString("account");
                        String to = info.getString("destination");

                        // When an account closes, all the SOL gets transferred over.
                        // There are two sources of SOL for the closed account. One is from within this transaction, and the other is the starting balance.
                        BigDecimal b = map.get(from); // Already Scaled
                        String amount = b.toPlainString();

                        // If I send something to myself, just reject it!
                        if (cryptoAddress.network.matchesAddress(from, to)) {
                            continue;
                        }

                        String action;
                        if (cryptoAddress.matchesAddress(to)) {
                            action = "Receive";
                            subtract(map, from, b);
                        } else if (cryptoAddress.matchesAddress(from)) {
                            action = "Send";
                            add(map, to, b);
                        } else {
                            // Our address is not directly involved.
                            // Keep track of this in the hashmap in case one of those accounts is closed by our account later.
                            subtract(map, from, b);
                            add(map, to, b);
                            continue;
                        }

                        transactionArrayList.add(new Transaction(new Action(action), new AssetQuantity(amount, cryptoAddress.getCrypto()), null, new Timestamp(block_time_date), "Close Other Account"));
                        if (transactionArrayList.size() == getMaxTransactions()) { return DONE; }
                    }
                }
            }

            return lastID;
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            return ERROR;
        }
    }

    private static void add(HashMap<String, BigDecimal> map, String address, BigDecimal value) {
        // All addresses should already have their pre-balances added to the map.
        BigDecimal oldValue = map.get(address);
        if(oldValue == null) { oldValue = BigDecimal.ZERO; }

        BigDecimal newValue = oldValue.add(value);
        map.put(address, newValue);
    }

    private static void subtract(HashMap<String, BigDecimal> map, String address, BigDecimal value) {
        // All addresses should already have their pre-balances added to the map.
        BigDecimal oldValue = map.get(address);
        if(oldValue == null) { oldValue = BigDecimal.ZERO; }

        BigDecimal newValue = oldValue.subtract(value);
        map.put(address, newValue);
    }
}
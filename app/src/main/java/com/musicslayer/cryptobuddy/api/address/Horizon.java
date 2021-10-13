package com.musicslayer.cryptobuddy.api.address;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import android.util.Log;

/*
Full list of possible operations:
    Create Account
    Payment
    Path Payment Strict Send
    Path Payment Strict Receive
    Manage Buy Offer
    Manage Sell Offer
    Create Passive Sell Offer
    Set Options
    Change Trust
    Allow Trust
    Account Merge
    Manage Data
    Bump Sequence
    Create Claimable Balance
    Claim Claimable Balance
    Begin Sponsoring Future Reserves
    End Sponsoring Future Reserves
    Revoke Sponsorship
    Clawback
    ClawbackClaimableBalance
    SetTrustLineFlags
 */

// TODO Pagination.
// TODO We need to do payments https://horizon.stellar.org/accounts/GADFXROGGR74V3MSWU2SUKCEUPQFZEIIF3IUHLRN3NKZ4JN2IPBMCODA/payments?limit=200&order=desc&include_failed=true

public class Horizon extends AddressAPI {
    public String getName() { return "Horizon"; }
    public String getDisplayName() { return "Horizon API"; }

    public boolean isSupported(CryptoAddress cryptoAddress) {
        return "XLM".equals(cryptoAddress.getCrypto().getName());
    }

    public ArrayList<AssetQuantity> getCurrentBalance(CryptoAddress cryptoAddress) {
        ArrayList<AssetQuantity> currentBalanceArrayList = new ArrayList<>();

        String baseURL;
        if(cryptoAddress.network.isMainnet()) {
            baseURL = "https://horizon.stellar.org";
        }
        else {
            baseURL = "https://horizon-testnet.stellar.org";
        }

        String addressDataJSON = RESTUtil.get(baseURL + "/accounts/" + cryptoAddress.address);
        if(addressDataJSON != null) {
            try {
                JSONObject json = new JSONObject(addressDataJSON);
                JSONArray balanceDataArray = json.getJSONArray("balances");
                for(int i = 0; i < balanceDataArray.length(); i++) {
                    JSONObject balanceData = balanceDataArray.getJSONObject(i);

                    Crypto crypto;
                    String currentBalance;
                    if("native".equals(balanceData.getString("asset_type"))) {
                        String balance = balanceData.getString("balance");

                        // Subtract the 1 used to create the account.
                        //BigDecimal b = new BigDecimal(balance);
                        //b = b.subtract(BigDecimal.ONE);

                        currentBalance = balance;

                        crypto = cryptoAddress.getCrypto();
                    }
                    else {
                        if(!shouldIncludeTokens(cryptoAddress)) {
                            continue;
                        }

                        currentBalance = balanceData.getString("balance");

                        String name = balanceData.getString("asset_code");
                        String display_name = name;
                        int scale = cryptoAddress.getCrypto().getScale();
                        String id = name + "-" + balanceData.getString("asset_issuer");

                        crypto = TokenManager.getTokenManagerFromKey("StellarTokenManager").getOrCreateToken(name, name, display_name, scale, id);
                    }

                    currentBalanceArrayList.add(new AssetQuantity(currentBalance, crypto));
                }
            }
            catch(Exception e) {
                ThrowableUtil.processThrowable(e);
                return null;
            }
        }
        else {
            // For XLM, if addressDataJSON is null, it could be that the account used to exist but is now closed.
            // Users still may want transaction history, so let's just say that the balance is zero.
            currentBalanceArrayList.add(new AssetQuantity("0", cryptoAddress.getCrypto()));
        }

        return currentBalanceArrayList;
    }

    public ArrayList<Transaction> getTransactions(CryptoAddress cryptoAddress) {
        // Roughly split max transactions between each type (rounding is OK).
        int splitNum = shouldIncludeTokens(cryptoAddress) ? 3 : 2; // Operations doesn't count.
        int splitMax = getMaxTransactions()/splitNum;

        ArrayList<Transaction> transactionNormalArrayList = new ArrayList<>();
        ArrayList<Transaction> transactionEffectsArrayList = new ArrayList<>();
        ArrayList<Transaction> transactionIssueArrayList = new ArrayList<>();

        String baseURL;
        if(cryptoAddress.network.isMainnet()) {
            baseURL = "https://horizon.stellar.org";
        }
        else {
            baseURL = "https://horizon-testnet.stellar.org";
        }

        String addressDataJSON = RESTUtil.get(baseURL + "/accounts/" + cryptoAddress.address + "/transactions?limit=200&order=desc&include_failed=true");
        String addressDataOperationsJSON = RESTUtil.get(baseURL + "/accounts/" + cryptoAddress.address + "/operations?limit=200&order=desc&include_failed=true");
        String addressDataEffectsJSON = RESTUtil.get(baseURL + "/accounts/" + cryptoAddress.address + "/effects?limit=200&order=desc&include_failed=true");
        if(addressDataJSON == null && addressDataOperationsJSON == null && addressDataEffectsJSON == null) {
            // If all of these are null, it's possible that there are 0 transactions.
            // If this is true, there are probably no tokens.
            // We really need a better way to check if the account is active.
            return new ArrayList<>();
        }
        else if(addressDataJSON == null || addressDataOperationsJSON == null || addressDataEffectsJSON == null) {
            return null;
        }

        try {
            // Only process fees here.
            JSONObject json = new JSONObject(addressDataJSON);
            JSONArray jsonData = json.getJSONObject("_embedded").getJSONArray("records");

            for(int i = 0; i < jsonData.length(); i++) {
                JSONObject jsonTransaction = jsonData.getJSONObject(i);

                // Don't check for errors because failed transactions need to pay the fee too.

                String block_time = jsonTransaction.getString("created_at");

                // Z means UTC time zone, but older Android cannot parse the Z correctly, so we must manually do it ourselves.
                DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
                format.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date block_time_date = format.parse(block_time);

                BigDecimal fee = BigDecimal.ZERO;

                if(cryptoAddress.address.equals(jsonTransaction.getString("fee_account"))) {
                    fee = new BigDecimal(jsonTransaction.getString("fee_charged"));
                    fee = fee.movePointLeft(cryptoAddress.getCrypto().getScale());
                }

                if(fee.compareTo(BigDecimal.ZERO) > 0) {
                    transactionNormalArrayList.add(new Transaction(new Action("Fee"), new AssetQuantity(fee.toPlainString(), cryptoAddress.getCrypto()), null, new Timestamp(block_time_date),"Transaction Fee"));
                    if(transactionNormalArrayList.size() == getMaxTransactions()) { break; }
                }
            }

            // Look at operations to figure out which trades we should count in the next session.
            ArrayList<BigInteger> skipID = new ArrayList<>();

            JSONObject jsonOperations = new JSONObject(addressDataOperationsJSON);
            JSONArray jsonOperationsData = jsonOperations.getJSONObject("_embedded").getJSONArray("records");
            for(int i = 0; i < jsonOperationsData.length(); i++) {
                JSONObject jsonTransaction = jsonOperationsData.getJSONObject(i);

                // Should we check for errors? jsonTransaction.getBoolean("transaction_successful")

                String type = jsonTransaction.getString("type");
                if("path_payment_strict_send".equals(type) || "path_payment_strict_receive".equals(type)) {
                    skipID.add(new BigInteger(jsonTransaction.getString("id")));
                }
            }

            // Process all real transactions here.
            JSONObject jsonEffects = new JSONObject(addressDataEffectsJSON);
            JSONArray jsonEffectsData = jsonEffects.getJSONObject("_embedded").getJSONArray("records");
            for(int i = 0; i < jsonEffectsData.length(); i++) {
                JSONObject jsonTransaction = jsonEffectsData.getJSONObject(i);

                String block_time = jsonTransaction.getString("created_at");

                // Z means UTC time zone, but older Android cannot parse the Z correctly, so we must manually do it ourselves.
                DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
                format.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date block_time_date = format.parse(block_time);

                String action;
                String amount;
                Crypto crypto;

                String type = jsonTransaction.getString("type");

                Log.e("Crypto Buddy Horizon", "Type = " + type);

                switch(type) {
                    case "account_created":
                        //transactionArrayList.add(new Transaction(new Action("Fee"), new AssetQuantity("1", cryptoAddress.crypto), null, new Timestamp(block_time_date), "", "Account Creation Fee"));

                        action = "Receive";
                        amount = jsonTransaction.getString("starting_balance");
                        crypto = cryptoAddress.getCrypto();
                        transactionEffectsArrayList.add(new Transaction(new Action(action), new AssetQuantity(amount, crypto), null, new Timestamp(block_time_date),"Transaction"));
                        if(transactionEffectsArrayList.size() == getMaxTransactions()) { break; }
                        break;

                    case "account_credited":
                        action = "Receive";
                        amount = jsonTransaction.getString("amount");

                        if("native".equals(jsonTransaction.getString("asset_type"))) {
                            crypto = cryptoAddress.getCrypto();
                        }
                        else {
                            if(!shouldIncludeTokens(cryptoAddress)) {
                                break;
                            }

                            // Tokens
                            String name = jsonTransaction.getString("asset_code");
                            String display_name = name;
                            int scale = cryptoAddress.getCrypto().getScale();
                            String id = name + "-" + jsonTransaction.getString("asset_issuer");

                            crypto = TokenManager.getTokenManagerFromKey("StellarTokenManager").getOrCreateToken(name, name, display_name, scale, id);
                        }

                        transactionEffectsArrayList.add(new Transaction(new Action(action), new AssetQuantity(amount, crypto), null, new Timestamp(block_time_date),"Transaction"));
                        if(transactionEffectsArrayList.size() == getMaxTransactions()) { break; }
                        break;

                    case "account_debited":
                        action = "Send";
                        amount = jsonTransaction.getString("amount");

                        if("native".equals(jsonTransaction.getString("asset_type"))) {
                            crypto = cryptoAddress.getCrypto();
                        }
                        else {
                            if(!shouldIncludeTokens(cryptoAddress)) {
                                break;
                            }

                            // Tokens
                            String name = jsonTransaction.getString("asset_code");
                            String display_name = name;
                            int scale = cryptoAddress.getCrypto().getScale();
                            String id = name + "-" + jsonTransaction.getString("asset_issuer");

                            crypto = TokenManager.getTokenManagerFromKey("StellarTokenManager").getOrCreateToken(name, name, display_name, scale, id);
                        }

                        transactionEffectsArrayList.add(new Transaction(new Action(action), new AssetQuantity(amount, crypto), null, new Timestamp(block_time_date),"Transaction"));
                        if(transactionEffectsArrayList.size() == getMaxTransactions()) { break; }
                        break;

                    case "trade":
                        // Only process trades from buy/sell orders.
                        // Trades from strict sends/receives will be represented by other transactions.

                        // Take the id up to the dash. Also, id's may contain extra leading zeros.
                        String id0 = jsonTransaction.getString("id");
                        int idx = id0.indexOf("-");
                        id0 = id0.substring(0, idx);
                        BigInteger idI = new BigInteger(id0);
                        if(skipID.contains(idI)) {
                            continue;
                        }

                        String thisAction;
                        String otherAction;

                        if(cryptoAddress.address.equals(jsonTransaction.getString("account"))) {
                            thisAction = "Receive";
                            otherAction = "Send";
                        }
                        else if(cryptoAddress.address.equals(jsonTransaction.getString("seller"))) {
                            thisAction = "Send";
                            otherAction = "Receive";
                        }
                        else {
                            // Assume there is nothing to process here.
                            continue;
                        }


                        // Bought
                        Crypto bought_crypto;
                        String bought_amount = jsonTransaction.getString("bought_amount");

                        if("native".equals(jsonTransaction.getString("bought_asset_type"))) {
                            bought_crypto = cryptoAddress.getCrypto();
                            transactionEffectsArrayList.add(new Transaction(new Action(thisAction), new AssetQuantity(bought_amount, bought_crypto), null, new Timestamp(block_time_date),"Transaction"));
                            if(transactionEffectsArrayList.size() == getMaxTransactions()) { break; }
                        }
                        else {
                            if(shouldIncludeTokens(cryptoAddress)) {
                                // Tokens
                                String name = jsonTransaction.getString("bought_asset_code");
                                String display_name = name;
                                int scale = cryptoAddress.getCrypto().getScale();
                                String id = name + "-" + jsonTransaction.getString("bought_asset_issuer");

                                bought_crypto = TokenManager.getTokenManagerFromKey("StellarTokenManager").getOrCreateToken(name, name, display_name, scale, id);
                                transactionEffectsArrayList.add(new Transaction(new Action(thisAction), new AssetQuantity(bought_amount, bought_crypto), null, new Timestamp(block_time_date),"Token Transaction"));
                                if(transactionEffectsArrayList.size() == getMaxTransactions()) { break; }
                            }
                        }


                        // Sold
                        Crypto sold_crypto;
                        String sold_amount = jsonTransaction.getString("sold_amount");

                        if("native".equals(jsonTransaction.getString("sold_asset_type"))) {
                            sold_crypto = cryptoAddress.getCrypto();
                            transactionEffectsArrayList.add(new Transaction(new Action(otherAction), new AssetQuantity(sold_amount, sold_crypto), null, new Timestamp(block_time_date),"Transaction"));
                            if(transactionEffectsArrayList.size() == getMaxTransactions()) { break; }
                        }
                        else {
                            if(shouldIncludeTokens(cryptoAddress)) {
                                // Tokens
                                String name = jsonTransaction.getString("sold_asset_code");
                                String display_name = name;
                                int scale = cryptoAddress.getCrypto().getScale();
                                String id = name + "-" + jsonTransaction.getString("sold_asset_issuer");

                                sold_crypto = TokenManager.getTokenManagerFromKey("StellarTokenManager").getOrCreateToken(name, name, display_name, scale, id);
                                transactionEffectsArrayList.add(new Transaction(new Action(otherAction), new AssetQuantity(sold_amount, sold_crypto), null, new Timestamp(block_time_date),"Token Transaction"));
                                if(transactionEffectsArrayList.size() == getMaxTransactions()) { break; }
                            }
                        }

                        break;

                    case "account_removed":
                    case "signer_created":
                    case "trustline_created":
                    case "trustline_removed":
                    case "trustline_updated":
                    case "account_thresholds_updated":
                    case "data_created":
                    case "account_inflation_destination_updated":
                    case "account_home_domain_updated":
                    case "claimable_balance_claimant_created":
                    case "claimable_balance_sponsorship_removed":
                    case "claimable_balance_claimed": //?
                        //Log.e("Crypto Buddy Horizon", "Old Type = " + type);
                        // NO-OP
                        break;

                    default:
                        Log.e("Crypto Buddy Horizon", "New Type = " + type);
                }
            }
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            return null;
        }

        if(shouldIncludeTokens(cryptoAddress)) {
            String addressDataIssueJSON = RESTUtil.get(baseURL + "/assets?asset_issuer=" + cryptoAddress.address + "&limit=200&include_failed=true");
            if(addressDataIssueJSON == null) {
                return null;
            }

            try {
                // Process assets that this account issued.
                JSONObject jsonIssue = new JSONObject(addressDataIssueJSON);
                JSONArray jsonIssueData = jsonIssue.getJSONObject("_embedded").getJSONArray("records");
                for(int i = 0; i < jsonIssueData.length(); i++) {
                    JSONObject jsonTransaction = jsonIssueData.getJSONObject(i);

                    String amount = jsonTransaction.getString("amount");

                    // The asset here should never be XLM.
                    String name = jsonTransaction.getString("asset_code");
                    String display_name = name;
                    int scale = cryptoAddress.getCrypto().getScale();
                    String id = name + "-" + jsonTransaction.getString("asset_issuer");

                    Token token = TokenManager.getTokenManagerFromKey("StellarTokenManager").getOrCreateToken(name, name, display_name, scale, id);

                    transactionIssueArrayList.add(new Transaction(new Action("Receive"), new AssetQuantity(amount, token), null, new Timestamp(null),"Issued Asset"));
                    if(transactionIssueArrayList.size() == getMaxTransactions()) { break; }
                }
            }
            catch(Exception e) {
                ThrowableUtil.processThrowable(e);
                return null;
            }
        }

        ArrayList<Transaction> transactionArrayList = new ArrayList<>();

        transactionArrayList.addAll(transactionNormalArrayList.subList(0, Math.min(splitMax, transactionNormalArrayList.size())));
        transactionArrayList.addAll(transactionEffectsArrayList.subList(0, Math.min(splitMax, transactionEffectsArrayList.size())));
        transactionArrayList.addAll(transactionIssueArrayList.subList(0, Math.min(splitMax, transactionIssueArrayList.size())));

        transactionNormalArrayList.subList(0, Math.min(splitMax, transactionNormalArrayList.size())).clear();
        transactionEffectsArrayList.subList(0, Math.min(splitMax, transactionEffectsArrayList.size())).clear();
        transactionIssueArrayList.subList(0, Math.min(splitMax, transactionIssueArrayList.size())).clear();

        while(transactionNormalArrayList.size() + transactionEffectsArrayList.size() + transactionIssueArrayList.size() > 0) {
            if(transactionNormalArrayList.size() > 0) {
                transactionArrayList.add(transactionNormalArrayList.get(0));
                transactionNormalArrayList.remove(0);
            }
            if(transactionArrayList.size() == getMaxTransactions()) { break; }

            if(transactionEffectsArrayList.size() > 0) {
                transactionArrayList.add(transactionEffectsArrayList.get(0));
                transactionEffectsArrayList.remove(0);
            }
            if(transactionArrayList.size() == getMaxTransactions()) { break; }

            if(transactionIssueArrayList.size() > 0) {
                transactionArrayList.add(transactionIssueArrayList.get(0));
                transactionIssueArrayList.remove(0);
            }
            if(transactionArrayList.size() == getMaxTransactions()) { break; }
        }

        return transactionArrayList;
    }
}

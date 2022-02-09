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
import java.util.Locale;
import java.util.TimeZone;

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

// Note that we do not need to do payments, or any other link because they are already included in effects.

public class Horizon extends AddressAPI {
    public String getName() { return "Horizon"; }
    public String getDisplayName() { return "Horizon API"; }

    public boolean isSupported(CryptoAddress cryptoAddress) {
        return "XLM".equals(cryptoAddress.getPrimaryCoin().getKey());
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

        String addressDataJSON = WebUtil.get(baseURL + "/accounts/" + cryptoAddress.address);
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

                        crypto = cryptoAddress.getPrimaryCoin();
                    }
                    else {
                        if(!shouldIncludeTokens(cryptoAddress)) {
                            continue;
                        }

                        currentBalance = balanceData.getString("balance");

                        String name = balanceData.getString("asset_code");
                        String display_name = name;
                        int scale = cryptoAddress.getPrimaryCoin().getScale();
                        String id = name + "-" + balanceData.getString("asset_issuer");

                        crypto = TokenManager.getTokenManagerFromKey("StellarTokenManager").getToken(cryptoAddress, name, name, display_name, scale, id);
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
            currentBalanceArrayList.add(new AssetQuantity("0", cryptoAddress.getPrimaryCoin()));
        }

        return currentBalanceArrayList;
    }

    public ArrayList<Transaction> getTransactions(CryptoAddress cryptoAddress) {
        ArrayList<Transaction> transactionNormalArrayList = new ArrayList<>();
        ArrayList<Transaction> transactionEffectsArrayList = new ArrayList<>();
        ArrayList<Transaction> transactionTokensArrayList = new ArrayList<>();

        String baseURL;
        if(cryptoAddress.network.isMainnet()) {
            baseURL = "https://horizon.stellar.org";
        }
        else {
            baseURL = "https://horizon-testnet.stellar.org";
        }

        String addressDataJSON = WebUtil.get(baseURL + "/accounts/" + cryptoAddress.address + "/transactions?limit=200&order=desc&include_failed=true");
        String addressDataOperationsJSON = WebUtil.get(baseURL + "/accounts/" + cryptoAddress.address + "/operations?limit=200&order=desc&include_failed=true");
        String addressDataEffectsJSON = WebUtil.get(baseURL + "/accounts/" + cryptoAddress.address + "/effects?limit=200&order=desc&include_failed=true");
        if(addressDataJSON == null && addressDataOperationsJSON == null && addressDataEffectsJSON == null) {
            // If all of these are null, it's possible that there are 0 transactions.
            // If this is true, there are probably no tokens.
            // We really need a better way to check if the account is active.
            return new ArrayList<>();
        }

        // Process all normal/fees.
        String nextLinkNormal = baseURL + "/accounts/" + cryptoAddress.address + "/transactions?limit=200&order=desc&include_failed=true";
        for(;;) {
            String url = nextLinkNormal;
            nextLinkNormal = processNormal(url, cryptoAddress, transactionNormalArrayList);

            if(ERROR.equals(nextLinkNormal)) {
                return null;
            }
            else if(DONE.equals(nextLinkNormal)) {
                break;
            }
        }

        // Process operations to fill skipID.
        ArrayList<BigInteger> skipID = new ArrayList<>();
        String nextLinkOperations = baseURL + "/accounts/" + cryptoAddress.address + "/operations?limit=200&order=desc&include_failed=true";

        // Only check "max transactions", even if skipID doesn't grow that large.
        for(int i = 200; i <= getMaxTransactions(); i += 200) {
            String url = nextLinkOperations;
            nextLinkOperations = processOperations(url, cryptoAddress, skipID);

            if(ERROR.equals(nextLinkOperations)) {
                return null;
            }
            else if(DONE.equals(nextLinkOperations)) {
                break;
            }
        }

        // Process all effects.
        String nextLinkEffects = baseURL + "/accounts/" + cryptoAddress.address + "/effects?limit=200&order=desc&include_failed=true";
        for(;;) {
            String url = nextLinkEffects;
            nextLinkEffects = processEffects(url, cryptoAddress, skipID, transactionEffectsArrayList);

            if(ERROR.equals(nextLinkEffects)) {
                return null;
            }
            else if(DONE.equals(nextLinkEffects)) {
                break;
            }
        }

        // Process all tokens.
        String nextLinkTokens = baseURL + "/assets?asset_issuer=" + cryptoAddress.address + "&limit=200&include_failed=true";
        for(;;) {
            String url = nextLinkTokens;
            nextLinkTokens = processTokens(url, cryptoAddress, transactionTokensArrayList);

            if(ERROR.equals(nextLinkTokens)) {
                return null;
            }
            else if(DONE.equals(nextLinkTokens)) {
                break;
            }
        }

        ArrayList<Transaction> transactionArrayList = new ArrayList<>();

        // Roughly split max transactions between each type (rounding is OK).
        int splitNum = shouldIncludeTokens(cryptoAddress) ? 3 : 2;
        int splitMax = getMaxTransactions()/splitNum;

        transactionArrayList.addAll(transactionNormalArrayList.subList(0, Math.min(splitMax, transactionNormalArrayList.size())));
        transactionArrayList.addAll(transactionEffectsArrayList.subList(0, Math.min(splitMax, transactionEffectsArrayList.size())));
        transactionArrayList.addAll(transactionTokensArrayList.subList(0, Math.min(splitMax, transactionTokensArrayList.size())));

        transactionNormalArrayList.subList(0, Math.min(splitMax, transactionNormalArrayList.size())).clear();
        transactionEffectsArrayList.subList(0, Math.min(splitMax, transactionEffectsArrayList.size())).clear();
        transactionTokensArrayList.subList(0, Math.min(splitMax, transactionTokensArrayList.size())).clear();

        while(transactionNormalArrayList.size() + transactionEffectsArrayList.size() + transactionTokensArrayList.size() > 0) {
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

            if(transactionTokensArrayList.size() > 0) {
                transactionArrayList.add(transactionTokensArrayList.get(0));
                transactionTokensArrayList.remove(0);
            }
            if(transactionArrayList.size() == getMaxTransactions()) { break; }
        }

        return transactionArrayList;
    }

    // Return null for error/no data, DONE to stop and any other non-null string to keep going.
    private String processNormal(String url, CryptoAddress cryptoAddress, ArrayList<Transaction> transactionNormalArrayList) {
        String addressDataJSON = WebUtil.get(url);
        if(addressDataJSON == null) {
            return ERROR;
        }

        try {
            String nextLink = DONE;

            // Only process fees here.
            JSONObject json = new JSONObject(addressDataJSON);
            JSONArray jsonData = json.getJSONObject("_embedded").getJSONArray("records");

            for(int i = 0; i < jsonData.length(); i++) {
                // If we processed anything, then store the next link.
                nextLink = json.getJSONObject("_links").getJSONObject("next").getString("href");

                JSONObject jsonTransaction = jsonData.getJSONObject(i);

                // Don't check for errors because failed transactions need to pay the fee too.

                String block_time = jsonTransaction.getString("created_at");

                // Z means UTC time zone, but older Android cannot parse the Z correctly, so we must manually do it ourselves.
                DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
                format.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date block_time_date = format.parse(block_time);

                BigDecimal fee = BigDecimal.ZERO;

                if(cryptoAddress.matchesAddress(jsonTransaction.getString("fee_account"))) {
                    fee = new BigDecimal(jsonTransaction.getString("fee_charged"));
                    fee = fee.movePointLeft(cryptoAddress.getFeeCoin().getScale());
                }

                if(fee.compareTo(BigDecimal.ZERO) > 0) {
                    transactionNormalArrayList.add(new Transaction(new Action("Fee"), new AssetQuantity(fee.toPlainString(), cryptoAddress.getFeeCoin()), null, new Timestamp(block_time_date),"Transaction Fee"));
                    if(transactionNormalArrayList.size() == getMaxTransactions()) { return DONE; }
                }
            }

            return nextLink;
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            return ERROR;
        }
    }

    private String processOperations(String url, CryptoAddress cryptoAddress, ArrayList<BigInteger> skipID) {
        String addressDataOperationsJSON = WebUtil.get(url);
        if(addressDataOperationsJSON == null) {
            return ERROR;
        }

        try {
            String nextLink = DONE;

            // Look at operations to figure out which trades we should count in the next session.
            JSONObject jsonOperations = new JSONObject(addressDataOperationsJSON);
            JSONArray jsonOperationsData = jsonOperations.getJSONObject("_embedded").getJSONArray("records");
            for(int i = 0; i < jsonOperationsData.length(); i++) {
                // If we processed anything, then store the next link.
                nextLink = jsonOperations.getJSONObject("_links").getJSONObject("next").getString("href");

                JSONObject jsonTransaction = jsonOperationsData.getJSONObject(i);

                // Should we check for errors? jsonTransaction.getBoolean("transaction_successful")

                String type = jsonTransaction.getString("type");
                if("path_payment_strict_send".equals(type) || "path_payment_strict_receive".equals(type)) {
                    skipID.add(new BigInteger(jsonTransaction.getString("id")));
                    if(skipID.size() == getMaxTransactions()) { return DONE; }
                }
            }

            return nextLink;
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            return ERROR;
        }
    }

    private String processEffects(String url, CryptoAddress cryptoAddress, ArrayList<BigInteger> skipID, ArrayList<Transaction> transactionEffectsArrayList) {
        String addressDataEffectsJSON = WebUtil.get(url);
        if(addressDataEffectsJSON == null) {
            return ERROR;
        }

        try {
            String nextLink = DONE;

            // Process all real transactions here.
            JSONObject jsonEffects = new JSONObject(addressDataEffectsJSON);
            JSONArray jsonEffectsData = jsonEffects.getJSONObject("_embedded").getJSONArray("records");
            for(int i = 0; i < jsonEffectsData.length(); i++) {
                // If we processed anything, then store the next link.
                nextLink = jsonEffects.getJSONObject("_links").getJSONObject("next").getString("href");

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

                switch(type) {
                    case "account_created":
                        // This only applies to this account being created, not this account creating another one.

                        //transactionArrayList.add(new Transaction(new Action("Fee"), new AssetQuantity("1", cryptoAddress.crypto), null, new Timestamp(block_time_date), "", "Account Creation Fee"));

                        action = "Receive";
                        String info = "This Account Created";

                        amount = jsonTransaction.getString("starting_balance");
                        crypto = cryptoAddress.getPrimaryCoin();
                        transactionEffectsArrayList.add(new Transaction(new Action(action), new AssetQuantity(amount, crypto), null, new Timestamp(block_time_date),info));
                        if(transactionEffectsArrayList.size() == getMaxTransactions()) { return DONE; }
                        break;

                    case "account_credited":
                        action = "Receive";
                        amount = jsonTransaction.getString("amount");

                        if("native".equals(jsonTransaction.getString("asset_type"))) {
                            crypto = cryptoAddress.getPrimaryCoin();
                        }
                        else {
                            if(!shouldIncludeTokens(cryptoAddress)) {
                                break;
                            }

                            // Tokens
                            String name = jsonTransaction.getString("asset_code");
                            String display_name = name;
                            int scale = cryptoAddress.getPrimaryCoin().getScale();
                            String id = name + "-" + jsonTransaction.getString("asset_issuer");

                            crypto = TokenManager.getTokenManagerFromKey("StellarTokenManager").getToken(cryptoAddress, name, name, display_name, scale, id);
                        }

                        transactionEffectsArrayList.add(new Transaction(new Action(action), new AssetQuantity(amount, crypto), null, new Timestamp(block_time_date),"Transaction"));
                        if(transactionEffectsArrayList.size() == getMaxTransactions()) { return DONE; }
                        break;

                    case "account_debited":
                        action = "Send";
                        amount = jsonTransaction.getString("amount");

                        if("native".equals(jsonTransaction.getString("asset_type"))) {
                            crypto = cryptoAddress.getPrimaryCoin();
                        }
                        else {
                            if(!shouldIncludeTokens(cryptoAddress)) {
                                break;
                            }

                            // Tokens
                            String name = jsonTransaction.getString("asset_code");
                            String display_name = name;
                            int scale = cryptoAddress.getPrimaryCoin().getScale();
                            String id = name + "-" + jsonTransaction.getString("asset_issuer");

                            crypto = TokenManager.getTokenManagerFromKey("StellarTokenManager").getToken(cryptoAddress, name, name, display_name, scale, id);
                        }

                        transactionEffectsArrayList.add(new Transaction(new Action(action), new AssetQuantity(amount, crypto), null, new Timestamp(block_time_date),"Transaction"));
                        if(transactionEffectsArrayList.size() == getMaxTransactions()) { return DONE; }
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

                        if(cryptoAddress.matchesAddress(jsonTransaction.getString("account"))) {
                            thisAction = "Receive";
                            otherAction = "Send";
                        }
                        else if(cryptoAddress.matchesAddress(jsonTransaction.getString("seller"))) {
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
                            bought_crypto = cryptoAddress.getPrimaryCoin();
                            transactionEffectsArrayList.add(new Transaction(new Action(thisAction), new AssetQuantity(bought_amount, bought_crypto), null, new Timestamp(block_time_date),"Transaction"));
                            if(transactionEffectsArrayList.size() == getMaxTransactions()) { return DONE; }
                        }
                        else {
                            if(shouldIncludeTokens(cryptoAddress)) {
                                // Tokens
                                String name = jsonTransaction.getString("bought_asset_code");
                                String display_name = name;
                                int scale = cryptoAddress.getPrimaryCoin().getScale();
                                String id = name + "-" + jsonTransaction.getString("bought_asset_issuer");

                                bought_crypto = TokenManager.getTokenManagerFromKey("StellarTokenManager").getToken(cryptoAddress, name, name, display_name, scale, id);
                                transactionEffectsArrayList.add(new Transaction(new Action(thisAction), new AssetQuantity(bought_amount, bought_crypto), null, new Timestamp(block_time_date),"Token Transaction"));
                                if(transactionEffectsArrayList.size() == getMaxTransactions()) { return DONE; }
                            }
                        }


                        // Sold
                        Crypto sold_crypto;
                        String sold_amount = jsonTransaction.getString("sold_amount");

                        if("native".equals(jsonTransaction.getString("sold_asset_type"))) {
                            sold_crypto = cryptoAddress.getPrimaryCoin();
                            transactionEffectsArrayList.add(new Transaction(new Action(otherAction), new AssetQuantity(sold_amount, sold_crypto), null, new Timestamp(block_time_date),"Transaction"));
                            if(transactionEffectsArrayList.size() == getMaxTransactions()) { return DONE; }
                        }
                        else {
                            if(shouldIncludeTokens(cryptoAddress)) {
                                // Tokens
                                String name = jsonTransaction.getString("sold_asset_code");
                                String display_name = name;
                                int scale = cryptoAddress.getPrimaryCoin().getScale();
                                String id = name + "-" + jsonTransaction.getString("sold_asset_issuer");

                                sold_crypto = TokenManager.getTokenManagerFromKey("StellarTokenManager").getToken(cryptoAddress, name, name, display_name, scale, id);
                                transactionEffectsArrayList.add(new Transaction(new Action(otherAction), new AssetQuantity(sold_amount, sold_crypto), null, new Timestamp(block_time_date),"Token Transaction"));
                                if(transactionEffectsArrayList.size() == getMaxTransactions()) { return DONE; }
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
                    case "claimable_balance_sponsorship_created": //?
                    case "claimable_balance_sponsorship_removed":
                    case "claimable_balance_claimed": //?
                    case "claimable_balance_created": //?
                        //Log.e("Crypto Buddy Horizon", "Old Type = " + type);
                        // NO-OP
                        break;

                    default:
                        //Log.e("Crypto Buddy Horizon", "New Type = " + type);
                }
            }

            return nextLink;
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            return ERROR;
        }
    }

    private String processTokens(String url, CryptoAddress cryptoAddress, ArrayList<Transaction> transactionTokenArrayList) {
        if(!shouldIncludeTokens(cryptoAddress)) { return DONE; }

        String addressDataIssueJSON = WebUtil.get(url);
        if(addressDataIssueJSON == null) {
            return ERROR;
        }

        try {
            String nextLink = DONE;

            // Process assets that this account issued.
            JSONObject jsonIssue = new JSONObject(addressDataIssueJSON);
            JSONArray jsonIssueData = jsonIssue.getJSONObject("_embedded").getJSONArray("records");
            for(int i = 0; i < jsonIssueData.length(); i++) {
                // If we processed anything, then store the next link.
                nextLink = jsonIssue.getJSONObject("_links").getJSONObject("next").getString("href");

                JSONObject jsonTransaction = jsonIssueData.getJSONObject(i);

                String amount = jsonTransaction.getString("amount");

                // The asset here should never be XLM.
                String name = jsonTransaction.getString("asset_code");
                String display_name = name;
                int scale = cryptoAddress.getPrimaryCoin().getScale();
                String id = name + "-" + jsonTransaction.getString("asset_issuer");

                Token token = TokenManager.getTokenManagerFromKey("StellarTokenManager").getToken(cryptoAddress, name, name, display_name, scale, id);

                transactionTokenArrayList.add(new Transaction(new Action("Receive"), new AssetQuantity(amount, token), null, new Timestamp(null),"Issued Asset"));
                if(transactionTokenArrayList.size() == getMaxTransactions()) { return DONE; }
            }

            return nextLink;
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            return ERROR;
        }
    }
}

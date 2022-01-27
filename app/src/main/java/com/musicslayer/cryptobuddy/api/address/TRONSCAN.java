package com.musicslayer.cryptobuddy.api.address;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.network.TRX_Testnet_Nile;
import com.musicslayer.cryptobuddy.asset.network.TRX_Testnet_Shasta;
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
import java.util.ArrayList;
import java.util.Date;

public class TRONSCAN extends AddressAPI {
    public String getName() { return "TRONSCAN"; }
    public String getDisplayName() { return "TRONSCAN REST API"; }

    public boolean isSupported(CryptoAddress cryptoAddress) {
        return "TRX".equals(cryptoAddress.getCrypto().getName());
    }

    public ArrayList<AssetQuantity> getCurrentBalance(CryptoAddress cryptoAddress) {
        ArrayList<AssetQuantity> currentBalanceArrayList = new ArrayList<>();

        String baseURL;
        if(cryptoAddress.network.isMainnet()) {
            baseURL = "https://apilist.tronscan.org";
        }
        else if(cryptoAddress.network instanceof TRX_Testnet_Nile) {
            baseURL = "https://nileapi.tronscan.org";
        }
        else if(cryptoAddress.network instanceof TRX_Testnet_Shasta) {
            baseURL = "https://shastapi.tronscan.org";
        }
        else {
            return null;
        }

        String addressDataJSON = WebUtil.get(baseURL + "/api/account/wallet?address=" + cryptoAddress.address);
        if(addressDataJSON == null) {
            return null;
        }

        try {
            JSONObject json = new JSONObject(addressDataJSON);
            JSONArray jsonArray = json.getJSONArray("data");
            for(int i = 0; i < jsonArray.length(); i++) {
                JSONObject o = jsonArray.getJSONObject(i);

                int scale = o.getInt("token_decimal");
                String id = o.getString("token_id");
                int type = o.getInt("token_type");

                Crypto crypto;
                if(type == 0) {
                    crypto = cryptoAddress.getCrypto();
                }
                else if(type == 10){
                    if(!shouldIncludeTokens(cryptoAddress)) {
                        continue;
                    }

                    String name = o.getString("token_abbr");
                    String display_name = o.getString("token_name");
                    crypto = TokenManager.getTokenManagerFromKey("TronTokenManager").getToken(cryptoAddress, id, name, display_name, scale, id);
                }
                else if(type == 20){
                    if(!shouldIncludeTokens(cryptoAddress)) {
                        continue;
                    }

                    String name = o.getString("token_abbr");
                    String display_name = o.getString("token_name");
                    crypto = TokenManager.getTokenManagerFromKey("TronSmartTokenManager").getToken(cryptoAddress, id, name, display_name, scale, id);
                }
                else {
                    // Other types (NFTs)?
                    continue;
                }

                BigDecimal b = new BigDecimal(o.getString("balance"));

                currentBalanceArrayList.add(new AssetQuantity(b.toPlainString(), crypto));
            }
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
            baseURL = "https://apilist.tronscan.org";
        }
        else if(cryptoAddress.network instanceof TRX_Testnet_Nile) {
            baseURL = "https://nileapi.tronscan.org";
        }
        else if(cryptoAddress.network instanceof TRX_Testnet_Shasta) {
            baseURL = "https://shastapi.tronscan.org";
        }
        else {
            return null;
        }

        ArrayList<Transaction> transactionNormalArrayList = new ArrayList<>();
        ArrayList<Transaction> transactionTokenFromArrayList = new ArrayList<>();
        ArrayList<Transaction> transactionTokenToArrayList = new ArrayList<>();

        // Process all normal.
        for(int start = 0; ; start += 50) {
            String url = baseURL + "/api/transaction?address=" + cryptoAddress.address + "&limit=50&start=" + start;
            String status = processNormal(url, cryptoAddress, transactionNormalArrayList);

            if(ERROR.equals(status)) {
                return null;
            }
            else if(DONE.equals(status)) {
                break;
            }
        }

        // Process all tokens from.
        for(int start = 0; ; start += 50) {
            String url = baseURL + "/api/token_trc20/transfers?fromAddress=" + cryptoAddress.address + "&limit=50&start=" + start;
            String status = processTokensFrom(url, cryptoAddress, transactionTokenFromArrayList);

            if(ERROR.equals(status)) {
                return null;
            }
            else if(DONE.equals(status)) {
                break;
            }
        }

        // Process all rewards.
        for(int start = 0; ; start += 50) {
            String url = baseURL + "/api/token_trc20/transfers?toAddress=" + cryptoAddress.address + "&limit=50&start=" + start;
            String status = processTokensTo(url, cryptoAddress, transactionTokenToArrayList);

            if(ERROR.equals(status)) {
                return null;
            }
            else if(DONE.equals(status)) {
                break;
            }
        }

        // Roughly split max transactions between each type (rounding is OK).
        int splitNum = shouldIncludeTokens(cryptoAddress) ? 3 : 1;
        int splitMax = getMaxTransactions()/splitNum;

        transactionArrayList.addAll(transactionNormalArrayList.subList(0, Math.min(splitMax, transactionNormalArrayList.size())));
        transactionArrayList.addAll(transactionTokenFromArrayList.subList(0, Math.min(splitMax, transactionTokenFromArrayList.size())));
        transactionArrayList.addAll(transactionTokenToArrayList.subList(0, Math.min(splitMax, transactionTokenToArrayList.size())));

        transactionNormalArrayList.subList(0, Math.min(splitMax, transactionNormalArrayList.size())).clear();
        transactionTokenFromArrayList.subList(0, Math.min(splitMax, transactionTokenFromArrayList.size())).clear();
        transactionTokenToArrayList.subList(0, Math.min(splitMax, transactionTokenToArrayList.size())).clear();

        while(transactionNormalArrayList.size() + transactionTokenFromArrayList.size() + transactionTokenToArrayList.size() > 0) {
            if(transactionNormalArrayList.size() > 0) {
                transactionArrayList.add(transactionNormalArrayList.get(0));
                transactionNormalArrayList.remove(0);
            }
            if(transactionArrayList.size() == getMaxTransactions()) { break; }

            if(transactionTokenFromArrayList.size() > 0) {
                transactionArrayList.add(transactionTokenFromArrayList.get(0));
                transactionTokenFromArrayList.remove(0);
            }
            if(transactionArrayList.size() == getMaxTransactions()) { break; }

            if(transactionTokenToArrayList.size() > 0) {
                transactionArrayList.add(transactionTokenToArrayList.get(0));
                transactionTokenToArrayList.remove(0);
            }
            if(transactionArrayList.size() == getMaxTransactions()) { break; }
        }

        return transactionArrayList;
    }

    public String processNormal(String url, CryptoAddress cryptoAddress, ArrayList<Transaction> transactionNormalArrayList) {
        String addressDataTransactionsJSON = WebUtil.get(url);
        if(addressDataTransactionsJSON == null) {
            return ERROR;
        }

        String baseURL;
        if(cryptoAddress.network.isMainnet()) {
            baseURL = "https://apilist.tronscan.org";
        }
        else if(cryptoAddress.network instanceof TRX_Testnet_Nile) {
            baseURL = "https://nileapi.tronscan.org";
        }
        else if(cryptoAddress.network instanceof TRX_Testnet_Shasta) {
            baseURL = "https://shastapi.tronscan.org";
        }
        else {
            return ERROR;
        }

        try {
            String status = DONE;

            // Transactions
            JSONObject json = new JSONObject(addressDataTransactionsJSON);
            JSONArray jsonData = json.getJSONArray("data");
            for(int i = 0; i < jsonData.length(); i++) {
                // If there is anything to process, we may not be done yet.
                status = NOTDONE;

                JSONObject o = jsonData.getJSONObject(i);

                BigInteger block_time = new BigInteger(o.getString("timestamp"));
                Date block_time_date = new Date(block_time.longValue());

                JSONObject cost = o.getJSONObject("cost");
                if(cost.length() == 0) {
                    // Sometimes the fee information is not here, so we need to fetch it.
                    String hash = o.getString("hash");
                    String addressDataAlternateJSON = WebUtil.get(baseURL + "/api/transaction-info?hash=" + hash);
                    if(addressDataAlternateJSON != null) {
                        JSONObject o2 = new JSONObject(addressDataAlternateJSON);
                        cost = o2.getJSONObject("cost");
                    }
                    else {
                        // Indicate unknown fee?
                    }
                }

                String to = o.getString("toAddress");
                String from = o.getString("ownerAddress");

                String action;
                boolean isFee;
                if(cryptoAddress.matchesAddress(to)) {
                    action = "Receive";
                    isFee = false;
                }
                else if(cryptoAddress.matchesAddress(from)){
                    action = "Send";
                    isFee = true;
                }
                else {
                    // Nothing to process here.
                    continue;
                }

                BigDecimal energy_fee = BigDecimal.ZERO;
                if(isFee && cost.has("energy_fee")) {
                    energy_fee = new BigDecimal(cost.getString("energy_fee")).movePointLeft(cryptoAddress.getCrypto().getScale());
                }

                BigDecimal network_fee = BigDecimal.ZERO;
                if(isFee && cost.has("net_fee")) {
                    network_fee = new BigDecimal(cost.getString("net_fee")).movePointLeft(cryptoAddress.getCrypto().getScale());
                }

                if(energy_fee.compareTo(BigDecimal.ZERO) > 0) {
                    transactionNormalArrayList.add(new Transaction(new Action("Fee"), new AssetQuantity(energy_fee.toPlainString(), cryptoAddress.getCrypto()), null, new Timestamp(block_time_date),"Energy Fee"));
                    if(transactionNormalArrayList.size() == getMaxTransactions()) { return DONE; }
                }

                if(network_fee.compareTo(BigDecimal.ZERO) > 0) {
                    transactionNormalArrayList.add(new Transaction(new Action("Fee"), new AssetQuantity(network_fee.toPlainString(), cryptoAddress.getCrypto()), null, new Timestamp(block_time_date),"Network Fee"));
                    if(transactionNormalArrayList.size() == getMaxTransactions()) { return DONE; }
                }

                // If I send something to myself, just reject it!
                if(cryptoAddress.network.matchesAddress(from, to)) { continue; }

                if(!"SUCCESS".equals(o.getString("result"))) {
                    continue;
                }

                String contractType = o.getString("contractType");

                if("0".equals(contractType)) {
                    // Account Creation - Nothing more to do.
                }
                else {
                    // Transaction
                    JSONObject tokenInfo = o.getJSONObject("tokenInfo");
                    int scale = tokenInfo.getInt("tokenDecimal");
                    String tokenType = o.getString("tokenType");
                    String id = tokenInfo.getString("tokenId");

                    BigDecimal amount = new BigDecimal(o.getString("amount")).movePointLeft(scale);

                    if(amount.compareTo(BigDecimal.ZERO) <= 0) {
                        continue;
                    }

                    Crypto crypto;
                    if("_".equals(id)) {
                        crypto = cryptoAddress.getCrypto();

                        transactionNormalArrayList.add(new Transaction(new Action(action), new AssetQuantity(amount.toPlainString(), crypto), null, new Timestamp(block_time_date),"Transaction"));
                        if(transactionNormalArrayList.size() == getMaxTransactions()) { return DONE; }
                    }
                    else if("trc10".equals(tokenType)) {
                        String name = tokenInfo.getString("tokenAbbr");
                        String display_name = tokenInfo.getString("tokenName");
                        crypto = TokenManager.getTokenManagerFromKey("TronTokenManager").getToken(cryptoAddress, id, name, display_name, scale, id);

                        if(shouldIncludeTokens(cryptoAddress)) {
                            transactionNormalArrayList.add(new Transaction(new Action(action), new AssetQuantity(amount.toPlainString(), crypto), null, new Timestamp(block_time_date),"Transaction"));
                            if(transactionNormalArrayList.size() == getMaxTransactions()) { return DONE; }
                        }
                    }
                    else if("trc20".equals(tokenType)) {
                        String name = tokenInfo.getString("tokenAbbr");
                        String display_name = tokenInfo.getString("tokenName");
                        crypto = TokenManager.getTokenManagerFromKey("TronSmartTokenManager").getToken(cryptoAddress, id, name, display_name, scale, id);

                        if(shouldIncludeTokens(cryptoAddress)) {
                            transactionNormalArrayList.add(new Transaction(new Action(action), new AssetQuantity(amount.toPlainString(), crypto), null, new Timestamp(block_time_date),"Transaction"));
                            if(transactionNormalArrayList.size() == getMaxTransactions()) { return DONE; }
                        }
                    }
                    else {
                        // Don't deal with NFTs yet...
                    }
                }
            }

            return status;
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            return ERROR;
        }
    }

    public String processTokensFrom(String url, CryptoAddress cryptoAddress, ArrayList<Transaction> transactionTokenFromArrayList) {
        // TRC-20 Tokens
        if(!shouldIncludeTokens(cryptoAddress)) { return DONE; }

        String addressDataTRC20FromJSON = WebUtil.get(url);
        if(addressDataTRC20FromJSON == null) {
            return ERROR;
        }

        try {
            String status = DONE;

            // TRC20 From
            JSONObject jsonTRC20From = new JSONObject(addressDataTRC20FromJSON);
            JSONArray jsonTRC20FromData = jsonTRC20From.getJSONArray("token_transfers");
            for(int i = 0; i < jsonTRC20FromData.length(); i++) {
                // If there is anything to process, we may not be done yet.
                status = NOTDONE;

                JSONObject o = jsonTRC20FromData.getJSONObject(i);

                if(!"SUCCESS".equals(o.getString("finalResult"))) {
                    continue;
                }

                BigInteger block_time = new BigInteger(o.getString("block_ts"));
                Date block_time_date = new Date(block_time.longValue());

                JSONObject tokenInfo = o.getJSONObject("tokenInfo");

                String to = o.getString("to_address");
                String from = o.getString("from_address");

                // If I send something to myself, just reject it!
                if(cryptoAddress.network.matchesAddress(from, to)) { continue; }

                String action;
                if(cryptoAddress.matchesAddress(to)) {
                    action = "Receive";
                }
                else if(cryptoAddress.matchesAddress(from)){
                    action = "Send";
                }
                else {
                    // Nothing to process here.
                    continue;
                }

                int scale = tokenInfo.getInt("tokenDecimal");
                String tokenType = tokenInfo.getString("tokenType");
                String id = tokenInfo.getString("tokenId");

                Crypto crypto;
                if("_".equals(id)) {
                    crypto = cryptoAddress.getCrypto();
                }
                else if("trc10".equals(tokenType)) {
                    String name = tokenInfo.getString("tokenAbbr");
                    String display_name = tokenInfo.getString("tokenName");
                    crypto = TokenManager.getTokenManagerFromKey("TronTokenManager").getToken(cryptoAddress, id, name, display_name, scale, id);
                }
                else if("trc20".equals(tokenType)) {
                    String name = tokenInfo.getString("tokenAbbr");
                    String display_name = tokenInfo.getString("tokenName");

                    crypto = TokenManager.getTokenManagerFromKey("TronSmartTokenManager").getToken(cryptoAddress, id, name, display_name, scale, id);
                }
                else {
                    // Don't deal with NFTs yet...
                    continue;
                }

                BigDecimal amount = new BigDecimal(o.getString("quant")).movePointLeft(scale);

                if(amount.compareTo(BigDecimal.ZERO) > 0) {
                    transactionTokenFromArrayList.add(new Transaction(new Action(action), new AssetQuantity(amount.toPlainString(), crypto), null, new Timestamp(block_time_date),"Transaction"));
                    if(transactionTokenFromArrayList.size() == getMaxTransactions()) { return DONE; }
                }
            }

            return status;
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            return ERROR;
        }
    }

    public String processTokensTo(String url, CryptoAddress cryptoAddress, ArrayList<Transaction> transactionTokensToArrayList) {
        // TRC-20 Tokens
        if(!shouldIncludeTokens(cryptoAddress)) { return DONE; }

        String addressDataTRC20ToJSON = WebUtil.get(url);
        if(addressDataTRC20ToJSON == null) {
            return ERROR;
        }

        try {
            String status = DONE;

            // TRC20 To
            JSONObject jsonTRC20To = new JSONObject(addressDataTRC20ToJSON);
            JSONArray jsonTRC20ToData = jsonTRC20To.getJSONArray("token_transfers");
            for(int i = 0; i < jsonTRC20ToData.length(); i++) {
                // If there is anything to process, we may not be done yet.
                status = NOTDONE;

                JSONObject o = jsonTRC20ToData.getJSONObject(i);

                if(!"SUCCESS".equals(o.getString("finalResult"))) {
                    continue;
                }

                BigInteger block_time = new BigInteger(o.getString("block_ts"));
                Date block_time_date = new Date(block_time.longValue());

                JSONObject tokenInfo = o.getJSONObject("tokenInfo");

                String to = o.getString("to_address");
                String from = o.getString("from_address");

                // If I send something to myself, just reject it!
                if(cryptoAddress.network.matchesAddress(from, to)) { continue; }

                String action;
                if(cryptoAddress.matchesAddress(to)) {
                    action = "Receive";
                }
                else if(cryptoAddress.matchesAddress(from)){
                    action = "Send";
                }
                else {
                    // Nothing to process here.
                    continue;
                }

                int scale = tokenInfo.getInt("tokenDecimal");
                String tokenType = tokenInfo.getString("tokenType");
                String id = tokenInfo.getString("tokenId");

                Crypto crypto;
                if("_".equals(id)) {
                    crypto = cryptoAddress.getCrypto();
                }
                else if("trc10".equals(tokenType)) {
                    String name = tokenInfo.getString("tokenAbbr");
                    String display_name = tokenInfo.getString("tokenName");
                    crypto = TokenManager.getTokenManagerFromKey("TronTokenManager").getToken(cryptoAddress, id, name, display_name, scale, id);
                }
                else if("trc20".equals(tokenType)) {
                    String name = tokenInfo.getString("tokenAbbr");
                    String display_name = tokenInfo.getString("tokenName");

                    crypto = TokenManager.getTokenManagerFromKey("TronSmartTokenManager").getToken(cryptoAddress, id, name, display_name, scale, id);
                }
                else {
                    // Don't deal with NFTs yet...
                    continue;
                }

                BigDecimal amount = new BigDecimal(o.getString("quant")).movePointLeft(scale);

                if(amount.compareTo(BigDecimal.ZERO) > 0) {
                    transactionTokensToArrayList.add(new Transaction(new Action(action), new AssetQuantity(amount.toPlainString(), crypto), null, new Timestamp(block_time_date),"Transaction"));
                    if(transactionTokensToArrayList.size() == getMaxTransactions()) { return DONE; }
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

package com.musicslayer.cryptobuddy.api.address;

import android.util.Log;

import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.network.TRX_Testnet_Nile;
import com.musicslayer.cryptobuddy.asset.network.TRX_Testnet_Shasta;
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

        String addressDataJSON = RESTUtil.get(baseURL + "/api/account/wallet?address=" + cryptoAddress.address);
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
                    crypto = TokenManager.getTokenManagerFromKey("TronTokenManager").getOrCreateToken(id, name, display_name, scale, id);
                }
                else if(type == 20){
                    if(!shouldIncludeTokens(cryptoAddress)) {
                        continue;
                    }

                    String name = o.getString("token_abbr");
                    String display_name = o.getString("token_name");
                    crypto = TokenManager.getTokenManagerFromKey("TronSmartTokenManager").getOrCreateToken(id, name, display_name, scale, id);
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

        String addressDataTransactionsJSON = RESTUtil.get(baseURL + "/api/transaction?address=" + cryptoAddress.address + "&limit=50");
        String addressDataTransactions2JSON = RESTUtil.get(baseURL + "/api/transaction?address=" + cryptoAddress.address + "&limit=50&start=50");
        if(addressDataTransactionsJSON == null || addressDataTransactions2JSON == null) {
            return null;
        }

        try {
            // Transactions
            JSONObject json = new JSONObject(addressDataTransactionsJSON);
            JSONArray jsonData = json.getJSONArray("data");
            for(int i = 0; i < jsonData.length(); i++) {
                JSONObject o = jsonData.getJSONObject(i);

                BigInteger block_time = new BigInteger(o.getString("timestamp"));
                Date block_time_date = new Date(block_time.longValue());

                JSONObject cost = o.getJSONObject("cost");
                if(cost.length() == 0) {
                    // Sometimes the fee information is not here, so we need to fetch it.
                    String hash = o.getString("hash");
                    String addressDataAlternateJSON = RESTUtil.get(baseURL + "/api/transaction-info?hash=" + hash);
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
                if(cryptoAddress.address.equals(to)) {
                    action = "Receive";
                    isFee = false;
                }
                else if(cryptoAddress.address.equals(from)){
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
                    transactionArrayList.add(new Transaction(new Action("Fee"), new AssetQuantity(energy_fee.toPlainString(), cryptoAddress.getCrypto()), null, new Timestamp(block_time_date),"Energy Fee"));
                    if(transactionArrayList.size() == getMaxTransactions()) { return transactionArrayList; }
                }

                if(network_fee.compareTo(BigDecimal.ZERO) > 0) {
                    transactionArrayList.add(new Transaction(new Action("Fee"), new AssetQuantity(network_fee.toPlainString(), cryptoAddress.getCrypto()), null, new Timestamp(block_time_date),"Network Fee"));
                    if(transactionArrayList.size() == getMaxTransactions()) { return transactionArrayList; }
                }

                // If I send something to myself, just reject it!
                if(from.equals(to)) { continue; }

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

                        transactionArrayList.add(new Transaction(new Action(action), new AssetQuantity(amount.toPlainString(), crypto), null, new Timestamp(block_time_date),"Transaction"));
                        if(transactionArrayList.size() == getMaxTransactions()) { return transactionArrayList; }
                    }
                    else if("trc10".equals(tokenType)) {
                        String name = tokenInfo.getString("tokenAbbr");
                        String display_name = tokenInfo.getString("tokenName");
                        crypto = TokenManager.getTokenManagerFromKey("TronTokenManager").getOrCreateToken(id, name, display_name, scale, id);

                        if(shouldIncludeTokens(cryptoAddress)) {
                            transactionArrayList.add(new Transaction(new Action(action), new AssetQuantity(amount.toPlainString(), crypto), null, new Timestamp(block_time_date),"Transaction"));
                            if(transactionArrayList.size() == getMaxTransactions()) { return transactionArrayList; }
                        }
                    }
                    else if("trc20".equals(tokenType)) {
                        String name = tokenInfo.getString("tokenAbbr");
                        String display_name = tokenInfo.getString("tokenName");
                        crypto = TokenManager.getTokenManagerFromKey("TronSmartTokenManager").getOrCreateToken(id, name, display_name, scale, id);

                        if(shouldIncludeTokens(cryptoAddress)) {
                            transactionArrayList.add(new Transaction(new Action(action), new AssetQuantity(amount.toPlainString(), crypto), null, new Timestamp(block_time_date),"Transaction"));
                            if(transactionArrayList.size() == getMaxTransactions()) { return transactionArrayList; }
                        }
                    }
                    else {
                        // Don't deal with NFTs yet...
                    }
                }
            }

            JSONObject json2 = new JSONObject(addressDataTransactions2JSON);
            JSONArray json2Data = json2.getJSONArray("data");
            for(int i = 0; i < json2Data.length(); i++) {
                JSONObject o = json2Data.getJSONObject(i);

                BigInteger block_time = new BigInteger(o.getString("timestamp"));
                Date block_time_date = new Date(block_time.longValue());

                JSONObject cost = o.getJSONObject("cost");
                if(cost.length() == 0) {
                    // Sometimes the fee information is not here, so we need to fetch it.
                    String hash = o.getString("hash");
                    String addressDataAlternateJSON = RESTUtil.get(baseURL + "/api/transaction-info?hash=" + hash);
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
                if(cryptoAddress.address.equals(to)) {
                    action = "Receive";
                    isFee = false;
                }
                else if(cryptoAddress.address.equals(from)){
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
                    transactionArrayList.add(new Transaction(new Action("Fee"), new AssetQuantity(energy_fee.toPlainString(), cryptoAddress.getCrypto()), null, new Timestamp(block_time_date),"Energy Fee"));
                    if(transactionArrayList.size() == getMaxTransactions()) { return transactionArrayList; }
                }

                if(network_fee.compareTo(BigDecimal.ZERO) > 0) {
                    transactionArrayList.add(new Transaction(new Action("Fee"), new AssetQuantity(network_fee.toPlainString(), cryptoAddress.getCrypto()), null, new Timestamp(block_time_date),"Network Fee"));
                    if(transactionArrayList.size() == getMaxTransactions()) { return transactionArrayList; }
                }

                // If I send something to myself, just reject it!
                if(from.equals(to)) { continue; }

                if(!"SUCCESS".equals(o.getString("result"))) {
                    continue;
                }

                String contractType = o.getString("contractType");

                if("0".equals(contractType)) {
                    // Account Creation - Nothing more to do.
                }
                else if("1".equals(contractType)){
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

                        transactionArrayList.add(new Transaction(new Action(action), new AssetQuantity(amount.toPlainString(), crypto), null, new Timestamp(block_time_date),"Transaction"));
                        if(transactionArrayList.size() == getMaxTransactions()) { return transactionArrayList; }
                    }
                    else if("trc10".equals(tokenType)) {
                        String name = tokenInfo.getString("tokenAbbr");
                        String display_name = tokenInfo.getString("tokenName");
                        crypto = TokenManager.getTokenManagerFromKey("TronTokenManager").getOrCreateToken(id, name, display_name, scale, id);

                        if(shouldIncludeTokens(cryptoAddress)) {
                            transactionArrayList.add(new Transaction(new Action(action), new AssetQuantity(amount.toPlainString(), crypto), null, new Timestamp(block_time_date),"Transaction"));
                            if(transactionArrayList.size() == getMaxTransactions()) { return transactionArrayList; }
                        }
                    }
                    else if("trc20".equals(tokenType)) {
                        String name = tokenInfo.getString("tokenAbbr");
                        String display_name = tokenInfo.getString("tokenName");
                        crypto = TokenManager.getTokenManagerFromKey("TronSmartTokenManager").getOrCreateToken(id, name, display_name, scale, id);

                        if(shouldIncludeTokens(cryptoAddress)) {
                            transactionArrayList.add(new Transaction(new Action(action), new AssetQuantity(amount.toPlainString(), crypto), null, new Timestamp(block_time_date),"Transaction"));
                            if(transactionArrayList.size() == getMaxTransactions()) { return transactionArrayList; }
                        }
                    }
                    else {
                        // Don't deal with NFTs yet...
                    }
                }
                else {
                    // New type?
                    Log.e("Crypto Buddy", "X");
                }
            }
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            return null;
        }

        if(shouldIncludeTokens(cryptoAddress)) {
            String addressDataTRC20FromJSON = RESTUtil.get(baseURL + "/api/token_trc20/transfers?fromAddress=" + cryptoAddress.address + "&limit=50");
            String addressDataTRC20ToJSON = RESTUtil.get(baseURL + "/api/token_trc20/transfers?toAddress=" + cryptoAddress.address + "&limit=50");
            if(addressDataTRC20FromJSON == null || addressDataTRC20ToJSON == null) {
                return null;
            }

            try {
                // TRC20 From
                JSONObject jsonTRC20From = new JSONObject(addressDataTRC20FromJSON);
                JSONArray jsonTRC20FromData = jsonTRC20From.getJSONArray("token_transfers");
                for(int i = 0; i < jsonTRC20FromData.length(); i++) {
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
                    if(from.equals(to)) { continue; }

                    String action;
                    if(cryptoAddress.address.equals(to)) {
                        action = "Receive";
                    }
                    else if(cryptoAddress.address.equals(from)){
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
                        crypto = TokenManager.getTokenManagerFromKey("TronTokenManager").getOrCreateToken(id, name, display_name, scale, id);
                    }
                    else if("trc20".equals(tokenType)) {
                        String name = tokenInfo.getString("tokenAbbr");
                        String display_name = tokenInfo.getString("tokenName");

                        crypto = TokenManager.getTokenManagerFromKey("TronSmartTokenManager").getOrCreateToken(id, name, display_name, scale, id);
                    }
                    else {
                        // Don't deal with NFTs yet...
                        continue;
                    }

                    BigDecimal amount = new BigDecimal(o.getString("quant")).movePointLeft(scale);

                    if(amount.compareTo(BigDecimal.ZERO) > 0) {
                        transactionArrayList.add(new Transaction(new Action(action), new AssetQuantity(amount.toPlainString(), crypto), null, new Timestamp(block_time_date),"Transaction"));
                        if(transactionArrayList.size() == getMaxTransactions()) { return transactionArrayList; }
                    }
                }

                // TRC20 To
                JSONObject jsonTRC20To = new JSONObject(addressDataTRC20ToJSON);
                JSONArray jsonTRC20ToData = jsonTRC20To.getJSONArray("token_transfers");
                for(int i = 0; i < jsonTRC20ToData.length(); i++) {
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
                    if(from.equals(to)) { continue; }

                    String action;
                    if(cryptoAddress.address.equals(to)) {
                        action = "Receive";
                    }
                    else if(cryptoAddress.address.equals(from)){
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
                        crypto = TokenManager.getTokenManagerFromKey("TronTokenManager").getOrCreateToken(id, name, display_name, scale, id);
                    }
                    else if("trc20".equals(tokenType)) {
                        String name = tokenInfo.getString("tokenAbbr");
                        String display_name = tokenInfo.getString("tokenName");

                        crypto = TokenManager.getTokenManagerFromKey("TronSmartTokenManager").getOrCreateToken(id, name, display_name, scale, id);
                    }
                    else {
                        // Don't deal with NFTs yet...
                        continue;
                    }

                    BigDecimal amount = new BigDecimal(o.getString("quant")).movePointLeft(scale);

                    if(amount.compareTo(BigDecimal.ZERO) > 0) {
                        transactionArrayList.add(new Transaction(new Action(action), new AssetQuantity(amount.toPlainString(), crypto), null, new Timestamp(block_time_date),"Transaction"));
                        if(transactionArrayList.size() == getMaxTransactions()) { return transactionArrayList; }
                    }
                }
            }
            catch(Exception e) {
                ThrowableUtil.processThrowable(e);
                return null;
            }
        }

        return transactionArrayList;
    }
}

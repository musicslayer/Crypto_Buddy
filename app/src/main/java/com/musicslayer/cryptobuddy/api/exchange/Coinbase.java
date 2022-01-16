package com.musicslayer.cryptobuddy.api.exchange;

import android.app.Activity;
import android.content.DialogInterface;

import com.musicslayer.cryptobuddy.api.address.CryptoAddress;
import com.musicslayer.cryptobuddy.asset.exchange.Exchange;
import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.dialog.BaseDialogFragment;
import com.musicslayer.cryptobuddy.dialog.ExchangeWebViewDialog;
import com.musicslayer.cryptobuddy.dialog.ProgressDialog;
import com.musicslayer.cryptobuddy.dialog.ProgressDialogFragment;
import com.musicslayer.cryptobuddy.serialize.Serialization;
import com.musicslayer.cryptobuddy.transaction.AssetQuantity;
import com.musicslayer.cryptobuddy.transaction.Transaction;
import com.musicslayer.cryptobuddy.util.RESTUtil;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;
import com.musicslayer.cryptobuddy.util.ToastUtil;

import org.json.JSONObject;

import java.util.ArrayList;

public class Coinbase extends ExchangeAPI {
    public String getName() { return "Coinbase"; }
    public String getDisplayName() { return "Coinbase REST API V2"; }

    public String code;

    public boolean isSupported(Exchange exchange) {
        return "Coinbase".equals(exchange.getName());
    }

    public void authorize(Activity activity, ExchangeAPI.AuthorizationListener L) {
        ProgressDialogFragment progressDialogFragment = ProgressDialogFragment.newInstance(ProgressDialog.class);
        progressDialogFragment.setOnShowListener(new CrashDialogInterface.CrashOnShowListener(activity) {
            @Override
            public void onShowImpl(DialogInterface dialog) {
                // Use code to get access token that we can use to request user info.
                String token = null;

                String body = "{" +
                        "\"grant_type\": \"authorization_code\"," +
                        "\"code\": \"" + code + "\"," +
                        "\"client_id\": \"6f45b6368b73c3cc30433361ecccc7b59d22413c99d822edc7763107cb776a4e\"," +
                        "\"client_secret\": \"2047cba7d7a4eba68652ff9ec8bf6a110a5004365eadcccf7773e2f6b004af85\"," +
                        "\"redirect_uri\": \"urn:ietf:wg:oauth:2.0:oob\"" +
                        "}";

                String authResponse = RESTUtil.post("https://api.coinbase.com/oauth/token", body);
                if(authResponse != null) {
                    try {
                        JSONObject authResponseJSON = new JSONObject(authResponse);
                        token = authResponseJSON.getString("access_token");
                    }
                    catch(Exception ignored) {
                    }
                }

                ProgressDialogFragment.setValue(Serialization.string_serialize(token));
            }
        });
        progressDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(activity) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                String token = Serialization.string_deserialize(ProgressDialogFragment.getValue());

                if(token == null) {
                    ToastUtil.showToast(activity, "no_exchange_authorization");
                }
                else {
                    L.onAuthorization("Coinbase", token);
                }
            }
        });
        progressDialogFragment.restoreListeners(activity, "progress");

        String authURLBase = "https://www.coinbase.com/oauth/authorize/";
        String authURL = "https://www.coinbase.com/oauth/authorize?client_id=6f45b6368b73c3cc30433361ecccc7b59d22413c99d822edc7763107cb776a4e&redirect_uri=urn:ietf:wg:oauth:2.0:oob&response_type=code&scope=wallet:transactions:read,wallet:accounts:read&account=all";
        BaseDialogFragment exchangeWebViewDialogFragment = BaseDialogFragment.newInstance(ExchangeWebViewDialog.class, "Authorize Coinbase", authURLBase, authURL);

        exchangeWebViewDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(activity) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ExchangeWebViewDialog)dialog).isComplete) {
                    code = ((ExchangeWebViewDialog)dialog).user_CODE;
                    progressDialogFragment.show(activity, "progress");
                }
            }
        });
        exchangeWebViewDialogFragment.restoreListeners(activity, "web_view");

        exchangeWebViewDialogFragment.show(activity, "web_view");
    }

    public ArrayList<AssetQuantity> getCurrentBalance(String token) {
        ArrayList<AssetQuantity> currentBalanceArrayList = new ArrayList<>();

        return currentBalanceArrayList;
    }

    public String processBalance(String url, String token, ArrayList<AssetQuantity> currentBalanceArrayList) {
        String addressDataJSON = RESTUtil.get(url);
        if(addressDataJSON == null) {
            return ERROR;
        }

        try {
            String status = DONE;

            return status;
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            return ERROR;
        }
    }

    public ArrayList<Transaction> getTransactions(String token) {
        ArrayList<Transaction> transactionArrayList = new ArrayList<>();

        return transactionArrayList;
    }

    // Return null for error/no data, DONE to stop and any other non-null string to keep going.
    private String processTransaction(String url, String token, ArrayList<Transaction> transactionArrayList) {
        return DONE;
    }
}

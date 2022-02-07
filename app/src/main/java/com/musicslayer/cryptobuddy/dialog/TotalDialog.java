package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.api.price.CryptoPrice;
import com.musicslayer.cryptobuddy.api.price.PriceData;
import com.musicslayer.cryptobuddy.asset.Asset;
import com.musicslayer.cryptobuddy.asset.fiat.Fiat;
import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.state.StateObj;
import com.musicslayer.cryptobuddy.transaction.Transaction;
import com.musicslayer.cryptobuddy.serialize.Serialization;
import com.musicslayer.cryptobuddy.transaction.TransactionData;
import com.musicslayer.cryptobuddy.util.ToastUtil;
import com.musicslayer.cryptobuddy.view.asset.SelectAndSearchView;

import java.util.ArrayList;

public class TotalDialog extends BaseDialog {
    ArrayList<Transaction> filteredTransactionArrayList;

    public TotalDialog(Activity activity) {
        super(activity);
        this.filteredTransactionArrayList = StateObj.filteredTransactionArrayList;
    }

    public int getBaseViewID() {
        return R.id.total_dialog;
    }

    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_total);

        if(savedInstanceState == null) {
            StateObj.priceData = null;
            StateObj.transactionData = new TransactionData(filteredTransactionArrayList);
        }

        SelectAndSearchView fssv = findViewById(R.id.total_dialog_fiatSelectAndSearchView);
        fssv.setIncludesFiat(true);
        fssv.setIncludesCoin(false);
        fssv.setIncludesToken(false);
        fssv.setCompleteOptions();
        fssv.chooseFiat();

        ProgressDialogFragment progressDialogFragment = ProgressDialogFragment.newInstance(ProgressDialog.class);
        progressDialogFragment.setOnShowListener(new CrashDialogInterface.CrashOnShowListener(this.activity) {
            @Override
            public void onShowImpl(DialogInterface dialog) {
                ProgressDialogFragment.updateProgressTitle("Retrieving Fiat Values...");

                ArrayList<Asset> assetKeySet = new ArrayList<>(StateObj.transactionData.netTransactionsMap.keySet());
                Fiat priceFiat = (Fiat)fssv.getChosenAsset();
                CryptoPrice cryptoPrice = new CryptoPrice(assetKeySet, priceFiat);

                PriceData newPriceData = PriceData.getPriceData(cryptoPrice);

                ProgressDialogFragment.setValue(Serialization.serialize(newPriceData));
            }
        });

        progressDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this.activity) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                PriceData newPriceData = Serialization.deserialize(ProgressDialogFragment.getValue(), PriceData.class);

                if(!newPriceData.isPriceFull()) {
                    ToastUtil.showToast(activity,"incomplete_price_data");
                }

                StateObj.priceData = newPriceData;

                updateLayout();
            }
        });
        progressDialogFragment.restoreListeners(this.activity, "progress");

        Button B_PRICES = findViewById(R.id.total_dialog_priceButton);
        B_PRICES.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                if(filteredTransactionArrayList.isEmpty()) {
                    ToastUtil.showToast(activity, "no_transactions_found");
                    return;
                }
                else if(fssv.getChosenAsset() == null) {
                    ToastUtil.showToast(activity,"must_choose_assets");
                }

                progressDialogFragment.show(TotalDialog.this.activity, "progress");
            }
        });

        updateLayout();
    }

    public void updateLayout() {
        TextView T = findViewById(R.id.total_dialog_textView);
        T.setText(Html.fromHtml(StateObj.transactionData.getNetSumsInfo(StateObj.priceData, true)));
    }
}
package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.musicslayer.cryptobuddy.api.address.AddressData;
import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.crash.CrashAdapterView;
import com.musicslayer.cryptobuddy.transaction.AssetQuantity;
import com.musicslayer.cryptobuddy.view.BorderedSpinnerView;

import java.util.ArrayList;

public class AddressInfoDialog extends BaseDialog {
    ArrayList<AddressData> addressDataArrayList;

    public AddressInfoDialog(Activity activity, ArrayList<AddressData> addressDataArrayList) {
        super(activity);
        this.addressDataArrayList = addressDataArrayList;
    }

    public int getBaseViewID() {
        return R.id.address_info_dialog;
    }

    public void createLayout() {
        setContentView(R.layout.dialog_address_info);

        TextView T = findViewById(R.id.address_info_dialog_textView);

        ArrayList<String> options = new ArrayList<>();
        for(AddressData addressData : addressDataArrayList) {
            options.add(addressData.cryptoAddress.toString());
        }

        BorderedSpinnerView bsv = findViewById(R.id.address_info_dialog_spinner);
        bsv.setOptions(options);
        bsv.setOnItemSelectedListener(new CrashAdapterView.CrashOnItemSelectedListener(this.activity) {
            public void onNothingSelectedImpl(AdapterView<?> parent) {}
            public void onItemSelectedImpl(AdapterView<?> parent, View view, int pos, long id) {
                AddressData addressData = addressDataArrayList.get(pos);
                T.setText(getInfoString(addressData));
            }
        });

        if(addressDataArrayList.size() == 1) {
            bsv.setVisibility(View.GONE);
        }

        if(addressDataArrayList.size() == 0) {
            bsv.setVisibility(View.GONE);
            T.setText("No addresses found.");
        }
    }

    public String getInfoString(AddressData addressData) {
        StringBuilder s = new StringBuilder("Address = " + addressData.cryptoAddress.toString());

        if(addressData.addressAPI_transactions == null || addressData.transactionArrayList == null) {
            s.append("\n(Transaction information cannot be obtained at this time.)");
        }
        else {
            s.append("\nTransaction Data Source = ").append(addressData.addressAPI_transactions.getDisplayName());
            s.append("\nNumber of Transactions = ").append(addressData.transactionArrayList.size());
        }

        if(addressData.addressAPI_currentBalance == null || addressData.currentBalanceArrayList == null) {
            s.append("\n(Current balance information cannot be obtained at this time.)");
        }
        else {
            s.append("\nCurrent Balance Data Source = ").append(addressData.addressAPI_currentBalance.getDisplayName());

            if(addressData.currentBalanceArrayList.isEmpty()) {
                s.append("\nNo Current Balances");
            }
            else {
                s.append("\nCurrent Balances:");
                for(AssetQuantity a : addressData.currentBalanceArrayList) {
                    s.append("\n    ").append(a.toString());
                }
            }
        }

        return s.toString();
    }
}
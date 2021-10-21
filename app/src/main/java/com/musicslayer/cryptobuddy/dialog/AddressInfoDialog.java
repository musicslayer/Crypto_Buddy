package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.widget.TextView;

import com.musicslayer.cryptobuddy.api.address.AddressData;
import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.transaction.AssetQuantity;

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

        StringBuilder s = new StringBuilder();
        if(addressDataArrayList.size() == 0) {
            s = new StringBuilder("No addresses found.");
        }
        else {
            for(AddressData addressData : addressDataArrayList) {
                s.append(getInfoString(addressData)).append("\n\n");
            }
        }

        TextView T = findViewById(R.id.address_info_dialog_textView);
        T.setText(s.toString());
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
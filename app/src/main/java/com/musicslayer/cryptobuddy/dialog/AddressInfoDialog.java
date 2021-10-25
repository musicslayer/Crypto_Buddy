package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.musicslayer.cryptobuddy.activity.AddressExplorerActivity;
import com.musicslayer.cryptobuddy.activity.AddressPortfolioExplorerActivity;
import com.musicslayer.cryptobuddy.api.address.AddressData;
import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.api.address.CryptoAddress;
import com.musicslayer.cryptobuddy.crash.CrashAdapterView;
import com.musicslayer.cryptobuddy.util.HashMapUtil;
import com.musicslayer.cryptobuddy.view.BorderedSpinnerView;

import java.util.ArrayList;
import java.util.HashMap;

public class AddressInfoDialog extends BaseDialog {
    ArrayList<CryptoAddress> cryptoAddressArrayList;
    HashMap<CryptoAddress, AddressData> addressDataMap;

/*
    public AddressInfoDialog(Activity activity, ArrayList<CryptoAddress> cryptoAddressArrayList, HashMap<CryptoAddress, AddressData> addressDataMap) {
        super(activity);
        this.cryptoAddressArrayList = cryptoAddressArrayList;
        this.addressDataMap = addressDataMap;
    }

 */

    public AddressInfoDialog(Activity activity, ArrayList<CryptoAddress> cryptoAddressArrayList) {
        super(activity);
        this.cryptoAddressArrayList = cryptoAddressArrayList;

        if(activity instanceof AddressExplorerActivity) {
            this.addressDataMap = ((AddressExplorerActivity)activity).activityStateObj[0].addressDataMap;
        }
        else if(activity instanceof AddressPortfolioExplorerActivity) {
            this.addressDataMap = ((AddressPortfolioExplorerActivity)activity).activityStateObj[0].addressDataMap;
        }
    }

    public int getBaseViewID() {
        return R.id.address_info_dialog;
    }

    public void createLayout() {
        setContentView(R.layout.dialog_address_info);

        TextView T = findViewById(R.id.address_info_dialog_textView);

        ArrayList<String> options = new ArrayList<>();
        for(CryptoAddress cryptoAddress : cryptoAddressArrayList) {
            options.add(cryptoAddress.toString());
        }

        BorderedSpinnerView bsv = findViewById(R.id.address_info_dialog_spinner);
        bsv.setOptions(options);
        bsv.setOnItemSelectedListener(new CrashAdapterView.CrashOnItemSelectedListener(this.activity) {
            public void onNothingSelectedImpl(AdapterView<?> parent) {}
            public void onItemSelectedImpl(AdapterView<?> parent, View view, int pos, long id) {
                CryptoAddress cryptoAddress = cryptoAddressArrayList.get(pos);
                AddressData addressData = HashMapUtil.getValueFromMap(addressDataMap, cryptoAddress);
                T.setText(addressData.getInfoString());
            }
        });

        if(cryptoAddressArrayList.size() == 1) {
            bsv.setVisibility(View.GONE);
        }

        if(cryptoAddressArrayList.size() == 0) {
            bsv.setVisibility(View.GONE);
            T.setText("No addresses found.");
        }
    }
}
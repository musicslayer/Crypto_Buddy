package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.api.address.AddressData;
import com.musicslayer.cryptobuddy.api.address.CryptoAddress;
import com.musicslayer.cryptobuddy.state.StateObj;

import java.util.ArrayList;
import java.util.HashMap;

public class AddressProblemDialog extends BaseDialog {
    ArrayList<CryptoAddress> cryptoAddressArrayList;
    HashMap<CryptoAddress, AddressData> addressDataMap;

    public AddressProblemDialog(Activity activity, ArrayList<CryptoAddress> cryptoAddressArrayList) {
        super(activity);
        this.cryptoAddressArrayList = cryptoAddressArrayList;
        this.addressDataMap = StateObj.addressDataMap;
    }

    public int getBaseViewID() {
        return R.id.address_problem_dialog;
    }

    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_address_problem);

        // To keep things simple, do not separate problem info by address. Just combine it all into one piece of text.
        // Also, right now problem info is only based on the assets, but in the future the address data may be used.
        StringBuilder infoText = new StringBuilder();
        ArrayList<String> seenNames = new ArrayList<>();
        boolean isFirst = true;

        for(CryptoAddress cryptoAddress : cryptoAddressArrayList) {
            if(seenNames.contains(cryptoAddress.getPrimaryCoin().getKey())) { continue; }

            AddressData addressData = addressDataMap.get(cryptoAddress);

            String info = addressData.getProblem();
            if(info != null) {
                // For first info, do not put new lines.
                if(!isFirst) {
                    infoText.append("\n\n");
                }

                isFirst = false;
                infoText.append(info);
            }

            seenNames.add(cryptoAddress.getPrimaryCoin().getKey());
        }

        TextView T = findViewById(R.id.address_problem_dialog_textView);
        T.setText(infoText.toString());
    }
}
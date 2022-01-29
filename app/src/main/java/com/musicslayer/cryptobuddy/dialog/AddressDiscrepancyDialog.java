package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.api.address.AddressData;
import com.musicslayer.cryptobuddy.api.address.CryptoAddress;
import com.musicslayer.cryptobuddy.asset.Asset;
import com.musicslayer.cryptobuddy.crash.CrashAdapterView;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.state.StateObj;
import com.musicslayer.cryptobuddy.transaction.AssetAmount;
import com.musicslayer.cryptobuddy.transaction.AssetQuantity;
import com.musicslayer.cryptobuddy.util.HashMapUtil;
import com.musicslayer.cryptobuddy.util.HelpUtil;
import com.musicslayer.cryptobuddy.view.BorderedSpinnerView;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;

public class AddressDiscrepancyDialog extends BaseDialog {
    ArrayList<CryptoAddress> cryptoAddressArrayList;
    HashMap<CryptoAddress, AddressData> addressDataMap;

    public AddressDiscrepancyDialog(Activity activity, ArrayList<CryptoAddress> cryptoAddressArrayList) {
        super(activity);
        this.cryptoAddressArrayList = cryptoAddressArrayList;
        this.addressDataMap = StateObj.addressDataMap;
    }

    public int getBaseViewID() {
        return R.id.address_discrepancy_dialog;
    }

    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_address_discrepancy);

        TextView T = findViewById(R.id.address_discrepancy_dialog_assetTextView);

        ImageButton helpButton = findViewById(R.id.address_discrepancy_dialog_helpButton);
        helpButton.setOnClickListener(new CrashView.CrashOnClickListener(activity) {
            @Override
            public void onClickImpl(View view) {
                HelpUtil.showHelp(activity, R.raw.help_discrepancy);
            }
        });

        ArrayList<String> options = new ArrayList<>();

        for(CryptoAddress cryptoAddress : cryptoAddressArrayList) {
            // Only add option if that address has a discrepancy.
            AddressData addressData = HashMapUtil.getValueFromMap(addressDataMap, cryptoAddress);
            if(addressData.hasDiscrepancy()) {
                options.add(cryptoAddress.toString());
            }
        }

        BorderedSpinnerView bsv = findViewById(R.id.address_discrepancy_dialog_spinner);
        bsv.setOptions(options);
        bsv.setOnItemSelectedListener(new CrashAdapterView.CrashOnItemSelectedListener(this.activity) {
            public void onNothingSelectedImpl(AdapterView<?> parent) {}
            public void onItemSelectedImpl(AdapterView<?> parent, View view, int pos, long id) {
                CryptoAddress cryptoAddress = cryptoAddressArrayList.get(pos); // TODO Doesn't line up since we removed elements from options...
                AddressData addressData = HashMapUtil.getValueFromMap(addressDataMap, cryptoAddress);

                // For each non-zero entry, display the discrepancy.
                // We do not assume here that any address will have a discrepancy, even though currently we know at least one will.
                HashMap<Asset, AssetAmount> delta = addressData.getDiscrepancyMap();

                StringBuilder s = new StringBuilder();
                s.append("Address = ").append(cryptoAddress.toString()).append("\n");

                boolean hasDiscrepancy = false;
                for(Asset asset : delta.keySet()) {
                    AssetAmount assetAmount = delta.get(asset);
                    AssetQuantity assetQuantity = new AssetQuantity(assetAmount, asset);

                    if(assetAmount.amount.compareTo(BigDecimal.ZERO) != 0) {
                        hasDiscrepancy = true;
                        s.append("\n").append(assetQuantity);
                    }
                }

                if(!hasDiscrepancy) {
                    s.append("\nThis address has no discrepancies.");
                }

                T.setText(s.toString());
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
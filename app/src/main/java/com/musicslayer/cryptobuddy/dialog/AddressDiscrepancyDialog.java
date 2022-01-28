package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.api.address.AddressData;
import com.musicslayer.cryptobuddy.api.address.CryptoAddress;
import com.musicslayer.cryptobuddy.asset.Asset;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.state.StateObj;
import com.musicslayer.cryptobuddy.transaction.AssetAmount;
import com.musicslayer.cryptobuddy.transaction.AssetQuantity;
import com.musicslayer.cryptobuddy.util.HelpUtil;

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

        ImageButton helpButton = findViewById(R.id.address_discrepancy_dialog_helpButton);
        helpButton.setOnClickListener(new CrashView.CrashOnClickListener(activity) {
            @Override
            public void onClickImpl(View view) {
                HelpUtil.showHelp(activity, R.raw.help_discrepancy);
            }
        });

        // TODO Allow other addresses.
        CryptoAddress cryptoAddress = cryptoAddressArrayList.get(0);
        AddressData addressData = addressDataMap.get(cryptoAddress);

        HashMap<Asset, AssetAmount> delta = addressData.getDiscrepancyMap();

        // For each non-zero entry, display the discrepancy.
        // This dialog will only be displayed if there is at least one discrepancy in some address, but any particular address may not have one.
        StringBuilder s = new StringBuilder();
        boolean hasDiscrepancy = false;
        for(Asset asset : delta.keySet()) {
            AssetAmount assetAmount = delta.get(asset);
            AssetQuantity assetQuantity = new AssetQuantity(assetAmount, asset);

            if(assetAmount.amount.compareTo(BigDecimal.ZERO) != 0) {
                hasDiscrepancy = true;
                s.append("\n").append(assetQuantity.toString());
            }
        }

        if(!hasDiscrepancy) {
            s.append("\nThis address has no discrepancies.");
        }

        TextView T = findViewById(R.id.address_discrepancy_dialog_assetTextView);
        T.setText(s.toString());
    }
}
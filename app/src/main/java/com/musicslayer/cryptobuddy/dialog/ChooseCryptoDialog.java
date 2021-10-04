package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.view.View;
import android.widget.Button;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.api.address.CryptoAddress;
import com.musicslayer.cryptobuddy.asset.network.Network;
import com.musicslayer.cryptobuddy.crash.CrashOnClickListener;
import com.musicslayer.cryptobuddy.view.BorderedSpinnerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ChooseCryptoDialog extends BaseDialog {
    public ArrayList<CryptoAddress> cryptoAddressArrayList;

    // Info that the user is providing.
    public CryptoAddress user_CRYPTOADDRESS;

    public ChooseCryptoDialog(Activity activity, ArrayList<CryptoAddress> cryptoAddressArrayList) {
        super(activity);
        this.cryptoAddressArrayList = cryptoAddressArrayList;
    }

    public int getBaseViewID() {
        return R.id.choose_crypto_dialog;
    }

    public void createLayout() {
        setContentView(R.layout.dialog_choose_crypto);

        ArrayList<CryptoAddress> sortedCryptoAddressArrayList = new ArrayList<>(cryptoAddressArrayList);

        // Sort by network
        Collections.sort(sortedCryptoAddressArrayList, new Comparator<CryptoAddress>() {
            @Override
            public int compare(CryptoAddress a, CryptoAddress b) {
                return Network.compare(a.network, b.network);
            }
        });

        ArrayList<String> options = new ArrayList<>();
        for(CryptoAddress ca : sortedCryptoAddressArrayList) {
            options.add(ca.network.getDisplayName());
        }

        BorderedSpinnerView bsv = findViewById(R.id.choose_crypto_dialog_spinner);
        bsv.setOptions(options);

        Button B = findViewById(R.id.choose_crypto_dialog_button);
        B.setOnClickListener(new CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                user_CRYPTOADDRESS = sortedCryptoAddressArrayList.get(bsv.spinner.getSelectedItemPosition());

                isComplete = true;
                dismiss();
            }
        });
    }
}

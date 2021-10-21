package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.view.View;
import android.widget.Button;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.api.address.CryptoAddress;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.view.BorderedSpinnerView;

import java.util.ArrayList;

// TODO This should be checkboxes, so people can multi-select.

public class AddressFilterDialog extends BaseDialog {
    Integer filterIndex;
    public ArrayList<CryptoAddress> cryptoAddressArrayList;

    // Info that the user is providing.
    public int user_INDEX;

    public AddressFilterDialog(Activity activity, Integer filterIndex, ArrayList<CryptoAddress> cryptoAddressArrayList) {
        super(activity);
        this.filterIndex = filterIndex;
        this.cryptoAddressArrayList = cryptoAddressArrayList;
    }

    public int getBaseViewID() {
        return R.id.address_filter_dialog;
    }

    public void createLayout() {
        setContentView(R.layout.dialog_address_filter);

        ArrayList<String> options = new ArrayList<>();
        options.add("(ALL)");
        for(CryptoAddress cryptoAddress : cryptoAddressArrayList) {
            options.add(cryptoAddress.toString());
        }

        BorderedSpinnerView bsv = findViewById(R.id.address_filter_dialog_spinner);
        bsv.setOptions(options);

        // Shift because of "all" option.
        bsv.setSelection(filterIndex + 1);

        Button B = findViewById(R.id.address_filter_dialog_button);
        B.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                // Shift because of "all" option.
                user_INDEX = bsv.spinner.getSelectedItemPosition() - 1;

                isComplete = true;
                dismiss();
            }
        });
    }
}

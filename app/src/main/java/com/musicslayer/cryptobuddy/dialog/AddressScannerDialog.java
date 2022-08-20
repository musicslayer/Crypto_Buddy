package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.api.address.AddressData;
import com.musicslayer.cryptobuddy.api.address.CryptoAddress;
import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.data.persistent.app.PersistentAppDataStore;
import com.musicslayer.cryptobuddy.data.persistent.app.TokenManagerList;
import com.musicslayer.cryptobuddy.util.ToastUtil;

public class AddressScannerDialog extends BaseDialog {
    CryptoAddress cryptoAddress;

    public AddressScannerDialog(Activity activity) {
        super(activity);
    }

    public int getBaseViewID() {
        return R.id.address_scanner_dialog;
    }

    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_address_scanner);

        BaseDialogFragment chooseAddressDialogFragment = BaseDialogFragment.newInstance(ChooseAddressDialog.class);
        chooseAddressDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(activity) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ChooseAddressDialog)dialog).isComplete) {
                    // Always include tokens regardless of user choice.
                    cryptoAddress = ((ChooseAddressDialog)dialog).user_CRYPTOADDRESS;
                    cryptoAddress.includeTokens = true;

                    updateLayout();
                }
            }
        });
        chooseAddressDialogFragment.restoreListeners(activity, "address");

        Button B_ADDRESS_EXPLORER = findViewById(R.id.address_scanner_dialog_addressButton);
        B_ADDRESS_EXPLORER.setOnClickListener(new CrashView.CrashOnClickListener(activity) {
            @Override
            public void onClickImpl(View view) {
                chooseAddressDialogFragment.show(activity, "address");
            }
        });

        ProgressDialogFragment scanProgressDialogFragment = ProgressDialogFragment.newInstance(ProgressDialog.class);
        scanProgressDialogFragment.setOnShowListener(new CrashDialogInterface.CrashOnShowListener(activity) {
            @Override
            public void onShowImpl(DialogInterface dialog) {
                ProgressDialogFragment.updateProgressTitle("Scanning For Tokens...");

                // Search the address balances and transactions for any tokens.
                // If any new tokens are found, save them here.
                AddressData.getAllData(cryptoAddress);
                PersistentAppDataStore.getInstance(TokenManagerList.class).saveAllData();
            }
        });
        scanProgressDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(activity) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                AddressScannerDialog.this.activity.updateLayout();
            }
        });
        scanProgressDialogFragment.restoreListeners(activity, "progress_scan");

        AppCompatButton scanButton = findViewById(R.id.address_scanner_dialog_scanButton);
        scanButton.setOnClickListener(new CrashView.CrashOnClickListener(activity) {
            @Override
            public void onClickImpl(View view) {
                if(cryptoAddress == null) {
                    ToastUtil.showToast("must_choose_address");
                }
                else {
                    scanProgressDialogFragment.show(activity, "progress_scan");
                }
            }
        });

        updateLayout();
    }

    public void updateLayout() {
        TextView T_ADDRESS = findViewById(R.id.address_scanner_dialog_addressTextView);

        if(cryptoAddress == null) {
            T_ADDRESS.setVisibility(View.GONE);
        }
        else {
            // Show the address but don't include the coins/tokens part.
            T_ADDRESS.setVisibility(View.VISIBLE);
            T_ADDRESS.setText(cryptoAddress.toSimpleString());
        }
    }

    @Override
    public Bundle onSaveInstanceStateImpl(Bundle bundle) {
        bundle.putParcelable("cryptoAddress", cryptoAddress);
        return bundle;
    }

    @Override
    public void onRestoreInstanceStateImpl(Bundle bundle) {
        if(bundle != null) {
            cryptoAddress = bundle.getParcelable("cryptoAddress");
        }
    }
}

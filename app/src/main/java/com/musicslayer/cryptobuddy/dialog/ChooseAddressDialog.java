package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.api.address.CryptoAddress;
import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.persistence.AddressHistory;
import com.musicslayer.cryptobuddy.persistence.AddressHistoryObj;
import com.musicslayer.cryptobuddy.persistence.Purchases;
import com.musicslayer.cryptobuddy.settings.setting.NetworksSetting;
import com.musicslayer.cryptobuddy.util.ClipboardUtil;
import com.musicslayer.cryptobuddy.util.HelpUtil;
import com.musicslayer.cryptobuddy.util.PermissionUtil;
import com.musicslayer.cryptobuddy.util.ToastUtil;
import com.musicslayer.cryptobuddy.view.red.AnythingEditText;

import java.util.ArrayList;

public class ChooseAddressDialog extends BaseDialog {
    public boolean includeTokens = Purchases.isUnlockTokensPurchased();

    public CryptoAddress user_CRYPTOADDRESS;

    public ChooseAddressDialog(Activity activity) {
        super(activity);
    }

    public int getBaseViewID() {
        return R.id.choose_exchange_dialog;
    }

    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_choose_address);

        ImageButton helpButton = findViewById(R.id.choose_exchange_dialog_helpButton);
        helpButton.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            @Override
            public void onClickImpl(View view) {
                HelpUtil.showHelp(ChooseAddressDialog.this.activity, R.raw.help_choose_address);
            }
        });

        if(Purchases.isUnlockTokensPurchased()) {
            Button B_TOGGLE = findViewById(R.id.choose_address_dialog_toggleButton);
            B_TOGGLE.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
                public void onClickImpl(View v) {
                    includeTokens = !includeTokens;
                    updateLayout();
                }
            });
        }

        // Enforce a global maximum address length to protect algorithms from having to process large values.
        final AnythingEditText E_ADDRESS = findViewById(R.id.choose_address_dialog_editText);

        Button B_PASTE = findViewById(R.id.choose_address_dialog_pasteButton);
        B_PASTE.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                CharSequence pasteText = ClipboardUtil.paste(ChooseAddressDialog.this.activity);
                if(!"".contentEquals(pasteText)) {
                    E_ADDRESS.setText(pasteText);
                }
            }
        });

        BaseDialogFragment scanQRDialogFragment = BaseDialogFragment.newInstance(ScanQRDialog.class);
        scanQRDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this.activity) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ScanQRDialog)dialog).isComplete) {
                    E_ADDRESS.setText(((ScanQRDialog) dialog).user_ADDRESS);
                }
            }
        });
        scanQRDialogFragment.restoreListeners(this.activity, "scanqr");

        Button B_SCANQR = findViewById(R.id.choose_address_dialog_scanQRButton);
        B_SCANQR.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                if(!PermissionUtil.isGooglePlayServicesAvailable(ChooseAddressDialog.this.activity)) {
                    return;
                }

                if(!PermissionUtil.requestCameraPermission(ChooseAddressDialog.this.activity)) {
                    return;
                }

                scanQRDialogFragment.show(ChooseAddressDialog.this.activity, "scanqr");
            }
        });

        BaseDialogFragment chooseHistoryAddressDialogFragment = BaseDialogFragment.newInstance(ChooseHistoryAddressDialog.class);
        chooseHistoryAddressDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this.activity) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ChooseHistoryAddressDialog)dialog).isComplete) {
                    user_CRYPTOADDRESS = ((ChooseHistoryAddressDialog)dialog).user_CRYPTOADDRESS;

                    isComplete = true;
                    dismiss();
                }
            }
        });
        chooseHistoryAddressDialogFragment.restoreListeners(this.activity, "address_history");

        Button B_HISTORY = findViewById(R.id.choose_address_dialog_historyButton);
        B_HISTORY.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                chooseHistoryAddressDialogFragment.show(ChooseAddressDialog.this.activity, "address_history");
            }
        });

        DialogInterface.OnDismissListener chooseCryptoDialogFragmentListener = new CrashDialogInterface.CrashOnDismissListener(this.activity) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ChooseCryptoDialog)dialog).isComplete) {
                    user_CRYPTOADDRESS = ((ChooseCryptoDialog)dialog).user_CRYPTOADDRESS;

                    AddressHistory.addAddressToHistory(ChooseAddressDialog.this.activity, new AddressHistoryObj(user_CRYPTOADDRESS));

                    isComplete = true;
                    dismiss();
                }
            }
        };

        Button B_CONFIRM = findViewById(R.id.choose_address_dialog_confirmButton);
        B_CONFIRM.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                String address = E_ADDRESS.getTextString();

                // Trim leading and trailing whitespace.
                address = address.trim();

                if(address.isEmpty()) {
                    ToastUtil.showToast(activity,"empty_address");
                    return;
                }

                ArrayList<CryptoAddress> cryptoAddressArrayList = CryptoAddress.getAllValidCryptoAddress(address, includeTokens);

                if(cryptoAddressArrayList.isEmpty()) {
                    ToastUtil.showToast(activity,"unrecognized_address");
                }
                else if(cryptoAddressArrayList.size() == 1) {
                    user_CRYPTOADDRESS = cryptoAddressArrayList.get(0);
                    AddressHistory.addAddressToHistory(ChooseAddressDialog.this.activity, new AddressHistoryObj(user_CRYPTOADDRESS));

                    isComplete = true;
                    dismiss();
                }
                else {
                    BaseDialogFragment chooseCryptoDialogFragment = BaseDialogFragment.newInstance(ChooseCryptoDialog.class, cryptoAddressArrayList);
                    chooseCryptoDialogFragment.setOnDismissListener(chooseCryptoDialogFragmentListener);
                    chooseCryptoDialogFragment.show(ChooseAddressDialog.this.activity, "choose");
                }
            }
        });

        BaseDialogFragment chooseCryptoDialogFragment2 = (BaseDialogFragment) this.activity.getSupportFragmentManager().findFragmentByTag("choose");
        if (chooseCryptoDialogFragment2 != null) {
            chooseCryptoDialogFragment2.setOnDismissListener(chooseCryptoDialogFragmentListener);
        }

        updateLayout();
    }

    public void updateLayout() {
        Button B_TOGGLE = findViewById(R.id.choose_address_dialog_toggleButton);
        TextView T = findViewById(R.id.choose_address_dialog_messageTextView);

        if(Purchases.isUnlockTokensPurchased()) {
            B_TOGGLE.setVisibility(View.VISIBLE);
            T.setVisibility(View.GONE);

            if(includeTokens) {
                B_TOGGLE.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_toggle_off_24, 0, 0, 0);
                B_TOGGLE.setText("Coins + Tokens");
            }
            else {
                B_TOGGLE.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_toggle_on_24, 0, 0, 0);
                B_TOGGLE.setText("Coins");
            }
        }
        else {
            B_TOGGLE.setVisibility(View.GONE);
            T.setVisibility(View.VISIBLE);
        }

        TextView T2 = findViewById(R.id.choose_address_dialog_message2TextView);
        if("Mainnet".equals(NetworksSetting.value)) {
            T2.setVisibility(View.VISIBLE);
        }
        else {
            T2.setVisibility(View.GONE);
        }
    }

    @Override
    public Bundle onSaveInstanceStateImpl(Bundle bundle) {
        bundle.putBoolean("includeTokens", includeTokens);
        return bundle;
    }

    @Override
    public void onRestoreInstanceStateImpl(Bundle bundle) {
        if(bundle != null) {
            includeTokens = bundle.getBoolean("includeTokens");
        }
    }
}

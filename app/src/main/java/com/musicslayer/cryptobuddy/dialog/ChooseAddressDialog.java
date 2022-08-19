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
import com.musicslayer.cryptobuddy.data.persistent.user.AddressHistory;
import com.musicslayer.cryptobuddy.data.persistent.user.AddressHistoryObj;
import com.musicslayer.cryptobuddy.data.persistent.user.PersistentUserDataStore;
import com.musicslayer.cryptobuddy.data.persistent.app.Purchases;
import com.musicslayer.cryptobuddy.settings.setting.NetworksSetting;
import com.musicslayer.cryptobuddy.util.ClipboardUtil;
import com.musicslayer.cryptobuddy.util.HelpUtil;
import com.musicslayer.cryptobuddy.util.PermissionUtil;
import com.musicslayer.cryptobuddy.util.ToastUtil;
import com.musicslayer.cryptobuddy.view.ToggleButton;
import com.musicslayer.cryptobuddy.view.red.AnythingEditText;

import java.util.ArrayList;

public class ChooseAddressDialog extends BaseDialog {
    public CryptoAddress user_CRYPTOADDRESS;

    ToggleButton B_TOGGLE;

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

        B_TOGGLE = findViewById(R.id.choose_address_dialog_toggleButton);
        B_TOGGLE.setOptions("Coins", "Coins + Tokens");
        B_TOGGLE.setAdditionalOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                updateLayout();
            }
        });

        // On first creation, set the toggle state based on whether tokens are purchased.
        if(savedInstanceState == null) {
            B_TOGGLE.toggleState = Purchases.isUnlockTokensPurchased();
            B_TOGGLE.updateLayout();
        }

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

        final AnythingEditText E_ADDRESS = findViewById(R.id.choose_address_dialog_editText);

        Button B_PASTE = findViewById(R.id.choose_address_dialog_pasteButton);
        B_PASTE.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                CharSequence pasteText = ClipboardUtil.paste();
                if(pasteText != null) {
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
                if(!PermissionUtil.isGooglePlayServicesAvailable()) {
                    return;
                }

                if(!PermissionUtil.requestCameraPermission(ChooseAddressDialog.this.activity)) {
                    return;
                }

                scanQRDialogFragment.show(ChooseAddressDialog.this.activity, "scanqr");
            }
        });

        Button B_CLEAR = findViewById(R.id.choose_address_dialog_clearButton);
        B_CLEAR.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                E_ADDRESS.clearTextString();
            }
        });

        BaseDialogFragment chooseCryptoDialogFragment = BaseDialogFragment.newInstance(ChooseNetworkDialog.class, new ArrayList<CryptoAddress>());
        chooseCryptoDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this.activity) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ChooseNetworkDialog)dialog).isComplete) {
                    user_CRYPTOADDRESS = ((ChooseNetworkDialog)dialog).user_CRYPTOADDRESS;

                    PersistentUserDataStore.getInstance(AddressHistory.class).addAddressToHistory(new AddressHistoryObj(user_CRYPTOADDRESS));

                    isComplete = true;
                    dismiss();
                }
            }
        });
        chooseCryptoDialogFragment.restoreListeners(this.activity, "choose");

        Button B_CONFIRM = findViewById(R.id.choose_address_dialog_confirmButton);
        B_CONFIRM.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                String address = E_ADDRESS.getTextString();

                // Trim leading and trailing whitespace.
                address = address.trim();

                if(address.isEmpty()) {
                    ToastUtil.showToast("empty_address");
                    return;
                }

                ArrayList<CryptoAddress> cryptoAddressArrayList = CryptoAddress.getAllValidCryptoAddress(address, B_TOGGLE.toggleState);

                if(cryptoAddressArrayList.isEmpty()) {
                    ToastUtil.showToast("unrecognized_address");
                }
                else if(cryptoAddressArrayList.size() == 1) {
                    user_CRYPTOADDRESS = cryptoAddressArrayList.get(0);
                    PersistentUserDataStore.getInstance(AddressHistory.class).addAddressToHistory(new AddressHistoryObj(user_CRYPTOADDRESS));

                    isComplete = true;
                    dismiss();
                }
                else {
                    chooseCryptoDialogFragment.updateArguments(ChooseNetworkDialog.class, cryptoAddressArrayList);
                    chooseCryptoDialogFragment.show(ChooseAddressDialog.this.activity, "choose");
                }
            }
        });

        updateLayout();
    }

    public void updateLayout() {
        B_TOGGLE = findViewById(R.id.choose_address_dialog_toggleButton);
        TextView T = findViewById(R.id.choose_address_dialog_messageTextView);

        if(Purchases.isUnlockTokensPurchased()) {
            B_TOGGLE.setVisibility(View.VISIBLE);
            T.setVisibility(View.GONE);
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
}

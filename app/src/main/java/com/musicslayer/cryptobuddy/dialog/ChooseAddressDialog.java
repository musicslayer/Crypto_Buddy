package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.api.address.CryptoAddress;
import com.musicslayer.cryptobuddy.crash.CrashOnClickListener;
import com.musicslayer.cryptobuddy.crash.CrashOnDismissListener;
import com.musicslayer.cryptobuddy.persistence.AddressHistory;
import com.musicslayer.cryptobuddy.persistence.AddressHistoryObj;
import com.musicslayer.cryptobuddy.persistence.Purchases;
import com.musicslayer.cryptobuddy.util.ClipboardUtil;
import com.musicslayer.cryptobuddy.util.HelpUtil;
import com.musicslayer.cryptobuddy.util.PermissionUtil;
import com.musicslayer.cryptobuddy.util.ToastUtil;

import java.util.ArrayList;

public class ChooseAddressDialog extends BaseDialog {
    public boolean includeTokens = Purchases.isUnlockTokensPurchased;

    public CryptoAddress user_CRYPTOADDRESS;

    public ChooseAddressDialog(Activity activity) {
        super(activity);
    }

    public int getBaseViewID() {
        return R.id.choose_address_dialog;
    }

    public void createLayout() {
        setContentView(R.layout.dialog_choose_address);

        ImageButton helpButton = findViewById(R.id.choose_address_dialog_helpButton);
        helpButton.setOnClickListener(new CrashOnClickListener(this.activity) {
            @Override
            public void onClickImpl(View view) {
                HelpUtil.showHelp(ChooseAddressDialog.this.activity, R.raw.help_choose_address);
            }
        });

        if(Purchases.isUnlockTokensPurchased) {
            Button B_TOGGLE = findViewById(R.id.choose_address_dialog_toggleButton);
            B_TOGGLE.setOnClickListener(new CrashOnClickListener(this.activity) {
                public void onClickImpl(View v) {
                    includeTokens = !includeTokens;
                    updateLayout();
                }
            });
        }

        final EditText E_ADDRESS = findViewById(R.id.choose_address_dialog_editText);

        Button B_PASTE = findViewById(R.id.choose_address_dialog_pasteButton);
        B_PASTE.setOnClickListener(new CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                CharSequence pasteText = ClipboardUtil.paste(ChooseAddressDialog.this.activity);
                if(!"".contentEquals(pasteText)) {
                    E_ADDRESS.setText(pasteText);
                }
            }
        });

        BaseDialogFragment scanQRDialogFragment = BaseDialogFragment.newInstance(ScanQRDialog.class);
        scanQRDialogFragment.setOnDismissListener(new CrashOnDismissListener(this.activity) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ScanQRDialog)dialog).isComplete) {
                    E_ADDRESS.setText(((ScanQRDialog) dialog).user_ADDRESS);
                }
            }
        });
        scanQRDialogFragment.restoreListeners(this.activity, "scanqr");

        Button B_SCANQR = findViewById(R.id.choose_address_dialog_scanQRButton);
        B_SCANQR.setOnClickListener(new CrashOnClickListener(this.activity) {
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
        chooseHistoryAddressDialogFragment.setOnDismissListener(new CrashOnDismissListener(this.activity) {
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
        B_HISTORY.setOnClickListener(new CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                chooseHistoryAddressDialogFragment.show(ChooseAddressDialog.this.activity, "address_history");
            }
        });

        DialogInterface.OnDismissListener chooseCryptoDialogFragmentListener = new CrashOnDismissListener(this.activity) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ChooseCryptoDialog)dialog).isComplete) {
                    user_CRYPTOADDRESS = ((ChooseCryptoDialog)dialog).user_CRYPTOADDRESS;

                    AddressHistory.addAddress(ChooseAddressDialog.this.activity, new AddressHistoryObj(user_CRYPTOADDRESS));

                    isComplete = true;
                    dismiss();
                }
            }
        };

        Button B_CONFIRM = findViewById(R.id.choose_address_dialog_confirmButton);
        B_CONFIRM.setOnClickListener(new CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                String address = E_ADDRESS.getText().toString();
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
                    AddressHistory.addAddress(ChooseAddressDialog.this.activity, new AddressHistoryObj(user_CRYPTOADDRESS));

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

        if(Purchases.isUnlockTokensPurchased) {
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

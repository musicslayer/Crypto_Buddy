package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.api.address.CryptoAddress;
import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.crash.CrashView;

import java.util.ArrayList;

public class RemoveAddressDialog extends BaseDialog {
    ArrayList<CryptoAddress> cryptoAddressArrayList;

    CheckBox[] C;
    ArrayList<Boolean> state = new ArrayList<>();

    public ArrayList<CryptoAddress> user_cryptoAddressArrayList;

    public RemoveAddressDialog(Activity activity, ArrayList<CryptoAddress> cryptoAddressArrayList) {
        super(activity);
        this.cryptoAddressArrayList = cryptoAddressArrayList;

        for(int i = 0; i < cryptoAddressArrayList.size(); i++) {
            state.add(false);
        }
    }

    public int getBaseViewID() {
        return R.id.remove_address_dialog;
    }

    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_remove_address);

        TextView T_MESSAGE = findViewById(R.id.remove_address_dialog_messageTextView);
        if(cryptoAddressArrayList.isEmpty()) {
            T_MESSAGE.setVisibility(View.VISIBLE);
        }
        else {
            T_MESSAGE.setVisibility(View.GONE);
        }

        BaseDialogFragment confirmRemoveAddressDialogFragment = BaseDialogFragment.newInstance(ConfirmRemoveAddressDialog.class);
        confirmRemoveAddressDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(activity) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ConfirmRemoveAddressDialog)dialog).isComplete) {
                    user_cryptoAddressArrayList = new ArrayList<>();
                    for(int i = 0; i < cryptoAddressArrayList.size(); i++) {
                        if(C[i].isChecked()) {
                            user_cryptoAddressArrayList.add(cryptoAddressArrayList.get(i));
                        }
                    }

                    isComplete = true;
                    dismiss();
                }
            }
        });
        confirmRemoveAddressDialogFragment.restoreListeners(activity, "delete");

        Button B_DELETE = findViewById(R.id.remove_address_dialog_applyFilterButton);
        B_DELETE.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                confirmRemoveAddressDialogFragment.show(activity, "delete");
            }
        });

        Button B_SELECTALL = findViewById(R.id.remove_address_dialog_selectAllButton);
        B_SELECTALL.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                for(int i = 0; i < cryptoAddressArrayList.size(); i++) {
                    C[i].setChecked(true);
                }
            }
        });

        Button B_CLEARALL = findViewById(R.id.remove_address_dialog_clearAllButton);
        B_CLEARALL.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                for(int i = 0; i < cryptoAddressArrayList.size(); i++) {
                    C[i].setChecked(false);
                }
            }
        });

        updateLayout();
    }

    public void updateLayout() {
        LinearLayout L = findViewById(R.id.remove_address_dialog_checkBoxLayout);

        C = new CheckBox[cryptoAddressArrayList.size()];
        for(int i = 0; i < cryptoAddressArrayList.size(); i++) {
            C[i] = new CheckBox(this.activity);
            C[i].setChecked(state.get(i));
            C[i].setText(cryptoAddressArrayList.get(i).toString());

            LinearLayout.LayoutParams LP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            LP.setMargins(0,0,0,50);
            C[i].setLayoutParams(LP);

            L.addView(C[i]);
        }
    }

    @Override
    public Bundle onSaveInstanceStateImpl(Bundle bundle) {
        state.clear();

        for(CheckBox checkBox : C) {
            state.add(checkBox.isChecked());
        }

        bundle.putSerializable("state", state);

        return bundle;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onRestoreInstanceStateImpl(Bundle bundle) {
        if(bundle != null) {
            state = (ArrayList<Boolean>)bundle.getSerializable("state");
        }
    }
}

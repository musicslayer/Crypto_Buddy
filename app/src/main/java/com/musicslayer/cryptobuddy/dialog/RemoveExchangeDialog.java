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
import com.musicslayer.cryptobuddy.asset.exchange.Exchange;
import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.util.ToastUtil;

import java.util.ArrayList;

public class RemoveExchangeDialog extends BaseDialog {
    ArrayList<Exchange> exchangeArrayList;

    CheckBox[] C;
    ArrayList<Boolean> state = new ArrayList<>();

    public ArrayList<Exchange> user_exchangeArrayList;

    public RemoveExchangeDialog(Activity activity, ArrayList<Exchange> exchangeArrayList) {
        super(activity);
        this.exchangeArrayList = exchangeArrayList;

        for(int i = 0; i < exchangeArrayList.size(); i++) {
            state.add(false);
        }
    }

    public int getBaseViewID() {
        return R.id.remove_exchange_dialog;
    }

    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_remove_exchange);

        TextView T_MESSAGE = findViewById(R.id.remove_exchange_dialog_messageTextView);
        if(exchangeArrayList.isEmpty()) {
            T_MESSAGE.setVisibility(View.VISIBLE);
        }
        else {
            T_MESSAGE.setVisibility(View.GONE);
        }

        BaseDialogFragment confirmRemoveExchangeDialogFragment = BaseDialogFragment.newInstance(ConfirmRemoveExchangeDialog.class);
        confirmRemoveExchangeDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(activity) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ConfirmRemoveExchangeDialog)dialog).isComplete) {
                    isComplete = true;
                    dismiss();
                }
            }
        });
        confirmRemoveExchangeDialogFragment.restoreListeners(activity, "delete");

        Button B_DELETE = findViewById(R.id.remove_exchange_dialog_applyFilterButton);
        B_DELETE.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                user_exchangeArrayList = new ArrayList<>();
                for(int i = 0; i < exchangeArrayList.size(); i++) {
                    if(C[i].isChecked()) {
                        user_exchangeArrayList.add(exchangeArrayList.get(i));
                    }
                }

                if(user_exchangeArrayList.isEmpty()) {
                    ToastUtil.showToast(activity, "nothing_to_remove");
                    return;
                }

                confirmRemoveExchangeDialogFragment.show(activity, "delete");
            }
        });

        Button B_SELECTALL = findViewById(R.id.remove_exchange_dialog_selectAllButton);
        B_SELECTALL.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                for(int i = 0; i < exchangeArrayList.size(); i++) {
                    C[i].setChecked(true);
                }
            }
        });

        Button B_CLEARALL = findViewById(R.id.remove_exchange_dialog_clearAllButton);
        B_CLEARALL.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                for(int i = 0; i < exchangeArrayList.size(); i++) {
                    C[i].setChecked(false);
                }
            }
        });

        updateLayout();
    }

    public void updateLayout() {
        LinearLayout L = findViewById(R.id.remove_exchange_dialog_checkBoxLayout);

        C = new CheckBox[exchangeArrayList.size()];
        for(int i = 0; i < exchangeArrayList.size(); i++) {
            C[i] = new CheckBox(this.activity);
            C[i].setChecked(state.get(i));
            C[i].setText(exchangeArrayList.get(i).toString());

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
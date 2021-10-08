package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;

import androidx.appcompat.widget.AppCompatButton;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.api.address.CryptoAddress;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.persistence.AddressHistory;
import com.musicslayer.cryptobuddy.persistence.AddressHistoryObj;

// TODO Maybe clicking a choice here shouldn't proceed. Your choice here could merely be filled in, so that the user can edit it if they wish ??

public class ChooseHistoryAddressDialog extends BaseDialog {
    public CryptoAddress user_CRYPTOADDRESS;

    public ChooseHistoryAddressDialog(Activity activity) {
        super(activity);
    }

    public int getBaseViewID() {
        return R.id.choose_history_address_dialog;
    }

    public void createLayout() {
        setContentView(R.layout.dialog_choose_history_address);

        TableLayout T = findViewById(R.id.choose_history_dialog_tableLayout);

        for(AddressHistoryObj a : AddressHistory.settings_address_history) {
            AppCompatButton B = new AppCompatButton(this.activity);
            B.setText(a.toString());
            B.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
                public void onClickImpl(View v) {
                    AddressHistory.addAddressToHistory(ChooseHistoryAddressDialog.this.activity, a);
                    user_CRYPTOADDRESS = a.cryptoAddress;

                    isComplete = true;
                    dismiss();
                }
            });

            TableRow TR = new TableRow(ChooseHistoryAddressDialog.this.activity);
            TableRow.LayoutParams TRP = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);

            TR.addView(B, TRP);
            T.addView(TR);
        }
    }
}

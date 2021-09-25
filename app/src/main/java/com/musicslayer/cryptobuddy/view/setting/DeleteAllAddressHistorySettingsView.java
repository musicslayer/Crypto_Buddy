package com.musicslayer.cryptobuddy.view.setting;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.dialog.ConfirmDeleteAllAddressHistoryDialog;
import com.musicslayer.cryptobuddy.dialog.BaseDialogFragment;
import com.musicslayer.cryptobuddy.persistence.AddressHistory;
import com.musicslayer.cryptobuddy.util.Toast;

public class DeleteAllAddressHistorySettingsView extends LinearLayout {
    public DeleteAllAddressHistorySettingsView(Context context) {
        super(context);

        this.setOrientation(VERTICAL);

        LayoutParams LP = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        LP.setMargins(0,0,0,50);
        this.setLayoutParams(LP);

        final TextView T_Reset=new TextView(context);
        T_Reset.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        T_Reset.setText("Delete all stored address history.");

        BaseDialogFragment confirmDeleteAllAddressHistoryDialogFragment = BaseDialogFragment.newInstance(ConfirmDeleteAllAddressHistoryDialog.class);
        confirmDeleteAllAddressHistoryDialogFragment.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if(((ConfirmDeleteAllAddressHistoryDialog)dialog).isComplete) {
                    AddressHistory.resetAllData(context);
                    Toast.showToast("reset_stored_addresses");
                }
            }
        });
        confirmDeleteAllAddressHistoryDialogFragment.restoreListeners(context, "delete_all");

        final AppCompatButton B_DELETEALL = new AppCompatButton(context);
        B_DELETEALL.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        B_DELETEALL.setText("Delete All Address History");
        B_DELETEALL.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_warning_24, 0, 0, 0);
        B_DELETEALL.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                confirmDeleteAllAddressHistoryDialogFragment.show(context, "delete_all");
            }
        });

        this.addView(T_Reset);
        this.addView(B_DELETEALL);
    }
}

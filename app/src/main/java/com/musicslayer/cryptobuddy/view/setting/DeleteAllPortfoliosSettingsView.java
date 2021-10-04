package com.musicslayer.cryptobuddy.view.setting;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;

import com.musicslayer.cryptobuddy.crash.CrashOnClickListener;
import com.musicslayer.cryptobuddy.crash.CrashOnDismissListener;
import com.musicslayer.cryptobuddy.persistence.AddressPortfolio;
import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.persistence.TransactionPortfolio;
import com.musicslayer.cryptobuddy.dialog.ConfirmDeleteAllPortfoliosDialog;
import com.musicslayer.cryptobuddy.dialog.BaseDialogFragment;
import com.musicslayer.cryptobuddy.util.Toast;

public class DeleteAllPortfoliosSettingsView extends LinearLayout {
    public DeleteAllPortfoliosSettingsView(Context context) {
        super(context);

        this.setOrientation(VERTICAL);

        LinearLayout.LayoutParams LP = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        LP.setMargins(0,0,0,50);
        this.setLayoutParams(LP);

        final TextView T_Reset=new TextView(context);
        T_Reset.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        T_Reset.setText("Delete all created portfolios.");

        BaseDialogFragment confirmDeleteAllPortfoliosDialogFragment = BaseDialogFragment.newInstance(ConfirmDeleteAllPortfoliosDialog.class);
        confirmDeleteAllPortfoliosDialogFragment.setOnDismissListener(new CrashOnDismissListener(context) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ConfirmDeleteAllPortfoliosDialog)dialog).isComplete) {
                    AddressPortfolio.resetAllData(context);
                    TransactionPortfolio.resetAllData(context);
                    Toast.showToast(context,"reset_portfolios");
                }
            }
        });
        confirmDeleteAllPortfoliosDialogFragment.restoreListeners(context, "delete_all");

        final AppCompatButton B_DELETEALL = new AppCompatButton(context);
        B_DELETEALL.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        B_DELETEALL.setText("Delete All Portfolios");
        B_DELETEALL.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_warning_24, 0, 0, 0);
        B_DELETEALL.setOnClickListener(new CrashOnClickListener(context) {
            public void onClickImpl(View v) {
                confirmDeleteAllPortfoliosDialogFragment.show(context, "delete_all");
            }
        });

        this.addView(T_Reset);
        this.addView(B_DELETEALL);
    }
}

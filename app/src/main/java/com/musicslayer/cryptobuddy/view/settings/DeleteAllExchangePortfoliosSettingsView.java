package com.musicslayer.cryptobuddy.view.settings;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.dialog.BaseDialogFragment;
import com.musicslayer.cryptobuddy.dialog.ConfirmDeleteAllExchangePortfoliosDialog;
import com.musicslayer.cryptobuddy.persistence.ExchangePortfolio;
import com.musicslayer.cryptobuddy.persistence.PersistentDataStore;
import com.musicslayer.cryptobuddy.settings.setting.Setting;
import com.musicslayer.cryptobuddy.util.ToastUtil;

public class DeleteAllExchangePortfoliosSettingsView extends SettingsView {
    public DeleteAllExchangePortfoliosSettingsView(Context context) {
        super(context);
    }

    public DeleteAllExchangePortfoliosSettingsView(Context context, Setting setting) {
        super(context);

        this.setOrientation(VERTICAL);

        LayoutParams LP = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        LP.setMargins(0,0,0,50);
        this.setLayoutParams(LP);

        final TextView T_Reset=new TextView(context);
        T_Reset.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        T_Reset.setText("Delete all exchange portfolios.");

        BaseDialogFragment confirmDeleteAllExchangePortfoliosDialogFragment = BaseDialogFragment.newInstance(ConfirmDeleteAllExchangePortfoliosDialog.class);
        confirmDeleteAllExchangePortfoliosDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(context) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ConfirmDeleteAllExchangePortfoliosDialog)dialog).isComplete) {
                    PersistentDataStore.getInstance(ExchangePortfolio.class).resetAllData();
                    ToastUtil.showToast("reset_exchange_portfolios");
                }
            }
        });
        confirmDeleteAllExchangePortfoliosDialogFragment.restoreListeners(context, "delete_all_exchange_portfolios_settings_view");

        final AppCompatButton B_DELETEALL = new AppCompatButton(context);
        B_DELETEALL.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        B_DELETEALL.setText("Delete All Exchange Portfolios");
        B_DELETEALL.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_warning_24, 0, 0, 0);
        B_DELETEALL.setOnClickListener(new CrashView.CrashOnClickListener(context) {
            public void onClickImpl(View v) {
                confirmDeleteAllExchangePortfoliosDialogFragment.show(context, "delete_all_exchange_portfolios_settings_view");
            }
        });

        this.addView(T_Reset);
        this.addView(B_DELETEALL);
    }
}

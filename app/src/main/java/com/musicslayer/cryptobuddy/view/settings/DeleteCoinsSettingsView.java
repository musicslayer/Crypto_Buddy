package com.musicslayer.cryptobuddy.view.settings;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.asset.coinmanager.CoinManager;
import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.dialog.BaseDialogFragment;
import com.musicslayer.cryptobuddy.dialog.ConfirmDeleteCoinsDialog;
import com.musicslayer.cryptobuddy.dialog.DeleteCoinsDialog;
import com.musicslayer.cryptobuddy.persistence.CoinManagerList;
import com.musicslayer.cryptobuddy.persistence.PersistentDataStore;
import com.musicslayer.cryptobuddy.persistence.SettingList;
import com.musicslayer.cryptobuddy.settings.setting.Setting;
import com.musicslayer.cryptobuddy.util.ToastUtil;

import java.util.ArrayList;

public class DeleteCoinsSettingsView extends SettingsView {
    public ArrayList<String> choices;

    public DeleteCoinsSettingsView(Context context) {
        super(context);
    }

    public DeleteCoinsSettingsView(Context context, Setting setting) {
        super(context);

        this.setOrientation(VERTICAL);

        LayoutParams LP = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        LP.setMargins(0,0,0,50);
        this.setLayoutParams(LP);

        final TextView T_Reset=new TextView(context);
        T_Reset.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        T_Reset.setText("Delete all coins from the app's database.");

        BaseDialogFragment confirmDeleteCoinsDialogFragment = BaseDialogFragment.newInstance(ConfirmDeleteCoinsDialog.class, "All");
        confirmDeleteCoinsDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(context) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ConfirmDeleteCoinsDialog)dialog).isComplete) {
                    if(choices.contains("found")) {
                        CoinManager.resetAllFoundCoins();
                    }
                    if(choices.contains("custom")) {
                        CoinManager.resetAllCustomCoins();
                    }

                    PersistentDataStore.getInstance(CoinManagerList.class).saveAllData();

                    Setting setting = Setting.getSettingFromKey("DefaultCoinSetting");
                    setting.refreshSetting();
                    PersistentDataStore.getInstance(SettingList.class).saveSetting(setting);

                    ToastUtil.showToast("reset_coins");
                }
            }
        });
        confirmDeleteCoinsDialogFragment.restoreListeners(context, "reset_confirm_delete_coins_settings_view");

        BaseDialogFragment deleteCoinsDialogFragment = BaseDialogFragment.newInstance(DeleteCoinsDialog.class, "All");
        deleteCoinsDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(context) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((DeleteCoinsDialog)dialog).isComplete) {
                    choices = ((DeleteCoinsDialog)dialog).user_CHOICES;
                    confirmDeleteCoinsDialogFragment.show(context, "reset_confirm_delete_coins_settings_view");
                }
            }
        });
        deleteCoinsDialogFragment.restoreListeners(context, "reset_delete_coins_settings_view");

        final AppCompatButton B_DELETE = new AppCompatButton(context);
        B_DELETE.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        B_DELETE.setText("Delete All Coins");
        B_DELETE.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_warning_24, 0, 0, 0);
        B_DELETE.setOnClickListener(new CrashView.CrashOnClickListener(context) {
            public void onClickImpl(View v) {
                deleteCoinsDialogFragment.show(context, "reset_delete_coins_settings_view");
            }
        });

        this.addView(T_Reset);
        this.addView(B_DELETE);
    }

    @Override
    public Parcelable onSaveInstanceStateImpl(Parcelable state)
    {
        Bundle bundle = new Bundle();
        bundle.putParcelable("superState", state);
        bundle.putStringArrayList("choices", choices);

        return bundle;
    }

    @Override
    public Parcelable onRestoreInstanceStateImpl(Parcelable state)
    {
        if (state instanceof Bundle) // implicit null check
        {
            Bundle bundle = (Bundle) state;
            state = bundle.getParcelable("superState");
            choices = bundle.getStringArrayList("choices");
        }
        return state;
    }
}

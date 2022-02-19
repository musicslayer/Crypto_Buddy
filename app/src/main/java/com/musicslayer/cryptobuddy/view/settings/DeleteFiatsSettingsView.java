package com.musicslayer.cryptobuddy.view.settings;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.asset.fiatmanager.FiatManager;
import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.dialog.BaseDialogFragment;
import com.musicslayer.cryptobuddy.dialog.ConfirmDeleteFiatsDialog;
import com.musicslayer.cryptobuddy.dialog.DeleteFiatsDialog;
import com.musicslayer.cryptobuddy.persistence.FiatManagerList;
import com.musicslayer.cryptobuddy.persistence.PersistentDataStore;
import com.musicslayer.cryptobuddy.persistence.SettingList;
import com.musicslayer.cryptobuddy.settings.setting.Setting;
import com.musicslayer.cryptobuddy.util.ToastUtil;

import java.util.ArrayList;

public class DeleteFiatsSettingsView extends SettingsView {
    public ArrayList<String> choices;

    public DeleteFiatsSettingsView(Context context) {
        super(context);
    }

    public DeleteFiatsSettingsView(Context context, Setting setting) {
        super(context);

        this.setOrientation(VERTICAL);

        LayoutParams LP = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        LP.setMargins(0,0,0,50);
        this.setLayoutParams(LP);

        final TextView T_Reset=new TextView(context);
        T_Reset.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        T_Reset.setText("Delete all fiats from the app's database.");

        BaseDialogFragment confirmDeleteFiatsDialogFragment = BaseDialogFragment.newInstance(ConfirmDeleteFiatsDialog.class, "All");
        confirmDeleteFiatsDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(context) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ConfirmDeleteFiatsDialog)dialog).isComplete) {
                    if(choices.contains("found")) {
                        FiatManager.resetAllFoundFiats();
                    }
                    if(choices.contains("custom")) {
                        FiatManager.resetAllCustomFiats();
                    }

                    PersistentDataStore.getInstance(FiatManagerList.class).saveAllData();

                    Setting setting = Setting.getSettingFromKey("DefaultFiatSetting");
                    setting.refreshSetting();
                    PersistentDataStore.getInstance(SettingList.class).saveSetting(setting);

                    ToastUtil.showToast("reset_fiats");
                }
            }
        });
        confirmDeleteFiatsDialogFragment.restoreListeners(context, "reset_confirm_delete_fiats_settings_view");

        BaseDialogFragment deleteFiatsDialogFragment = BaseDialogFragment.newInstance(DeleteFiatsDialog.class, "All");
        deleteFiatsDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(context) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((DeleteFiatsDialog)dialog).isComplete) {
                    choices = ((DeleteFiatsDialog)dialog).user_CHOICES;
                    confirmDeleteFiatsDialogFragment.show(context, "reset_confirm_delete_fiats_settings_view");
                }
            }
        });
        deleteFiatsDialogFragment.restoreListeners(context, "reset_delete_fiats_settings_view");

        final AppCompatButton B_DELETE = new AppCompatButton(context);
        B_DELETE.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        B_DELETE.setText("Delete All Fiats");
        B_DELETE.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_warning_24, 0, 0, 0);
        B_DELETE.setOnClickListener(new CrashView.CrashOnClickListener(context) {
            public void onClickImpl(View v) {
                deleteFiatsDialogFragment.show(context, "reset_delete_fiats_settings_view");
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

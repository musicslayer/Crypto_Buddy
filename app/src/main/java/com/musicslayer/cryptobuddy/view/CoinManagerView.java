package com.musicslayer.cryptobuddy.view;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageButton;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.asset.coinmanager.CoinManager;
import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.crash.CrashTableRow;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.dialog.BaseDialogFragment;
import com.musicslayer.cryptobuddy.dialog.ConfirmDeleteCoinsDialog;
import com.musicslayer.cryptobuddy.dialog.DeleteCoinsDialog;
import com.musicslayer.cryptobuddy.dialog.ViewCoinsDialog;
import com.musicslayer.cryptobuddy.persistence.CoinManagerList;
import com.musicslayer.cryptobuddy.persistence.SettingList;
import com.musicslayer.cryptobuddy.settings.setting.Setting;

import java.util.ArrayList;

public class CoinManagerView extends CrashTableRow {
    public TextView T;
    public AppCompatImageButton B_DELETE;
    public AppCompatImageButton B_VIEW;
    public CoinManager coinManager;
    public ArrayList<String> choices;

    public CoinManagerView(Context context) {
        super(context);
    }

    public CoinManagerView(Context context, CoinManager coinManager) {
        super(context);
        this.coinManager = coinManager;
        this.makeLayout();
    }

    public void makeLayout() {
        Context context = getContext();

        LinearLayout L = new LinearLayout(context);
        L.setGravity(Gravity.CENTER_VERTICAL);

        T = new TextView(context);

        // The user cannot delete hardcoded coins.
        BaseDialogFragment confirmDeleteCoinsDialogFragment = BaseDialogFragment.newInstance(ConfirmDeleteCoinsDialog.class);
        confirmDeleteCoinsDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(context) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ConfirmDeleteCoinsDialog)dialog).isComplete) {
                    if(choices.contains("found")) {
                        coinManager.resetFoundCoins();
                    }
                    if(choices.contains("custom")) {
                        coinManager.resetCustomCoins();
                    }

                    CoinManagerList.updateCoinManager(getContext(), coinManager);

                    Setting setting = Setting.getSettingFromKey("DefaultCoinSetting");
                    setting.refreshSetting();
                    SettingList.saveSetting(context, setting);

                    updateLayout();
                }
            }
        });
        confirmDeleteCoinsDialogFragment.restoreListeners(context, "confirm_delete_coins_" + coinManager.getSettingsKey());

        BaseDialogFragment deleteCoinsDialogFragment = BaseDialogFragment.newInstance(DeleteCoinsDialog.class);
        deleteCoinsDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(context) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((DeleteCoinsDialog)dialog).isComplete) {
                    choices = ((DeleteCoinsDialog)dialog).user_CHOICES;
                    confirmDeleteCoinsDialogFragment.show(context, "confirm_delete_coins_" + coinManager.getSettingsKey());
                }
            }
        });
        deleteCoinsDialogFragment.restoreListeners(context, "delete_coins_" + coinManager.getSettingsKey());

        B_DELETE = new AppCompatImageButton(context);
        B_DELETE.setImageResource(R.drawable.ic_baseline_delete_24);
        B_DELETE.setOnClickListener(new CrashView.CrashOnClickListener(context) {
            public void onClickImpl(View v) {
                deleteCoinsDialogFragment.show(context, "delete_coins_" + coinManager.getSettingsKey());
            }
        });

        B_VIEW = new AppCompatImageButton(context);
        B_VIEW.setImageResource(R.drawable.ic_baseline_pageview_24);
        B_VIEW.setOnClickListener(new CrashView.CrashOnClickListener(context) {
            public void onClickImpl(View v) {
                BaseDialogFragment.newInstance(ViewCoinsDialog.class).show(context, "view_coins_" + coinManager.getSettingsKey());
            }
        });

        L.addView(B_DELETE);
        L.addView(B_VIEW);

        this.addView(L);
        this.addView(T);
    }

    public void updateLayout() {
        T.setText("Coins:\n(" + coinManager.hardcoded_coins.size() + ", " + coinManager.found_coins.size() + ", " + coinManager.custom_coins.size() + ")");
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

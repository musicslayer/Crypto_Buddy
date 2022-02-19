package com.musicslayer.cryptobuddy.view.asset;

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
import com.musicslayer.cryptobuddy.asset.fiatmanager.FiatManager;
import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.crash.CrashTableRow;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.dialog.BaseDialogFragment;
import com.musicslayer.cryptobuddy.dialog.ConfirmDeleteFiatsDialog;
import com.musicslayer.cryptobuddy.dialog.DeleteFiatsDialog;
import com.musicslayer.cryptobuddy.dialog.ViewFiatsDialog;
import com.musicslayer.cryptobuddy.persistence.FiatManagerList;
import com.musicslayer.cryptobuddy.persistence.PersistentDataStore;
import com.musicslayer.cryptobuddy.persistence.SettingList;
import com.musicslayer.cryptobuddy.settings.setting.Setting;

import java.util.ArrayList;

public class FiatManagerView extends CrashTableRow {
    public TextView T;
    public AppCompatImageButton B_DELETE;
    public AppCompatImageButton B_VIEW;
    public FiatManager fiatManager;
    public ArrayList<String> choices;

    public FiatManagerView(Context context) {
        super(context);
    }

    public FiatManagerView(Context context, FiatManager fiatManager) {
        super(context);
        this.fiatManager = fiatManager;
        this.makeLayout();
    }

    public void makeLayout() {
        Context context = getContext();

        LinearLayout L = new LinearLayout(context);
        L.setGravity(Gravity.CENTER_VERTICAL);

        T = new TextView(context);

        BaseDialogFragment confirmDeleteFiatsDialogFragment = BaseDialogFragment.newInstance(ConfirmDeleteFiatsDialog.class, fiatManager.getFiatType());
        confirmDeleteFiatsDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(context) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ConfirmDeleteFiatsDialog)dialog).isComplete) {
                    // The user cannot delete hardcoded fiats.
                    if(choices.contains("found")) {
                        fiatManager.resetFoundFiats();
                    }
                    if(choices.contains("custom")) {
                        fiatManager.resetCustomFiats();
                    }

                    PersistentDataStore.getInstance(FiatManagerList.class).updateFiatManager(fiatManager);

                    Setting setting = Setting.getSettingFromKey("DefaultFiatSetting");
                    setting.refreshSetting();
                    PersistentDataStore.getInstance(SettingList.class).saveSetting(setting);

                    updateLayout();
                }
            }
        });
        confirmDeleteFiatsDialogFragment.restoreListeners(context, "confirm_delete_fiats_" + fiatManager.getSettingsKey());

        BaseDialogFragment deleteFiatsDialogFragment = BaseDialogFragment.newInstance(DeleteFiatsDialog.class, fiatManager.getFiatType());
        deleteFiatsDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(context) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((DeleteFiatsDialog)dialog).isComplete) {
                    choices = ((DeleteFiatsDialog)dialog).user_CHOICES;
                    confirmDeleteFiatsDialogFragment.show(context, "confirm_delete_fiats_" + fiatManager.getSettingsKey());
                }
            }
        });
        deleteFiatsDialogFragment.restoreListeners(context, "delete_fiats_" + fiatManager.getSettingsKey());

        B_DELETE = new AppCompatImageButton(context);
        B_DELETE.setImageResource(R.drawable.ic_baseline_delete_24);
        B_DELETE.setOnClickListener(new CrashView.CrashOnClickListener(context) {
            public void onClickImpl(View v) {
                deleteFiatsDialogFragment.show(context, "delete_fiats_" + fiatManager.getSettingsKey());
            }
        });

        B_VIEW = new AppCompatImageButton(context);
        B_VIEW.setImageResource(R.drawable.ic_baseline_pageview_24);
        B_VIEW.setOnClickListener(new CrashView.CrashOnClickListener(context) {
            public void onClickImpl(View v) {
                BaseDialogFragment.newInstance(ViewFiatsDialog.class, fiatManager.getFiatType()).show(context, "view_fiats_" + fiatManager.getSettingsKey());
            }
        });

        L.addView(B_DELETE);
        L.addView(B_VIEW);

        this.addView(L);
        this.addView(T);
    }

    public void updateLayout() {
        T.setText(fiatManager.getFiatType() + ":\n(" + fiatManager.hardcoded_fiats.size() + ", " + fiatManager.found_fiats.size() + ", " + fiatManager.custom_fiats.size() + ")");
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

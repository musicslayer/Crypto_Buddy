package com.musicslayer.cryptobuddy.view;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.asset.fiatmanager.FiatManager;
import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.crash.CrashTableRow;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.dialog.BaseDialogFragment;
import com.musicslayer.cryptobuddy.dialog.ConfirmDeleteFiatsDialog;
import com.musicslayer.cryptobuddy.dialog.DeleteFiatsDialog;
import com.musicslayer.cryptobuddy.persistence.FiatManagerList;
import com.musicslayer.cryptobuddy.persistence.SettingList;
import com.musicslayer.cryptobuddy.settings.setting.Setting;

import java.util.ArrayList;

public class FiatManagerView extends CrashTableRow {
    public TextView T;
    public AppCompatButton B_DELETE;
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

        // The user cannot delete hardcoded fiats.
        BaseDialogFragment confirmDeleteFiatsDialogFragment = BaseDialogFragment.newInstance(ConfirmDeleteFiatsDialog.class);
        confirmDeleteFiatsDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(context) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ConfirmDeleteFiatsDialog)dialog).isComplete) {
                    if(choices.contains("found")) {
                        fiatManager.resetFoundFiats();
                    }
                    if(choices.contains("custom")) {
                        fiatManager.resetCustomFiats();
                    }

                    FiatManagerList.updateFiatManager(getContext(), fiatManager);

                    Setting setting = Setting.getSettingFromKey("DefaultFiatSetting");
                    setting.refreshSetting();
                    SettingList.saveSetting(context, setting);

                    updateLayout();
                }
            }
        });
        confirmDeleteFiatsDialogFragment.restoreListeners(context, "confirm_delete_fiats");

        BaseDialogFragment deleteFiatsDialogFragment = BaseDialogFragment.newInstance(DeleteFiatsDialog.class);
        deleteFiatsDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(context) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((DeleteFiatsDialog)dialog).isComplete) {
                    choices = ((DeleteFiatsDialog)dialog).user_CHOICES;
                    confirmDeleteFiatsDialogFragment.show(context, "confirm_delete_fiats");
                }
            }
        });
        deleteFiatsDialogFragment.restoreListeners(context, "delete_fiats");

        B_DELETE = new AppCompatButton(context);
        B_DELETE.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_delete_24, 0, 0, 0);
        B_DELETE.setText("Delete");
        B_DELETE.setOnClickListener(new CrashView.CrashOnClickListener(context) {
            public void onClickImpl(View v) {
                deleteFiatsDialogFragment.show(context, "delete_fiats");
            }
        });

        L.addView(B_DELETE);
        L.addView(T);

        this.addView(L);
    }

    public void updateLayout() {
        T.setText("Fiats:\n(" + fiatManager.hardcoded_fiats.size() + ", " + fiatManager.found_fiats.size() + ", " + fiatManager.custom_fiats.size() + ")");
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

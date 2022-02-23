package com.musicslayer.cryptobuddy.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.asset.fiatmanager.FiatManager;
import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.data.persistent.app.FiatManagerList;
import com.musicslayer.cryptobuddy.data.persistent.app.PersistentAppDataStore;
import com.musicslayer.cryptobuddy.data.persistent.user.PersistentUserDataStore;
import com.musicslayer.cryptobuddy.data.persistent.user.SettingList;
import com.musicslayer.cryptobuddy.dialog.BaseDialogFragment;
import com.musicslayer.cryptobuddy.dialog.ConfirmDeleteFiatsDialog;
import com.musicslayer.cryptobuddy.dialog.DeleteFiatsDialog;
import com.musicslayer.cryptobuddy.settings.setting.Setting;
import com.musicslayer.cryptobuddy.util.HelpUtil;
import com.musicslayer.cryptobuddy.util.ToastUtil;
import com.musicslayer.cryptobuddy.view.asset.FiatManagerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class FiatManagerActivity extends BaseActivity {
    ArrayList<FiatManagerView> fiatManagerViewArrayList;
    public ArrayList<String> choices;

    @Override
    public int getAdLayoutViewID() {
        return R.id.fiat_manager_adLayout;
    }

    @Override
    public void onBackPressedImpl() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.activity_fiat_manager);

        ImageButton helpButton = findViewById(R.id.fiat_manager_helpButton);
        helpButton.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                HelpUtil.showHelp(FiatManagerActivity.this, R.raw.help_fiat_manager);
            }
        });

        BaseDialogFragment confirmDeleteFiatsDialogFragment = BaseDialogFragment.newInstance(ConfirmDeleteFiatsDialog.class, "All");
        confirmDeleteFiatsDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(FiatManagerActivity.this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ConfirmDeleteFiatsDialog)dialog).isComplete) {
                    if(choices.contains("found")) {
                        FiatManager.resetAllFoundFiats();
                    }
                    if(choices.contains("custom")) {
                        FiatManager.resetAllCustomFiats();
                    }

                    PersistentAppDataStore.getInstance(FiatManagerList.class).saveAllData();
                    ToastUtil.showToast("reset_fiats");

                    updateLayout();
                }
            }
        });
        confirmDeleteFiatsDialogFragment.restoreListeners(this, "confirm_delete_all_fiats");

        BaseDialogFragment deleteFiatsDialogFragment = BaseDialogFragment.newInstance(DeleteFiatsDialog.class, "All");
        deleteFiatsDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(FiatManagerActivity.this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((DeleteFiatsDialog)dialog).isComplete) {
                    choices = ((DeleteFiatsDialog)dialog).user_CHOICES;
                    confirmDeleteFiatsDialogFragment.show(FiatManagerActivity.this, "confirm_delete_all_fiats");
                }
            }
        });
        deleteFiatsDialogFragment.restoreListeners(FiatManagerActivity.this, "delete_all_fiats");

        AppCompatButton B_DELETE = findViewById(R.id.fiat_manager_deleteAllFiatsButton);
        B_DELETE.setOnClickListener(new CrashView.CrashOnClickListener(FiatManagerActivity.this) {
            public void onClickImpl(View v) {
                deleteFiatsDialogFragment.show(FiatManagerActivity.this, "delete_all_fiats");
            }
        });

        updateLayout();
    }

    public void updateLayout() {
        TableLayout tableLayout = findViewById(R.id.fiat_manager_tableLayout);
        TableRow firstRow = findViewById(R.id.fiat_manager_tableRow1);

        tableLayout.removeAllViews();
        tableLayout.addView(firstRow);

        ArrayList<String> fiatTypes = FiatManager.fiatManagers_fiat_types;
        Collections.sort(fiatTypes, Comparator.comparing(String::toLowerCase));

        fiatManagerViewArrayList = new ArrayList<>();
        for(String fiatType : fiatTypes) {
            FiatManager fiatManager = FiatManager.getFiatManagerFromFiatType(fiatType);
            FiatManagerView fiatManagerView = new FiatManagerView(FiatManagerActivity.this, fiatManager);
            fiatManagerView.updateLayout();

            fiatManagerViewArrayList.add(fiatManagerView);
            tableLayout.addView(fiatManagerView);
        }
    }

    @Override
    public void onSaveInstanceStateImpl(@NonNull Bundle bundle) {
        for(FiatManagerView fiatManagerView : fiatManagerViewArrayList) {
            bundle.putParcelable("fiatManagerView_" + fiatManagerView.fiatManager.getFiatType(), fiatManagerView.onSaveInstanceState());
        }
        bundle.putStringArrayList("choices", choices);
    }

    @Override
    public void onRestoreInstanceStateImpl(Bundle bundle) {
        if(bundle != null) {
            for(FiatManagerView fiatManagerView : fiatManagerViewArrayList) {
                fiatManagerView.onRestoreInstanceState(bundle.getParcelable("fiatManagerView_" + fiatManagerView.fiatManager.getFiatType()));
            }
            choices = bundle.getStringArrayList("choices");
        }
    }
}
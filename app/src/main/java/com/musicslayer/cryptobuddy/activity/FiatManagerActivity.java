package com.musicslayer.cryptobuddy.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;

import androidx.annotation.NonNull;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.asset.fiatmanager.FiatManager;
import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.dialog.AddCustomFiatDialog;
import com.musicslayer.cryptobuddy.dialog.BaseDialogFragment;
import com.musicslayer.cryptobuddy.util.HelpUtil;
import com.musicslayer.cryptobuddy.view.asset.FiatManagerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class FiatManagerActivity extends BaseActivity {
    ArrayList<FiatManagerView> fiatManagerViewArrayList;

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

        BaseDialogFragment addCustomFiatDialogFragment = BaseDialogFragment.newInstance(AddCustomFiatDialog.class);
        addCustomFiatDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((AddCustomFiatDialog)dialog).isComplete) {
                    // It's easier to just update all of them.
                    for(FiatManagerView fiatManagerView : fiatManagerViewArrayList) {
                        fiatManagerView.updateLayout();
                    }
                }
            }
        });
        addCustomFiatDialogFragment.restoreListeners(this, "add_custom_fiat");

        Button B_CustomFiat = findViewById(R.id.fiat_manager_customFiatButton);
        B_CustomFiat.setOnClickListener(new CrashView.CrashOnClickListener(this) {
            @Override
            public void onClickImpl(View view) {
                addCustomFiatDialogFragment.show(FiatManagerActivity.this, "add_custom_fiat");
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
    }

    @Override
    public void onRestoreInstanceStateImpl(Bundle bundle) {
        if(bundle != null) {
            for(FiatManagerView fiatManagerView : fiatManagerViewArrayList) {
                fiatManagerView.onRestoreInstanceState(bundle.getParcelable("fiatManagerView_" + fiatManagerView.fiatManager.getFiatType()));
            }
        }
    }
}
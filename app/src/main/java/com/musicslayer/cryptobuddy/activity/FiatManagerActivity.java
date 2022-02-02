package com.musicslayer.cryptobuddy.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TableLayout;

import androidx.annotation.NonNull;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.asset.fiatmanager.FiatManager;
import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.dialog.AddCustomFiatDialog;
import com.musicslayer.cryptobuddy.dialog.BaseDialogFragment;
import com.musicslayer.cryptobuddy.util.HelpUtil;
import com.musicslayer.cryptobuddy.view.FiatManagerView;

import java.util.ArrayList;

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

        TableLayout tableLayout = findViewById(R.id.fiat_manager_tableLayout);

        // For now, there is only one type of Fiat.
        fiatManagerViewArrayList = new ArrayList<>();
        FiatManager fiatManager = FiatManager.getFiatManagerFromKey("BaseFiatManager");
        FiatManagerView fiatManagerView = new FiatManagerView(FiatManagerActivity.this, fiatManager);
        fiatManagerView.updateLayout();

        fiatManagerViewArrayList.add(fiatManagerView);
        tableLayout.addView(fiatManagerView);

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
    }

    @Override
    public void onSaveInstanceStateImpl(@NonNull Bundle bundle) {
        bundle.putParcelable("fiatManagerView", fiatManagerViewArrayList.get(0).onSaveInstanceState());
    }

    @Override
    public void onRestoreInstanceStateImpl(Bundle bundle) {
        if(bundle != null) {
            fiatManagerViewArrayList.get(0).onRestoreInstanceState(bundle.getParcelable("fiatManagerView"));
        }
    }
}
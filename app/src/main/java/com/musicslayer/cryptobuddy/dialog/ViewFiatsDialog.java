package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.widget.Toolbar;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.activity.FiatManagerActivity;
import com.musicslayer.cryptobuddy.asset.Asset;
import com.musicslayer.cryptobuddy.asset.fiat.Fiat;
import com.musicslayer.cryptobuddy.asset.fiatmanager.FiatManager;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.persistence.FiatManagerList;
import com.musicslayer.cryptobuddy.view.asset.FiatView;
import com.musicslayer.cryptobuddy.view.asset.SelectAndSearchView;

import java.util.ArrayList;

public class ViewFiatsDialog extends BaseDialog {
    public String fiatType;

    int LAST_CHECK = 0;

    public ViewFiatsDialog(Activity activity, String fiatType) {
        super(activity);
        this.fiatType = fiatType;
    }

    public int getBaseViewID() {
        return R.id.view_fiats_dialog;
    }

    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_view_fiats);

        Toolbar toolbar = findViewById(R.id.view_fiats_dialog_toolbar);
        toolbar.setTitle("View " + fiatType + " Fiats");

        RadioGroup radioGroup = findViewById(R.id.view_fiats_dialog_radioGroup);
        RadioButton[] rb = new RadioButton[3];

        rb[0] = findViewById(R.id.view_fiats_dialog_hardcodedRadioButton);
        rb[0].setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                LAST_CHECK = 0;
                updateLayout();
            }
        });

        rb[1] = findViewById(R.id.view_fiats_dialog_foundRadioButton);
        rb[1].setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                LAST_CHECK = 1;
                updateLayout();
            }
        });

        rb[2] = findViewById(R.id.view_fiats_dialog_customRadioButton);
        rb[2].setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                LAST_CHECK = 2;
                updateLayout();
            }
        });

        radioGroup.check(rb[LAST_CHECK].getId());
        rb[LAST_CHECK].callOnClick();

        updateLayout();
    }

    public void updateLayout() {
        FiatView fiatView = findViewById(R.id.view_fiats_dialog_fiatView);

        SelectAndSearchView ssv = findViewById(R.id.view_fiats_dialog_selectAndSearchView);
        ssv.setChooseAssetListener(new SelectAndSearchView.ChooseAssetListener() {
            @Override
            public void onAssetChosen(Asset asset) {
                fiatView.setFiat((Fiat)asset, LAST_CHECK != 0);
            }
        });

        ssv.setIncludesFiat(true);
        ssv.setIncludesCoin(false);
        ssv.setIncludesToken(false);

        FiatManager fiatManager = FiatManager.getFiatManagerFromFiatType(fiatType);

        fiatView.setOnDeleteListener(new FiatView.OnDeleteListener() {
            @Override
            public void onDelete(Asset asset) {
                ssv.removeAsset(asset);

                if(LAST_CHECK == 0) {
                    fiatManager.removeHardcodedFiat((Fiat)asset);
                }
                else if(LAST_CHECK == 1) {
                    fiatManager.removeFoundFiat((Fiat)asset);
                }
                else if(LAST_CHECK == 2) {
                    fiatManager.removeCustomFiat((Fiat)asset);
                }

                FiatManagerList.updateFiatManager(activity, fiatManager);

                ((FiatManagerActivity)activity).updateLayout();
            }
        });

        if(LAST_CHECK == 0) {
            ssv.setFiatOptions(fiatManager.hardcoded_fiats);
        }
        else if(LAST_CHECK == 1) {
            ssv.setFiatOptions(fiatManager.found_fiats);
        }
        else if(LAST_CHECK == 2) {
            ssv.setFiatOptions(fiatManager.custom_fiats);
        }

        ArrayList<FiatManager> fiatManagerArrayList = new ArrayList<>();
        fiatManagerArrayList.add(fiatManager);
        ssv.setFiatManagerOptions(fiatManagerArrayList);

        ssv.chooseFiat(fiatType);
    }

    @Override
    public Bundle onSaveInstanceStateImpl(Bundle bundle) {
        bundle.putInt("lastcheck", LAST_CHECK);
        return bundle;
    }

    @Override
    public void onRestoreInstanceStateImpl(Bundle bundle) {
        if(bundle != null) {
            LAST_CHECK = bundle.getInt("lastcheck");
        }
    }
}

package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.filter.DiscreteFilter;
import com.musicslayer.cryptobuddy.serialize.Serialization;

import java.util.ArrayList;

public class DiscreteFilterDialog extends FilterDialog {
    public DiscreteFilter discreteFilter;

    CheckBox[] C;
    ArrayList<Boolean> state = new ArrayList<>();

    public DiscreteFilterDialog(Activity activity, DiscreteFilter discreteFilter) {
        super(activity, discreteFilter);
        this.discreteFilter = discreteFilter;

        for(int i = 0; i < discreteFilter.choices.size(); i++) {
            state.add(discreteFilter.isIncluded(discreteFilter.choices.get(i)));
        }
    }

    public int getBaseViewID() {
        return R.id.discrete_filter_dialog;
    }

    public void createLayout() {
        setContentView(R.layout.dialog_discrete_filter);

        Button B_FILTER = findViewById(R.id.discrete_filter_dialog_applyFilterButton);
        B_FILTER.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                discreteFilter.user_choices = new ArrayList<>();
                discreteFilter.user_not_choices = new ArrayList<>();

                for(int i = 0; i < discreteFilter.choices.size(); i++) {
                    if(C[i].isChecked()) {
                        discreteFilter.user_choices.add(discreteFilter.choices.get(i));
                    }
                    else {
                        discreteFilter.user_not_choices.add(discreteFilter.choices.get(i));
                    }
                }

                isComplete = true;
                dismiss();
            }
        });

        Button B_SELECTALL = findViewById(R.id.discrete_filter_dialog_selectAllButton);
        B_SELECTALL.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                for(int i = 0; i < discreteFilter.choices.size(); i++) {
                    C[i].setChecked(true);
                }
            }
        });

        Button B_CLEARALL = findViewById(R.id.discrete_filter_dialog_clearAllButton);
        B_CLEARALL.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                for(int i = 0; i < discreteFilter.choices.size(); i++) {
                    C[i].setChecked(false);
                }
            }
        });

        updateLayout();
    }

    public void updateLayout() {
        LinearLayout L = findViewById(R.id.discrete_filter_dialog_checkBoxLayout);

        C = new CheckBox[discreteFilter.choices.size()];
        for(int i = 0; i < discreteFilter.choices.size(); i++) {
            C[i] = new CheckBox(this.activity);
            C[i].setChecked(state.get(i));
            C[i].setText(discreteFilter.choices.get(i));

            LinearLayout.LayoutParams LP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            LP.setMargins(0,0,0,50);
            C[i].setLayoutParams(LP);

            L.addView(C[i]);
        }
    }

    @Override
    public Bundle onSaveInstanceStateImpl(Bundle bundle) {
        state.clear();

        for(CheckBox checkBox : C) {
            state.add(checkBox.isChecked());
        }

        bundle.putString("state", Serialization.boolean_serializeArrayList(state));

        return bundle;
    }

    @Override
    public void onRestoreInstanceStateImpl(Bundle bundle) {
        if(bundle != null) {
            state = Serialization.boolean_deserializeArrayList(bundle.getString("state"));
        }
    }
}

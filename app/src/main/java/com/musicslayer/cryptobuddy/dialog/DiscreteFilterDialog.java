package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.filter.DiscreteFilter;

import java.util.ArrayList;

public class DiscreteFilterDialog extends BaseDialog {
    public DiscreteFilter filter;

    CheckBox[] C;

    public DiscreteFilterDialog(Activity activity, DiscreteFilter filter) {
        super(activity);
        this.filter = filter;
    }

    public int getBaseViewID() {
        return R.id.discrete_filter_dialog;
    }

    public void createLayout() {
        setContentView(R.layout.dialog_discrete_filter);

        Button B_FILTER = findViewById(R.id.discrete_filter_dialog_applyFilterButton);
        B_FILTER.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                filter.user_choices = new ArrayList<>();
                filter.user_not_choices = new ArrayList<>();

                for(int i = 0; i < filter.choices.size(); i++) {
                    if(C[i].isChecked()) {
                        filter.user_choices.add(filter.choices.get(i));
                    }
                    else {
                        filter.user_not_choices.add(filter.choices.get(i));
                    }
                }

                isComplete = true;
                dismiss();
            }
        });

        Button B_SELECTALL = findViewById(R.id.discrete_filter_dialog_selectAllButton);
        B_SELECTALL.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                for(int i = 0; i < filter.choices.size(); i++) {
                    C[i].setChecked(true);
                }
            }
        });

        Button B_CLEARALL = findViewById(R.id.discrete_filter_dialog_clearAllButton);
        B_CLEARALL.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                for(int i = 0; i < filter.choices.size(); i++) {
                    C[i].setChecked(false);
                }
            }
        });

        LinearLayout L = findViewById(R.id.discrete_filter_dialog_checkBoxLayout);


        if(C == null) {
            C = new CheckBox[filter.choices.size()];
            for(int i = 0; i < filter.choices.size(); i++) {
                C[i] = new CheckBox(this.activity);
                C[i].setChecked(filter.isIncluded(filter.choices.get(i)));
            }
        }

        for(int i = 0; i < filter.choices.size(); i++) {
            LinearLayout L_ROW = new LinearLayout(this.activity);
            L_ROW.setOrientation(LinearLayout.HORIZONTAL);
            L_ROW.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

            TextView T = new TextView(this.activity);
            T.setText(filter.choices.get(i));

            L_ROW.addView(C[i]);
            L_ROW.addView(T);

            L.addView(L_ROW);
        }
    }

    @Override
    public Bundle onSaveInstanceStateImpl(Bundle bundle) {
        for(int i = 0; i < C.length; i++) {
            bundle.putBoolean("checkbox" + i, C[i].isChecked());
        }

        return bundle;
    }

    @Override
    public void onRestoreInstanceStateImpl(Bundle bundle) {
        if(bundle != null) {
            C = new CheckBox[filter.choices.size()];
            for(int i = 0; i < filter.choices.size(); i++) {
                C[i] = new CheckBox(this.activity);
                C[i].setChecked(bundle.getBoolean("checkbox" + i));
            }
        }
    }
}

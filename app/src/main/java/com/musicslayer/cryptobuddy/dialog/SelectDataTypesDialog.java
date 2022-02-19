package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.util.ToastUtil;

import java.util.ArrayList;

// Select data types for import or export.

public class SelectDataTypesDialog extends BaseDialog {
    CheckBox[] C;
    ArrayList<Boolean> state = new ArrayList<>();

    public ArrayList<String> dataTypes;

    public ArrayList<String> user_CHOICES;

    public SelectDataTypesDialog(Activity activity, ArrayList<String> dataTypes) {
        super(activity);
        this.dataTypes = dataTypes;

        // Checkboxes always start as unselected.
        for(int i = 0; i < dataTypes.size(); i++) {
            state.add(false);
        }
    }

    public int getBaseViewID() {
        return R.id.select_data_types_dialog;
    }

    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_select_data_types);

        Button B_SELECT = findViewById(R.id.select_data_types_dialog_confirmButton);
        B_SELECT.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            @Override
            public void onClickImpl(View v) {
                user_CHOICES = new ArrayList<>();

                for(int i = 0; i < dataTypes.size(); i++) {
                    if(C[i].isChecked()) {
                        user_CHOICES.add(dataTypes.get(i));
                    }
                }

                if(user_CHOICES.isEmpty()) {
                    ToastUtil.showToast("nothing_selected");
                    return;
                }

                isComplete = true;
                dismiss();
            }
        });

        Button B_SELECTALL = findViewById(R.id.select_data_types_dialog_selectAllButton);
        B_SELECTALL.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                for(int i = 0; i < dataTypes.size(); i++) {
                    C[i].setChecked(true);
                }
            }
        });

        Button B_CLEARALL = findViewById(R.id.select_data_types_dialog_clearAllButton);
        B_CLEARALL.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                for(int i = 0; i < dataTypes.size(); i++) {
                    C[i].setChecked(false);
                }
            }
        });

        updateLayout();
    }

    public void updateLayout() {
        LinearLayout L = findViewById(R.id.select_data_types_dialog_checkBoxLayout);

        LinearLayout.LayoutParams LP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        LP.setMargins(0,0,0,50);

        C = new CheckBox[dataTypes.size()];
        for(int i = 0; i < dataTypes.size(); i++) {
            C[i] = new CheckBox(this.activity);
            C[i].setChecked(state.get(i));
            C[i].setText(dataTypes.get(i));
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

        bundle.putSerializable("state", state);

        return bundle;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onRestoreInstanceStateImpl(Bundle bundle) {
        if(bundle != null) {
            state = (ArrayList<Boolean>)bundle.getSerializable("state");
        }
    }
}

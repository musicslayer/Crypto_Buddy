package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import androidx.appcompat.widget.Toolbar;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.util.ToastUtil;

import java.util.ArrayList;

public class DeleteFiatsDialog extends BaseDialog {
    CheckBox[] C;
    ArrayList<Boolean> state = new ArrayList<>();

    String fiatType;

    public ArrayList<String> user_CHOICES;

    public DeleteFiatsDialog(Activity activity, String fiatType) {
        super(activity);
        this.fiatType = fiatType;

        // Checkboxes always start as unselected.
        for(int i = 0; i < 2; i++) {
            state.add(false);
        }
    }

    public int getBaseViewID() {
        return R.id.delete_fiats_dialog;
    }

    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_delete_fiats);

        Toolbar toolbar = findViewById(R.id.delete_fiats_dialog_toolbar);
        toolbar.setTitle("Delete " + fiatType + " Fiats");

        Button B_DELETE = findViewById(R.id.delete_fiats_dialog_deleteButton);
        B_DELETE.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            @Override
            public void onClickImpl(View v) {
                user_CHOICES = new ArrayList<>();

                if(C[0].isChecked()) {
                    user_CHOICES.add("found");
                }
                if(C[1].isChecked()) {
                    user_CHOICES.add("custom");
                }

                if(user_CHOICES.isEmpty()) {
                    ToastUtil.showToast(activity, "nothing_to_delete");
                    return;
                }

                isComplete = true;
                dismiss();
            }
        });

        updateLayout();
    }

    public void updateLayout() {
        LinearLayout L = findViewById(R.id.delete_fiats_dialog_checkBoxLayout);

        LinearLayout.LayoutParams LP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        LP.setMargins(0,0,0,50);

        C = new CheckBox[2];

        C[0] = new CheckBox(this.activity);
        C[0].setChecked(state.get(0));
        C[0].setText("Delete found fiats from the app's database.");
        C[0].setLayoutParams(LP);

        C[1] = new CheckBox(this.activity);
        C[1].setChecked(state.get(1));
        C[1].setText("Delete custom fiats from the app's database.");
        C[1].setLayoutParams(LP);

        L.addView(C[0]);
        L.addView(C[1]);
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

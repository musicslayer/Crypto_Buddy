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

public class DeleteTokensDialog extends BaseDialog {
    CheckBox[] C;
    ArrayList<Boolean> state = new ArrayList<>();

    public String tokenType;
    public boolean canGetJSON;

    public ArrayList<String> user_CHOICES;

    public DeleteTokensDialog(Activity activity, String tokenType, Boolean canGetJSON) {
        super(activity);
        this.tokenType = tokenType;
        this.canGetJSON = canGetJSON;

        // Checkboxes always start as unselected.
        for(int i = 0; i < 3; i++) {
            state.add(false);
        }
    }

    public int getBaseViewID() {
        return R.id.delete_tokens_dialog;
    }

    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_delete_tokens);

        Toolbar toolbar = findViewById(R.id.delete_tokens_dialog_toolbar);
        toolbar.setTitle("Delete " + tokenType + " Tokens");

        Button B_DELETE = findViewById(R.id.delete_tokens_dialog_deleteButton);
        B_DELETE.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            @Override
            public void onClickImpl(View v) {
                user_CHOICES = new ArrayList<>();

                if(C[0].isChecked()) {
                    user_CHOICES.add("downloaded");
                }
                if(C[1].isChecked()) {
                    user_CHOICES.add("found");
                }
                if(C[2].isChecked()) {
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
        LinearLayout L = findViewById(R.id.delete_tokens_dialog_checkBoxLayout);

        LinearLayout.LayoutParams LP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        LP.setMargins(0,0,0,50);

        C = new CheckBox[3];

        C[0] = new CheckBox(this.activity);
        C[0].setChecked(state.get(0));
        C[0].setText("Delete downloaded tokens from the app's database.");
        C[0].setLayoutParams(LP);

        C[1] = new CheckBox(this.activity);
        C[1].setChecked(state.get(1));
        C[1].setText("Delete found tokens from the app's database.");
        C[1].setLayoutParams(LP);

        C[2] = new CheckBox(this.activity);
        C[2].setChecked(state.get(2));
        C[2].setText("Delete custom tokens from the app's database.");
        C[2].setLayoutParams(LP);

        // Some token types can't be downloaded, so don't offer this deletion option.
        if(canGetJSON) { L.addView(C[0]); }
        L.addView(C[1]);
        L.addView(C[2]);
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

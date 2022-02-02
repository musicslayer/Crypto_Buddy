package com.musicslayer.cryptobuddy.view.settings;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;

import com.musicslayer.cryptobuddy.crash.CrashAdapterView;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.persistence.SettingList;
import com.musicslayer.cryptobuddy.settings.setting.MessageLengthSetting;
import com.musicslayer.cryptobuddy.settings.setting.Setting;
import com.musicslayer.cryptobuddy.util.ToastUtil;
import com.musicslayer.cryptobuddy.view.BorderedSpinnerView;

public class MessageSettingsView extends SettingsView {
    public MessageSettingsView(Context context) {
        super(context);
    }

    public MessageSettingsView(Context context, Setting setting) {
        super(context);

        this.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        this.setOrientation(LinearLayout.VERTICAL);

        final LinearLayout L_Row=new LinearLayout(context);
        L_Row.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        L_Row.setOrientation(LinearLayout.HORIZONTAL);
        L_Row.setGravity(Gravity.CENTER_VERTICAL);

        final TextView T=new TextView(context);
        T.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        T.setText(setting.getDisplayName() + ":");

        BorderedSpinnerView bsv = new BorderedSpinnerView(context);
        bsv.setOptions(setting.getModifiedOptionNames());
        bsv.setMargins(10, 0, 10, 0);

        final TextView prefText=new TextView(context);
        prefText.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

        int idx = setting.getOptionNames().indexOf(setting.chosenOptionName);
        if(idx == -1) {
            // If saved option choice no longer exists, use default value.
            idx = setting.getOptionNames().indexOf(setting.getDefaultOptionName());
        }
        prefText.setText(setting.getOptionDisplays().get(idx));

        bsv.spinner.setOnItemSelectedListener(new CrashAdapterView.CrashOnItemSelectedListener(context) {
            public void onNothingSelectedImpl(AdapterView<?> parent){}
            public void onItemSelectedImpl(AdapterView<?> parent, View view, int pos, long id) {
                setting.setSetting(pos);
                prefText.setText(setting.getOptionDisplays().get(pos));
                SettingList.saveSetting(context, setting);

                ToastUtil.loadAllToasts(context.getApplicationContext());
            }
        });

        bsv.spinner.setSelection(setting.chosenOptionPosition);

        final AppCompatButton B_MessageTest = new AppCompatButton(context);
        B_MessageTest.setText("Message Test");
        B_MessageTest.setOnClickListener(new CrashView.CrashOnClickListener(context) {
            public void onClickImpl(View v) {
                if(MessageLengthSetting.value.equals(android.widget.Toast.LENGTH_SHORT)) {
                    ToastUtil.showToast(context,"setting_message_test_short");
                }
                else {
                    ToastUtil.showToast(context,"setting_message_test_long");
                }
            }
        });

        prefText.setPadding(0,0,0, 50);

        L_Row.addView(T);
        L_Row.addView(bsv);
        L_Row.addView(B_MessageTest);

        this.addView(L_Row);
        this.addView(prefText);
    }
}

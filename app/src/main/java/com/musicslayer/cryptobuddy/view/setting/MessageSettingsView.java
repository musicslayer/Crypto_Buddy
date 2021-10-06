package com.musicslayer.cryptobuddy.view.setting;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;

import com.musicslayer.cryptobuddy.crash.CrashLinearLayout;
import com.musicslayer.cryptobuddy.crash.CrashOnClickListener;
import com.musicslayer.cryptobuddy.crash.CrashOnItemSelectedListener;
import com.musicslayer.cryptobuddy.persistence.Settings;
import com.musicslayer.cryptobuddy.util.ToastUtil;
import com.musicslayer.cryptobuddy.view.BorderedSpinnerView;

public class MessageSettingsView extends CrashLinearLayout {
    public MessageSettingsView(Context context) {
        super(context);
    }

    public MessageSettingsView(Context context, String settingName, String settingDisplayName, String[] settingOptions, String[] settingDescriptions) {
        super(context);

        this.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        this.setOrientation(LinearLayout.VERTICAL);

        final LinearLayout L_Row=new LinearLayout(context);
        L_Row.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        L_Row.setOrientation(LinearLayout.HORIZONTAL);
        L_Row.setGravity(Gravity.CENTER_VERTICAL);

        final TextView T=new TextView(context);
        T.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        T.setText(settingDisplayName + ":");

        BorderedSpinnerView bsv = new BorderedSpinnerView(context);
        bsv.setOptions(settingOptions);
        bsv.setMargins(10, 0, 10, 0);

        final TextView prefText=new TextView(context);
        prefText.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        prefText.setText(settingDescriptions[Settings.getSettingValue(settingName)]);

        bsv.spinner.setOnItemSelectedListener(new CrashOnItemSelectedListener(context) {
            public void onNothingSelectedImpl(AdapterView<?> parent){}
            public void onItemSelectedImpl(AdapterView<?> parent, View view, int pos, long id) {
                prefText.setText(settingDescriptions[pos]);
                Settings.setSetting(context, settingName, pos);
                ToastUtil.loadAllToasts(context.getApplicationContext());
            }
        });

        bsv.spinner.setSelection(Settings.getSettingValue(settingName));

        final AppCompatButton B_MessageTest = new AppCompatButton(context);
        B_MessageTest.setText("Message Test");
        B_MessageTest.setOnClickListener(new CrashOnClickListener(context) {
            public void onClickImpl(View v) {
                if(Settings.setting_message == android.widget.Toast.LENGTH_SHORT) {
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

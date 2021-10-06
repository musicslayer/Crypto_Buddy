package com.musicslayer.cryptobuddy.view.setting;

import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.crash.CrashLinearLayout;
import com.musicslayer.cryptobuddy.crash.CrashOnClickListener;
import com.musicslayer.cryptobuddy.crash.CrashOnDismissListener;
import com.musicslayer.cryptobuddy.crash.CrashOnItemSelectedListener;
import com.musicslayer.cryptobuddy.dialog.BaseDialogFragment;
import com.musicslayer.cryptobuddy.dialog.ConfirmDeleteAllAddressHistoryDialog;
import com.musicslayer.cryptobuddy.persistence.AddressHistory;
import com.musicslayer.cryptobuddy.persistence.Settings;
import com.musicslayer.cryptobuddy.util.LocaleManager;
import com.musicslayer.cryptobuddy.util.Toast;
import com.musicslayer.cryptobuddy.view.BorderedSpinnerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

public class NumericLocaleSettingsView extends CrashLinearLayout {
    public NumericLocaleSettingsView(Context context) {
        super(context);
    }

    public NumericLocaleSettingsView(Context context, String settingName, String settingDisplayName, String[] settingOptions, String[] settingDescriptions) {
        super(context);

        // Two options will be passed in, but we must dynamically add the rest.
        ArrayList<String> settingOptionsArrayList = new ArrayList<>();
        Collections.addAll(settingOptionsArrayList, settingOptions);

        ArrayList<String> settingDescriptionsArrayList = new ArrayList<>();
        Collections.addAll(settingDescriptionsArrayList, settingDescriptions);

        for(Locale locale : LocaleManager.getAvailableLocalesNumeric()) {
            settingOptionsArrayList.add(locale.toString());
            settingDescriptionsArrayList.add("Use " + locale.toString() + " numeric locale.");
        }

        this.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        this.setOrientation(LinearLayout.VERTICAL);

        final LinearLayout L_Row=new LinearLayout(context);
        L_Row.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        L_Row.setOrientation(LinearLayout.HORIZONTAL);
        L_Row.setGravity(Gravity.CENTER_VERTICAL);

        final TextView T=new TextView(context);
        T.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        T.setText(settingDisplayName + ":");

        BorderedSpinnerView bsv = new BorderedSpinnerView(context);
        bsv.setOptions(settingOptionsArrayList);
        bsv.setMargins(10, 0, 10, 0);

        final TextView prefText=new TextView(context);
        prefText.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        prefText.setText(settingDescriptionsArrayList.get(Settings.getSettingValue(settingName)));


        bsv.spinner.setOnItemSelectedListener(new CrashOnItemSelectedListener(context) {
            public void onNothingSelectedImpl(AdapterView<?> parent){}
            public void onItemSelectedImpl(AdapterView<?> parent, View view, int pos, long id) {
                prefText.setText(settingDescriptionsArrayList.get(pos));
                Settings.setSetting(context, settingName, pos);
            }
        });

        bsv.spinner.setSelection(Settings.getSettingValue(settingName));

        prefText.setPadding(0,0,0, 50);

        L_Row.addView(T);
        L_Row.addView(bsv);

        this.addView(L_Row);
        this.addView(prefText);
    }
}

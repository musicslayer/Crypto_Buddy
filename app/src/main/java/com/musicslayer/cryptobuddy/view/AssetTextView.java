package com.musicslayer.cryptobuddy.view;

import android.content.Context;

import com.musicslayer.cryptobuddy.crash.CrashTextView;
import com.musicslayer.cryptobuddy.settings.Setting;
import com.musicslayer.cryptobuddy.transaction.AssetQuantity;

public class AssetTextView extends CrashTextView {
    Boolean isLoss;
    AssetQuantity assetQuantity;

    public AssetTextView(Context context) {
        super(context);
    }

    public AssetTextView(Context context, Boolean isLoss, AssetQuantity assetQuantity) {
        super(context);
        this.isLoss = isLoss;
        this.assetQuantity = assetQuantity;

        this.makeLayout();
    }

    public void makeLayout() {
        String text;

        if(assetQuantity == null) {
            text = "-";
        }
        else {
            text = assetQuantity.toString();

            // Number is already formatted by Locale. Just apply the color red here if we need it.
            String lossSetting = Setting.getSettingValueFromKey("LossValuesSetting");
            if(isLoss && ("red".equals(lossSetting) || "red_match_locale".equals(lossSetting) || "red_negative".equals(lossSetting) || "red_parentheses".equals(lossSetting))) {
                this.setTextColor(0xFFFF0000);
            }
        }

        this.setText(text);
    }
}

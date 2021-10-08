package com.musicslayer.cryptobuddy.view;

import android.content.Context;

import com.musicslayer.cryptobuddy.crash.CrashTextView;
import com.musicslayer.cryptobuddy.transaction.AssetQuantity;
import com.musicslayer.cryptobuddy.persistence.Settings;

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
            if(isLoss && ("red".equals(Settings.setting_loss) || "red_match_locale".equals(Settings.setting_loss) || "red_negative".equals(Settings.setting_loss) || "red_parentheses".equals(Settings.setting_loss))) {
                this.setTextColor(0xFFFF0000);
            }
        }

        this.setText(text);
    }
}

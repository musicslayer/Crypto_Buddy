package com.musicslayer.cryptobuddy.view;

import android.content.Context;

import androidx.appcompat.widget.AppCompatTextView;

import com.musicslayer.cryptobuddy.transaction.AssetQuantity;
import com.musicslayer.cryptobuddy.persistence.Settings;

public class AssetTextView extends AppCompatTextView {
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
            if(isLoss) {
                if("negative".equals(Settings.setting_loss) || "red_negative".equals(Settings.setting_loss)) {
                    text = "-" + text;
                }

                if("parenthesis".equals(Settings.setting_loss) || "red_parenthesis".equals(Settings.setting_loss)) {
                    text = "(" + text + ")";
                }

                if("red".equals(Settings.setting_loss) || "red_negative".equals(Settings.setting_loss) || "red_parenthesis".equals(Settings.setting_loss)) {
                    this.setTextColor(0xFFFF0000);
                }
            }
        }

        this.setText(text);
    }
}

package com.musicslayer.cryptobuddy.rich;

import androidx.annotation.NonNull;

import com.musicslayer.cryptobuddy.settings.setting.LossValuesSetting;
import com.musicslayer.cryptobuddy.transaction.AssetQuantity;

import java.math.BigDecimal;

public class RichStringBuilder {
    public StringBuilder s = new StringBuilder();
    boolean isRich;

    public RichStringBuilder(boolean isRich) {
        this.isRich = isRich;
    }

    public RichStringBuilder appendAssetQuantity(AssetQuantity assetQuantity) {
        // Append AssetQuantity, applying rich formatting for color if applicable.
        String str;

        if(assetQuantity == null) {
            str = "-";
            if(isRich) {
                str = enrich(str);
            }
        }
        else {
            if(isRich) {
                str = enrich(assetQuantity.toString());

                String lossSetting = LossValuesSetting.value;
                if((assetQuantity.assetAmount.isLoss || assetQuantity.assetAmount.amount.compareTo(BigDecimal.ZERO) < 0) && ("red".equals(lossSetting) || "red_match_locale".equals(lossSetting) || "red_negative".equals(lossSetting) || "red_parentheses".equals(lossSetting))) {
                    // Make the string red.
                    str = "<font color=#ff0000>" + str + "</font>";
                }
                // else, just use the default color, so that it matches the theme.
            }
            else {
                str = assetQuantity.toRawString();
            }
        }

        s.append(str);
        return this;
    }

    public RichStringBuilder appendRich(String str) {
        // Append text but format for rich if applicable.
        if(isRich) {
            s.append(enrich(str));
        }
        else {
            s.append(str);
        }

        return this;
    }

    public RichStringBuilder append(String str) {
        // Do not apply rich formatting. Input string is assumed to already be in rich format if that is desired.
        s.append(str);
        return this;
    }

    public String enrich(String str) {
        // Just deal with spaces and newlines for now.
        str = str.replace("\n", "<br/>"); // Don't bother with \r here.
        str = str.replace(" ", "&nbsp;");
        return str;
    }

    @NonNull
    @Override
    public String toString() {
        return s.toString();
    }
}
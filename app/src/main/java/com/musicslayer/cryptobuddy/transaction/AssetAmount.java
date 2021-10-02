package com.musicslayer.cryptobuddy.transaction;

import androidx.annotation.NonNull;

import com.musicslayer.cryptobuddy.persistence.Settings;
import com.musicslayer.cryptobuddy.util.LocaleManager;
import com.musicslayer.cryptobuddy.util.Serialization;

import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class AssetAmount implements Serialization.SerializableToJSON {
    public final static int MAXSCALE = 20;

    public BigDecimal amount;

    // Amounts should always be positive.
    // Transactions can set this to be true for loses, but a standalone AssetAmount should keep this as false.
    public boolean isLoss = false;

    // BigDecimal cannot support infinity, so we have a manual flag that can be set.
    public boolean isInfinity = false;

    // All assets we support have a fixed number of decimals, so allowing String input is a universal way to specify the amount.
    public AssetAmount(String s) {
        this(new BigDecimal(s));
    }

    private AssetAmount(BigDecimal d) {
        amount = d;
    }

    public static AssetAmount makeInfinityAssetAmount() {
        // Use 0 as the value, and set the "isInfinity" flag.
        AssetAmount infinityAssetAmount = new AssetAmount("0");
        infinityAssetAmount.isInfinity = true;
        return infinityAssetAmount;
    }

    @NonNull
    public String toScaledString(int scale, boolean hasSlidingScale) {
        // Fiat and Crypto have different rules for scaling.
        String s;

        if(isInfinity) {
            s = "∞";
        }
        else {
            if(hasSlidingScale) {
                // Show between scale and MAXSCALE
                BigDecimal S = amount.movePointRight(scale);
                BigDecimal I = new BigDecimal(S.toBigInteger());
                BigDecimal D = S.subtract(I);
                if(I.compareTo(BigDecimal.ZERO) > 0) {
                    // Just keep "scale" digits.
                    s = amount.setScale(scale, RoundingMode.HALF_UP).toPlainString();
                }
                else {
                    // Keep up to MAXSCALE digits.
                    D = D.setScale(MAXSCALE, RoundingMode.HALF_UP);
                    if(D.compareTo(BigDecimal.ZERO) == 0) {
                        // Just keep "scale" digits. Everything else is zero anyway.
                        s = amount.setScale(scale, RoundingMode.HALF_UP).toPlainString();
                    }
                    else {
                        // Keep only 1 non-zero digit.
                        char[] chars = D.toPlainString().toCharArray();
                        int newScale = scale;

                        // First 2 chars are "0." so skip them.
                        for(int i = 2; i < chars.length; i++) {
                            newScale ++;
                            if(chars[i] != '0') { break; }
                        }

                        s = amount.setScale(newScale, RoundingMode.HALF_UP).toPlainString();

                        // If we round up a 9, the last digit will now be zero, so we need to get rid of it.
                        if(s.endsWith("0")) {
                            s = amount.setScale(newScale - 1, RoundingMode.HALF_UP).toPlainString();
                        }
                    }
                }
            }
            else {
                // Crypto starts with fixed digits and truncates based on Setting.
                if("Truncated".equals(Settings.setting_decimal)) {
                    s = amount.setScale(scale, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
                }
                else {
                    s = amount.setScale(scale, RoundingMode.HALF_UP).toPlainString();
                }
            }

            // In all finite cases, we may apply a certain numeric appearance based on the setting.
            Locale L = LocaleManager.getSettingLocaleNumeric();
            if(L != null) {
                BigDecimal D = new BigDecimal(s);
                NumberFormat nf = NumberFormat.getInstance(L);
                DecimalFormat df = ((DecimalFormat)nf);

                String pattern = df.toLocalizedPattern();
                char decimalSeparator_c = df.getDecimalFormatSymbols().getDecimalSeparator();
                String decimalSeparator = String.valueOf(decimalSeparator_c);

                int idx_decimal = pattern.lastIndexOf(decimalSeparator_c);
                if(idx_decimal != -1) {
                    // Insert the exact number of decimal places in the pattern that we need, despite what the Locale says.
                    String pattern_front = pattern.substring(0, idx_decimal);
                    String pattern_back;
                    if(D.scale() == 0) {
                        pattern_back = "";
                    }
                    else {
                        pattern_back = String.format("%0" + D.scale() + "d", 0).replace("0", String.valueOf(df.getDecimalFormatSymbols().getZeroDigit()));
                    }

                    // Use "format" because some locales (ccp) have characters ('\uD804' 55300) that are altered by concatenation.
                    pattern = String.format("%s%s%s", pattern_front, decimalSeparator, pattern_back);
                    df.applyLocalizedPattern(pattern);
                }

                s = df.format(D);
            }
        }

        return s;
    }

    public String toNumericScaledString(int scale, boolean hasSlidingScale) {
        // Returns toScaledString, but with a negative sign for a loss.
        String s = this.toScaledString(scale, hasSlidingScale);

        if(isLoss) {
            s = "-" + s;
        }

        return s;
    }

    private BigDecimal numericAmount() {
        // Returns amount, but with a negative sign for a loss.
        BigDecimal newAssetAmount = this.amount;
        if(isLoss) {
            newAssetAmount = newAssetAmount.negate();
        }

        return newAssetAmount;
    }

    private int compare(AssetAmount other) {
        // Deal with both positive and negative infinity.
        boolean isInf = isInfinity;
        boolean isOtherInf = other.isInfinity;

        if(isInf || isOtherInf) {
            if(isInf && !isOtherInf) {
                if(isLoss) {
                    return -1;
                }
                else {
                    return 1;
                }
            }
            else if(!isInf && isOtherInf) {
                if(other.isLoss) {
                    return 1;
                }
                else {
                    return -1;
                }
            }
            else {
                // Both are infinity. Only the sign matters.
                return Boolean.compare(!isLoss, !other.isLoss);
            }
        }

        // At this point, everything is finite.

        int valueAmount = numericAmount().compareTo(other.numericAmount());
        if(valueAmount != 0) {
            return valueAmount;
        }

        // For cosmetic sake, a loss of zero is less than a gain of zero.
        return Boolean.compare(!isLoss, !other.isLoss);
    }

    public static int compare(AssetAmount a, AssetAmount b) {
        boolean isValidA = a != null;
        boolean isValidB = b != null;

        // Null is always smaller than a real action.
        if(isValidA & isValidB) { return a.compare(b); }
        else { return Boolean.compare(isValidA, isValidB); }
    }

    public AssetAmount add(AssetAmount other) {
        return new AssetAmount(amount.add(other.amount));
    }

    public AssetAmount subtract(AssetAmount other) {
        return new AssetAmount(amount.subtract(other.amount));
    }

    public AssetAmount multiply(AssetAmount other) {
        return new AssetAmount(amount.multiply(other.amount));
    }

    public AssetAmount divide(AssetAmount other) {
        // When dividing, there are two things we have to worry about:
        // 1. Dividing by zero, where we return "Infinity".
        // 2. Specifying the scale of the result, where we just make sure we have more digits than any asset would ever use.
        if(BigDecimal.ZERO.compareTo(other.amount) == 0) {
            return AssetAmount.makeInfinityAssetAmount();
        }
        else {
            return new AssetAmount(amount.divide(other.amount, 50, RoundingMode.HALF_UP));
        }
    }

    public String serializeToJSON() {
        return "{\"amount\":\"" + amount.toString() + "\"}";
    }

    public static AssetAmount deserializeFromJSON(String s) throws org.json.JSONException {
        JSONObject o = new JSONObject(s);
        BigDecimal amount = new BigDecimal(o.getString("amount"));
        return new AssetAmount(amount);
    }
}

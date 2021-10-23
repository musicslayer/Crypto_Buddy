package com.musicslayer.cryptobuddy.transaction;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.musicslayer.cryptobuddy.i18n.LocaleManager;
import com.musicslayer.cryptobuddy.serialize.Serialization;
import com.musicslayer.cryptobuddy.settings.NumberDecimalPlacesSetting;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class AssetAmount implements Serialization.SerializableToJSON, Parcelable {
    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(amount.toString());
    }

    public static final Parcelable.Creator<AssetAmount> CREATOR = new Parcelable.Creator<AssetAmount>() {
        @Override
        public AssetAmount createFromParcel(Parcel in) {
            return new AssetAmount(new BigDecimal(in.readString()));
        }

        // We just need to copy this and change the type to match our class.
        @Override
        public AssetAmount[] newArray(int size) {
            return new AssetAmount[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

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
        // Return a scaled string without any minus sign or Locale formatting.

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
                if("Truncated".equals(NumberDecimalPlacesSetting.value)) {
                    s = amount.setScale(scale, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
                }
                else {
                    s = amount.setScale(scale, RoundingMode.HALF_UP).toPlainString();
                }
            }
        }

        return s;
    }

    public String toFormattedScaledString(int scale, boolean hasSlidingScale) {
        // Returns a scaled string, properly formatted based on the numeric Locale setting.
        String s = this.toScaledString(scale, hasSlidingScale);

        if(isLoss) {
            s = LocaleManager.formatNegativeNumber(s);
        }
        else {
            s = LocaleManager.formatPositiveNumber(s);
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
            else if(!isInf) {
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

    public String serializationVersion() { return "1"; }

    public String serializeToJSON() throws org.json.JSONException {
        return new Serialization.JSONObjectWithNull()
            .put("amount", Serialization.bigdecimal_serialize(amount))
            .put("isLoss", Serialization.boolean_serialize(isLoss))
            .put("isInfinity", Serialization.boolean_serialize(isInfinity))
            .toStringOrNull();
    }

    public static AssetAmount deserializeFromJSON1(String s) throws org.json.JSONException {
        Serialization.JSONObjectWithNull o = new Serialization.JSONObjectWithNull(s);
        BigDecimal amount = Serialization.bigdecimal_deserialize(o.getString("amount"));
        boolean isLoss = Serialization.boolean_deserialize(o.getString("isLoss"));
        boolean isInfinity = Serialization.boolean_deserialize(o.getString("isInfinity"));

        AssetAmount a = new AssetAmount(amount);
        a.isLoss = isLoss;
        a.isInfinity = isInfinity;

        return a;
    }
}

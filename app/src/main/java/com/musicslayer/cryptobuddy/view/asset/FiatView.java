package com.musicslayer.cryptobuddy.view.asset;

import android.content.Context;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

import com.musicslayer.cryptobuddy.asset.fiat.Fiat;
import com.musicslayer.cryptobuddy.crash.CrashLinearLayout;

public class FiatView extends CrashLinearLayout {
    public Fiat fiat;

    public FiatView(Context context) {
        this(context, null);
    }

    public FiatView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.makeLayout();
    }

    public void setFiat(Fiat fiat) {
        this.fiat = fiat;

        this.removeAllViews();
        this.makeLayout();
    }

    public void makeLayout() {
        this.setOrientation(VERTICAL);

        Context context = getContext();

        if(fiat == null) {
            setVisibility(GONE);
        }
        else {
            setVisibility(VISIBLE);

            String text = "Fiat Info:\n" +
                "  Name = " + fiat.getDisplayName() + "\n" +
                "  Symbol = " + fiat.getName() + "\n" +
                "  Decimals = " + fiat.getScale();

            AppCompatTextView T = new AppCompatTextView(context);
            T.setText(text);

            this.addView(T);
        }
    }
}

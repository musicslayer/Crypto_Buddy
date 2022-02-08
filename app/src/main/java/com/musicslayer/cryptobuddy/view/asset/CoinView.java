package com.musicslayer.cryptobuddy.view.asset;

import android.content.Context;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.crash.CrashLinearLayout;

public class CoinView extends CrashLinearLayout {
    public Coin coin;

    public CoinView(Context context) {
        this(context, null);
    }

    public CoinView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.makeLayout();
    }

    public void setCoin(Coin coin) {
        this.coin = coin;

        this.removeAllViews();
        this.makeLayout();
    }

    public void makeLayout() {
        this.setOrientation(VERTICAL);

        Context context = getContext();

        if(coin == null) {
            setVisibility(GONE);
        }
        else {
            setVisibility(VISIBLE);

            String text = "Coin Info:\n" +
                "  Name = " + coin.getDisplayName() + "\n" +
                "  Symbol = " + coin.getName() + "\n" +
                "  Decimals = " + coin.getScale();

            AppCompatTextView T = new AppCompatTextView(context);
            T.setText(text);

            this.addView(T);
        }
    }
}

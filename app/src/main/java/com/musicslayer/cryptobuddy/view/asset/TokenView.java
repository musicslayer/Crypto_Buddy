package com.musicslayer.cryptobuddy.view.asset;

import android.content.Context;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

import com.musicslayer.cryptobuddy.asset.crypto.token.Token;
import com.musicslayer.cryptobuddy.crash.CrashLinearLayout;

public class TokenView extends CrashLinearLayout {
    public Token token;

    public TokenView(Context context) {
        this(context, null);
    }

    public TokenView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.makeLayout();
    }

    public void setToken(Token token) {
        this.token = token;

        this.removeAllViews();
        this.makeLayout();
    }

    public void makeLayout() {
        this.setOrientation(VERTICAL);

        Context context = getContext();

        if(token == null) {
            setVisibility(GONE);
        }
        else {
            setVisibility(VISIBLE);

            AppCompatTextView T = new AppCompatTextView(context);
            T.setText(token.toString());

            this.addView(T);
        }
    }
}

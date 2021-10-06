package com.musicslayer.cryptobuddy.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.crash.CrashLinearLayout;
import com.musicslayer.cryptobuddy.crash.CrashOnClickListener;
import com.musicslayer.cryptobuddy.persistence.Settings;
import com.musicslayer.cryptobuddy.util.Serialization;

import java.util.ArrayList;
import java.util.Random;

public class ConfirmationView extends CrashLinearLayout {
    ArrayList<Integer> randomCode;
    ArrayList<Integer> lastDigits;
    int buttonSize = 150;

    // By default we require 4 digits, but extra sensitive things can increase this number.
    int numDigits = 4;

    private ConfirmationView.ConfirmationListener confirmationListener;

    public ConfirmationView(Context context) {
        this(context, null);
    }

    public ConfirmationView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        makeRandomDigits();
        makeLayout();
    }

    public void setNumDigits(int numDigits) {
        this.numDigits = numDigits;

        this.removeAllViews();
        makeRandomDigits();
        makeLayout();
    }

    public void makeRandomDigits() {
        // We don't need high quality randomness, we just need something that the user has to confirm.
        Random rand = new Random();
        randomCode = new ArrayList<>();
        for(int i = 0; i < numDigits; i++) {
            randomCode.add(rand.nextInt(10));
        }
    }

    public void makeLayout() {
        if(Settings.setting_confirm) {
            this.makeLayoutConfirmation();
        }
        else {
            this.makeLayoutBypass();
        }
    }

    @SuppressLint("SetTextI18n")
    public void makeLayoutConfirmation() {
        this.setOrientation(LinearLayout.VERTICAL);

        lastDigits = new ArrayList<>();
        for(int i = 0; i < numDigits; i++) {
            lastDigits.add(-1);
        }

        Context context = getContext();

        TextView messageText = new TextView(context);
        messageText.setText("Confirm by pressing these buttons in sequence: " + getIntArrayText(randomCode));

        TextView lastDigitsText = new TextView(context);
        lastDigitsText.setTextSize(18);
        lastDigitsText.setText(getIntArrayText(lastDigits));

        AppCompatButton BDA = new AppCompatButton(context);
        BDA.setLayoutParams(new LinearLayout.LayoutParams(buttonSize, buttonSize));
        BDA.setEnabled(false);

        AppCompatButton BDB = new AppCompatButton(context);
        BDB.setLayoutParams(new LinearLayout.LayoutParams(buttonSize, buttonSize));
        BDB.setEnabled(false);

        AppCompatButton[] B = new AppCompatButton[10];
        for(int i = 0; i < 10; i++) {
            int ii = i;

            B[i] = new AppCompatButton(context);
            B[i].setText(Integer.toString(i));
            B[i].setLayoutParams(new LinearLayout.LayoutParams(buttonSize, buttonSize));
            B[i].setOnClickListener(new CrashOnClickListener(context) {
                @Override
                public void onClickImpl(View view) {
                    for(int j = 0; j < numDigits - 1; j++) {
                        lastDigits.set(j, lastDigits.get(j + 1));
                    }
                    lastDigits.set(numDigits - 1, ii);

                    lastDigitsText.setText(getIntArrayText(lastDigits));
                    checkConfirmation();
                }
            });
        }

        LinearLayout L1 = new LinearLayout(context);
        LinearLayout L2 = new LinearLayout(context);
        LinearLayout L3 = new LinearLayout(context);
        LinearLayout L4 = new LinearLayout(context);

        L1.setOrientation(LinearLayout.HORIZONTAL);
        L2.setOrientation(LinearLayout.HORIZONTAL);
        L3.setOrientation(LinearLayout.HORIZONTAL);
        L4.setOrientation(LinearLayout.HORIZONTAL);

        L1.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        L2.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        L3.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        L4.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

        L1.addView(B[1]);
        L1.addView(B[2]);
        L1.addView(B[3]);

        L2.addView(B[4]);
        L2.addView(B[5]);
        L2.addView(B[6]);

        L3.addView(B[7]);
        L3.addView(B[8]);
        L3.addView(B[9]);

        L4.addView(BDA);
        L4.addView(B[0]);
        L4.addView(BDB);

        this.addView(messageText);
        this.addView(lastDigitsText);
        this.addView(L1);
        this.addView(L2);
        this.addView(L3);
        this.addView(L4);
    }

    public void makeLayoutBypass() {
        Context context = getContext();

        AppCompatButton B = new AppCompatButton(context);
        B.setText("Confirm");
        B.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_check_24, 0, 0, 0);
        B.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        B.setOnClickListener(new CrashOnClickListener(context) {
            @Override
            public void onClickImpl(View view) {
                if(confirmationListener != null) {
                    confirmationListener.onConfirmation(ConfirmationView.this);
                }
            }
        });

        this.addView(B);
    }

    public void checkConfirmation() {
        if(confirmationListener != null && randomCode.equals(lastDigits)) {
            confirmationListener.onConfirmation(this);
        }
    }

    public String getIntArrayText(ArrayList<Integer> arrayList) {
        StringBuilder s = new StringBuilder();
        for(int i : arrayList) {
            if(i == -1) {
                s.append("*");
            }
            else {
                s.append(i);
            }
        }
        return s.toString();
    }

    public void setOnConfirmationListener(ConfirmationView.ConfirmationListener confirmationListener) {
        this.confirmationListener = confirmationListener;
    }

    abstract public static class ConfirmationListener {
        abstract public void onConfirmation(ConfirmationView confirmationView);
    }

    @Override
    public Parcelable onSaveInstanceStateImpl(Parcelable state)
    {
        Bundle bundle = new Bundle();
        bundle.putParcelable("superState", state);

        bundle.putString("randomCode", Serialization.int_serializeArrayList(randomCode));
        bundle.putString("lastDigits", Serialization.int_serializeArrayList(lastDigits));
        bundle.putInt("numDigits", numDigits);

        return bundle;
    }

    @Override
    public Parcelable onRestoreInstanceStateImpl(Parcelable state)
    {
        if (state instanceof Bundle) // implicit null check
        {
            Bundle bundle = (Bundle) state;
            state = bundle.getParcelable("superState");

            randomCode = Serialization.int_deserializeArrayList(bundle.getString("randomCode"));
            lastDigits = Serialization.int_deserializeArrayList(bundle.getString("lastDigits"));
            numDigits = bundle.getInt("numDigits");

            this.removeAllViews();
            makeLayout();
        }
        return state;
    }
}

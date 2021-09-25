package com.musicslayer.cryptobuddy.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.persistence.Settings;

import java.util.Random;

public class ConfirmationView extends LinearLayout {
    int[] randomCode;
    int[] lastDigits = new int[] {-1, -1, -1, -1};
    int buttonSize = 150;

    private ConfirmationView.ConfirmationListener confirmationListener;

    public ConfirmationView(Context context) {
        this(context, null);
    }

    public ConfirmationView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        if(Settings.setting_confirm) {
            this.makeLayoutConfirmation(context);
        }
        else {
            this.makeLayoutBypass(context);
        }
    }

    @SuppressLint("SetTextI18n")
    public void makeLayoutConfirmation(Context context) {
        this.setOrientation(LinearLayout.VERTICAL);

        Random rand = new Random();
        randomCode = new int[] {rand.nextInt(10), rand.nextInt(10), rand.nextInt(10), rand.nextInt(10)};

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
            B[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    lastDigits[0] = lastDigits[1];
                    lastDigits[1] = lastDigits[2];
                    lastDigits[2] = lastDigits[3];
                    lastDigits[3] = ii;

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

    public void makeLayoutBypass(Context context) {
        AppCompatButton B = new AppCompatButton(context);
        B.setText("Confirm");
        B.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_check_24, 0, 0, 0);
        B.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        B.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(confirmationListener != null) {
                    confirmationListener.onConfirmation(ConfirmationView.this);
                }
            }
        });

        this.addView(B);
    }

    public void checkConfirmation() {
        if(confirmationListener != null && (randomCode[0] == lastDigits[0]) && (randomCode[1] == lastDigits[1]) && (randomCode[2] == lastDigits[2]) && (randomCode[3] == lastDigits[3])) {
            confirmationListener.onConfirmation(this);
        }
    }

    public String getIntArrayText(int[] array) {
        StringBuilder s = new StringBuilder();
        for(int i : array) {
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
}

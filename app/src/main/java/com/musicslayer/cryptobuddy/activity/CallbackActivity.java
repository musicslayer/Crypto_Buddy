package com.musicslayer.cryptobuddy.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

// This Activity class only exists to handle callbacks, not to be seen by the user.
public class CallbackActivity extends AppCompatActivity {
    public static CallbackListener callbackListener;

    public static void setCallbackListener(CallbackListener callbackListener) {
        CallbackActivity.callbackListener = callbackListener;
    }


    abstract public static class CallbackListener {
        abstract public void onCallback(Intent intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(callbackListener != null) {
            callbackListener.onCallback(getIntent());
        }
        finish();
    }
}

package com.musicslayer.cryptobuddy.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

// This Activity class only exists to handle callbacks, not to be seen by the user.
// This should not be a subclass of BaseActivity.
public class CallbackActivity extends AppCompatActivity {
    public final static boolean[] wasCallbackFired = new boolean[1];
    public final static Intent[] lastIntent = new Intent[1];

    public static void resetState() {
        CallbackActivity.wasCallbackFired[0] = false;
        CallbackActivity.lastIntent[0] = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wasCallbackFired[0] = true;
        lastIntent[0] = getIntent();
        finish();
    }
}

package com.musicslayer.cryptobuddy.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

// This Activity class only exists to handle callbacks, not to be seen by the user.
public class CallbackActivity extends AppCompatActivity {
    public final static boolean[] wasCallbackFired = new boolean[1];
    public final static Intent[] lastIntent = new Intent[1];

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wasCallbackFired[0] = true;
        lastIntent[0] = getIntent();
        finish();
    }
}

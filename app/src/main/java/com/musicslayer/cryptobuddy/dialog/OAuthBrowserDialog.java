package com.musicslayer.cryptobuddy.dialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.musicslayer.cryptobuddy.BuildConfig;
import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.activity.CallbackActivity;
import com.musicslayer.cryptobuddy.decode.Alphanumeric;
import com.musicslayer.cryptobuddy.encryption.Encryption;
import com.musicslayer.cryptobuddy.util.AuthUtil;
import com.musicslayer.cryptobuddy.util.ToastUtil;
import com.musicslayer.cryptobuddy.util.URLUtil;
import com.musicslayer.cryptobuddy.util.WebUtil;

import java.util.HashMap;

// Dialog that allows user to grant OAuth authorization using a Browser App.

public class OAuthBrowserDialog extends BaseDialog {
    public byte[] user_CODE_E;

    String state;

    AuthUtil.OAuthInfo oAuthInfo;

    public OAuthBrowserDialog(Activity activity, AuthUtil.OAuthInfo oAuthInfo) {
        super(activity);
        this.oAuthInfo = oAuthInfo;
    }

    public int getBaseViewID() {
        return R.id.oauth_browser_dialog;
    }

    @Override
    public void onBackPressedImpl() {
        CallbackActivity.wasCallbackFired[0] = false;
        CallbackActivity.lastIntent[0] = null;
        super.onBackPressedImpl();
    }

    @Override
    public void onResumeImpl() {
        // This action cannot be taken if the activity is not active, so wait until we resume.
        if(CallbackActivity.wasCallbackFired[0]) {
            CallbackActivity.wasCallbackFired[0] = false;
            Intent intent = CallbackActivity.lastIntent[0];
            CallbackActivity.lastIntent[0] = null;

            String responseURL = intent.getData().toString();
            if(responseURL.startsWith("com.musicslayer.cryptobuddy://oauth")) {
                HashMap<String, String> parameters = URLUtil.parseURL(responseURL);
                if(parameters.containsKey("code") && parameters.containsKey("state")) {
                    // Validate we get the same state back for security.
                    String responseState = parameters.get("state");
                    if(!state.equals(responseState)) {
                        ToastUtil.showToast(activity, "authorization_failed");
                        dismiss();
                    }

                    // Now extract the code and return it.
                    String code = parameters.get("code");
                    if(isValidCode(code)) {
                        user_CODE_E = Encryption.encrypt(code, BuildConfig.key_oauth_code);

                        isComplete = true;
                        dismiss();
                    }
                }
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_oauth_browser);

        CallbackActivity.wasCallbackFired[0] = false;

        // Only launch Browser once.
        if(savedInstanceState == null) {
            // Use this random string to validate the OAuth response.
            state = Alphanumeric.createRandomString(40);

            String authURL = oAuthInfo.authURLBase +
                    "?client_id=" + oAuthInfo.client_id +
                    "&redirect_uri=" + oAuthInfo.redirect_uri +
                    "&response_type=" + oAuthInfo.response_type +
                    "&scope=" + TextUtils.join(",", oAuthInfo.scopes) +
                    "&state=" + state +
                    "&account=all"; // Needed for Coinbase, but ignored by other exchanges.

            WebUtil.launchURL(activity, authURL);
        }
    }

    private boolean isValidCode(String code) {
        // Valid codes are alphanumeric and have at least one character.
        try {
            return code != null && code.length() > 0 && Alphanumeric.isAlphanumeric(code);
        }
        catch(Exception ignored) {
            return false;
        }
    }

    @Override
    public Bundle onSaveInstanceStateImpl(Bundle bundle) {
        bundle.putString("state", state);
        return bundle;
    }

    @Override
    public void onRestoreInstanceStateImpl(Bundle bundle) {
        if(bundle != null) {
            state = bundle.getString("state");
        }
    }
}
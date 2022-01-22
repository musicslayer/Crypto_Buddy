package com.musicslayer.cryptobuddy.dialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.browser.customtabs.CustomTabsIntent;

import com.musicslayer.cryptobuddy.BuildConfig;
import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.activity.CallbackActivity;
import com.musicslayer.cryptobuddy.decode.Alphanumeric;
import com.musicslayer.cryptobuddy.encryption.Encryption;
import com.musicslayer.cryptobuddy.util.AuthUtil;
import com.musicslayer.cryptobuddy.util.ToastUtil;
import com.musicslayer.cryptobuddy.util.URLUtil;

import java.util.HashMap;

// Dialog that allows user to grant OAuth authorization.

public class OAuthCustomTabDialog extends BaseDialog {
    public byte[] user_CODE_E;

    String state;

    AuthUtil.OAuthInfo oAuthInfo;

    public OAuthCustomTabDialog(Activity activity, AuthUtil.OAuthInfo oAuthInfo) {
        super(activity);
        this.oAuthInfo = oAuthInfo;
    }

    public int getBaseViewID() {
        return R.id.oauth_custom_tab_dialog;
    }

    @SuppressLint("SetJavaScriptEnabled")
    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_oauth_custom_tab);

        CallbackActivity.setCallbackListener(new CallbackActivity.CallbackListener() {
            @Override
            public void onCallback(Intent intent) {
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
        });

        // Only launch CustomTab once.
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

            Intent browserIntent = new Intent(Intent.ACTION_VIEW);
            browserIntent.setData(Uri.parse(authURL));
            activity.startActivity(browserIntent);

/*
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            customTabsIntent.intent.putExtra(Intent.EXTRA_REFERRER, Uri.parse("android-app://" + activity.getPackageName()));
            customTabsIntent.launchUrl(activity, Uri.parse(authURL));

 */
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
}
package com.musicslayer.cryptobuddy.dialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.musicslayer.cryptobuddy.BuildConfig;
import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.decode.Alphanumeric;
import com.musicslayer.cryptobuddy.encryption.Encryption;
import com.musicslayer.cryptobuddy.util.AuthUtil;
import com.musicslayer.cryptobuddy.util.ToastUtil;
import com.musicslayer.cryptobuddy.util.URLUtil;

import java.util.HashMap;

// Dialog that allows user to grant OAuth authorization.
// TODO Use custom tabs instead of WebView.

public class OAuthDialog extends BaseDialog {
    public byte[] user_CODE_E;

    AuthUtil.OAuthInfo oAuthInfo;

    public OAuthDialog(Activity activity, AuthUtil.OAuthInfo oAuthInfo) {
        super(activity);
        this.oAuthInfo = oAuthInfo;
    }

    public int getBaseViewID() {
        return R.id.oauth_dialog;
    }

    @SuppressLint("SetJavaScriptEnabled")
    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_oauth);

        // Use these random strings to validate the OAuth response.
        String state = Alphanumeric.createRandomString(40);

        WebView webView = findViewById(R.id.oauth_dialog_webView);
        TextView loadingTextView = findViewById(R.id.oauth_dialog_loadingTextView);

        webView.getSettings().setJavaScriptEnabled(true); // JavaScript is needed by some websites to complete authorizations.
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                // When anything finishes loading, get rid of loading text.
                loadingTextView.setVisibility(View.GONE);

                // When authorization is complete, the url will be the redirect URL and will have the code and state as parameters.
                if(url.startsWith(oAuthInfo.redirect_uri)) {
                    HashMap<String, String> parameters = URLUtil.parseURL(url);
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

        String authURL = oAuthInfo.authURLBase +
            "?client_id=" + oAuthInfo.client_id +
            "&redirect_uri=" + oAuthInfo.redirect_uri +
            "&response_type=" + oAuthInfo.response_type +
            "&scope=" + TextUtils.join(",", oAuthInfo.scopes) +
            "&state=" + state +
            "&account=all"; // Needed for Coinbase, but ignored by other exchanges.
        webView.loadUrl(authURL);
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
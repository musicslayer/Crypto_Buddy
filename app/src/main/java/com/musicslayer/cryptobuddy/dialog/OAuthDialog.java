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

// Dialog that allows user to grant OAuth authorization.

// TODO Deal with XSS issues? Using the special mobile redirect URI does not support the "state" parameter.
// Also when should state be randomly created (does it get reset on recreation)?

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

        WebView webView = findViewById(R.id.oauth_dialog_webView);
        TextView loadingTextView = findViewById(R.id.oauth_dialog_loadingTextView);

        webView.getSettings().setJavaScriptEnabled(true); // JavaScript is needed by some websites to complete authorizations.
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                // When anything finishes loading, get rid of loading text.
                loadingTextView.setVisibility(View.GONE);

                // When authorization is complete, the url will have the code in it.
                // The code will always be between the last "/" and a "?".
                if(url.startsWith(oAuthInfo.authURLBase) && url.endsWith("?")) {
                    url = url.replace(oAuthInfo.authURLBase, "");
                    url = url.replace("?", "");

                    if(isValidCode(url)) {
                        user_CODE_E = Encryption.encrypt(url, BuildConfig.key_oauth_code);

                        isComplete = true;
                        dismiss();
                    }
                }
            }
        });

        String authURL = oAuthInfo.authURLBase +
            "?client_id=" + oAuthInfo.client_id +
            "&redirect_uri=" + oAuthInfo.redirect_uri +
            "&response_type=" + oAuthInfo.response_type +
            "&state=" + oAuthInfo.state +
            "&scope=" + TextUtils.join(",", oAuthInfo.scopes) +
            "&account=all"; // TODO Needed for Coinbase, but does every exchange use "account"?
        webView.loadUrl(authURL);
    }

    private boolean isValidCode(String code) {
        // Valid codes are alphanumeric and have at least one character.
        try {
            return code.length() > 0 && Alphanumeric.isAlphanumeric(code);
        }
        catch(Exception ignored) {
            return false;
        }
    }
}
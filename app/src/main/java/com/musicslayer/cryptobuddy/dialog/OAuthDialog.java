package com.musicslayer.cryptobuddy.dialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.musicslayer.cryptobuddy.R;

// Dialog that allows user to grant OAuth authorization.

public class OAuthDialog extends BaseDialog {
    public String user_CODE;

    String authURLBase; // TODO remove/calculate?
    String authURL;

    public OAuthDialog(Activity activity, String authURLBase, String authURL) {
        super(activity);
        this.authURLBase = authURLBase;
        this.authURL = authURL;
    }

    public int getBaseViewID() {
        return R.id.oauth_dialog;
    }

    // TODO Deal with XSS issues?
    @SuppressLint("SetJavaScriptEnabled")
    public void createLayout() {
        setContentView(R.layout.dialog_oauth);

        WebView webView = findViewById(R.id.oauth_dialog_web_view);
        webView.getSettings().setJavaScriptEnabled(true); // JavaScript is needed by some websites to complete authorizations.
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                // When authorization is complete, the url will have the code in it.
                if(url.startsWith(authURLBase) && url.endsWith("?")) {
                    user_CODE = url;
                    user_CODE = user_CODE.replace(authURLBase, "");
                    user_CODE = user_CODE.replace("?", "");

                    isComplete = true;
                    dismiss();
                }
            }
        });

        webView.loadUrl(authURL);
    }
}
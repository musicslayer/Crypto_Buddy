package com.musicslayer.cryptobuddy.dialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.view.fixed.FixedFrameLayout;

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
    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_oauth);

        TextView loadingTextView = findViewById(R.id.oauth_dialog_loadingTextView);

        // We use the frame and conditional initialization to make sure WebView doesn't reload the webpage if the dialog is recreated.
        // This also protects against mysterious crashes that had been observed when having the WebView defined in the XML file.
        FixedFrameLayout frame = findViewById(R.id.oauth_dialog_fixedFrameLayout);
        WebView webView = (WebView)frame.innerView;

        if(savedInstanceState == null) {
            webView.loadUrl(authURL);
        }
        else {
            webView.invalidate();
        }

        webView.getSettings().setJavaScriptEnabled(true); // JavaScript is needed by some websites to complete authorizations.
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                // When anything finishes loading, get rid of loading text.
                loadingTextView.setVisibility(View.GONE);

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
    }
}
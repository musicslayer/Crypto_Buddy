package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import androidx.appcompat.widget.Toolbar;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.crash.CrashView;

// Dialog that wraps a WebView for the user to interact with a web page to grant an exchange authorization.

public class ExchangeWebViewDialog extends BaseDialog {
    public String user_CODE;

    String titleString;
    String authURLBase;
    String authURL;

    public ExchangeWebViewDialog(Activity activity, String titleString, String authURLBase, String authURL) {
        super(activity);
        this.titleString = titleString;
        this.authURLBase = authURLBase;
        this.authURL = authURL;
    }

    public int getBaseViewID() {
        return R.id.exchange_web_view_dialog;
    }

    public void createLayout() {
        setContentView(R.layout.dialog_exchange_web_view);

        Toolbar toolbar = findViewById(R.id.exchange_web_view_dialog_toolbar);
        toolbar.setTitle(titleString);

        // TODO Give WebView focus to start with.
        WebView webView = findViewById(R.id.exchange_web_view_dialog_web_view);
        webView.getSettings().setJavaScriptEnabled(true); // Needed to complete authorizations.
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

        Button B_PROCEED = findViewById(R.id.exchange_web_view_dialog_proceedButton);
        B_PROCEED.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                B_PROCEED.setVisibility(View.GONE);
                webView.loadUrl(authURL);
            }
        });
    }
}
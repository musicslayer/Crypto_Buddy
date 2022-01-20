package com.musicslayer.cryptobuddy.util;

import android.content.Context;
import android.content.DialogInterface;

import com.musicslayer.cryptobuddy.BuildConfig;
import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.dialog.OAuthDialog;
import com.musicslayer.cryptobuddy.dialog.BaseDialogFragment;
import com.musicslayer.cryptobuddy.dialog.ProgressDialog;
import com.musicslayer.cryptobuddy.dialog.ProgressDialogFragment;
import com.musicslayer.cryptobuddy.encryption.Encryption;
import com.musicslayer.cryptobuddy.serialize.Serialization;

import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.Date;

// Handles common authentication cases.

public class AuthUtil {
    public static BaseDialogFragment fragment;

    public static byte[] code_e; // Only encrypted code should be stored.

    public static void authorizeOAuth(Context context) {
        fragment.show(context, "oauth");
    }

    public static void restoreListeners(Context context, OAuthInfo oAuthInfo, OAuthAuthorizationListener L) {
        ProgressDialogFragment progressDialogFragment = ProgressDialogFragment.newInstance(ProgressDialog.class);
        progressDialogFragment.setOnShowListener(new CrashDialogInterface.CrashOnShowListener(context) {
            @Override
            public void onShowImpl(DialogInterface dialog) {
                // Use the code to get access token that we can use to request user info.
                OAuthToken oAuthToken = null;

                String body = "{" +
                        "\"grant_type\": \"" + oAuthInfo.grant_type + "\"," +
                        "\"code\": \"" + Encryption.decrypt(code_e, BuildConfig.key_oauth_code) + "\"," +
                        "\"client_id\": \"" + oAuthInfo.client_id + "\"," +
                        "\"client_secret\": \"" + oAuthInfo.client_secret + "\"," +
                        "\"redirect_uri\": \"" + oAuthInfo.redirect_uri + "\"" +
                        "}";

                String authResponse = RESTUtil.post(oAuthInfo.tokenURLBase, body);
                if(authResponse != null) {
                    try {
                        JSONObject authResponseJSON = new JSONObject(authResponse);
                        byte[] token_e = Encryption.encrypt(authResponseJSON.getString("access_token"), BuildConfig.key_oauth_token);

                        // These times are all in seconds.
                        BigDecimal created_at = new BigDecimal(authResponseJSON.getString("created_at"));
                        BigDecimal expires_in = new BigDecimal(authResponseJSON.getString("expires_in"));
                        BigDecimal expires_at = created_at.add(expires_in).multiply(new BigDecimal("1000"));

                        long expiryTime = expires_at.longValue();
                        oAuthToken = new OAuthToken(token_e, expiryTime);
                    }
                    catch(Exception ignored) {
                    }
                }

                ProgressDialogFragment.setValue(Serialization.serialize(oAuthToken));
            }
        });
        progressDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(context) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                OAuthToken oAuthToken = Serialization.deserialize(ProgressDialogFragment.getValue(), OAuthToken.class);

                if(oAuthToken != null && oAuthToken.isAuthorized()) {
                    ToastUtil.showToast(activity, "authorization_successful");
                    L.onAuthorization(oAuthToken);
                }
                else {
                    ToastUtil.showToast(activity, "authorization_failed");
                }
            }
        });
        progressDialogFragment.restoreListeners(context, "progress");

        // Use this dialog to launch a WebView that allows the user to authenticate via OAuth.
        BaseDialogFragment oauthDialogFragment = BaseDialogFragment.newInstance(OAuthDialog.class, oAuthInfo);
        oauthDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(context) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((OAuthDialog)dialog).isComplete) {
                    code_e = ((OAuthDialog)dialog).user_CODE_E;
                    progressDialogFragment.show(activity, "progress");
                }
            }
        });
        oauthDialogFragment.restoreListeners(context, "oauth");

        fragment = oauthDialogFragment;
    }

    public static class OAuthInfo {
        public String authURLBase;
        public String tokenURLBase;
        public String client_id;
        public String client_secret;
        public String redirect_uri;
        public String response_type;
        public String grant_type;
        public String[] scopes;
        public String state;

        public OAuthInfo(String authURLBase, String tokenURLBase, String client_id, String client_secret, String redirect_uri, String response_type, String grant_type, String[] scopes, String state) {
            this.authURLBase = authURLBase;
            this.tokenURLBase = tokenURLBase;
            this.client_id = client_id;
            this.client_secret = client_secret;
            this.redirect_uri = redirect_uri;
            this.response_type = response_type;
            this.grant_type = grant_type;
            this.scopes = scopes;
            this.state = state;
        }
    }

    public static class OAuthToken implements Serialization.SerializableToJSON {
        private final byte[] token_e; // Only encrypted token should be stored.
        private final long expiryTime;

        public String serializationVersion() { return "1"; }

        public String serializeToJSON() throws org.json.JSONException {
            return new Serialization.JSONObjectWithNull()
                    .put("token_e", Serialization.byte_serializeArray(token_e))
                    .put("expiryTime", Serialization.long_serialize(expiryTime))
                    .toStringOrNull();
        }

        public static OAuthToken deserializeFromJSON1(String s) throws org.json.JSONException {
            Serialization.JSONObjectWithNull o = new Serialization.JSONObjectWithNull(s);
            byte[] token_e = Serialization.byte_deserializeArray(o.getString("token_e"));
            long expiryTime = Serialization.long_deserialize(o.getString("expiryTime"));
            return new OAuthToken(token_e, expiryTime);
        }

        private OAuthToken(byte[] token_e, long expiryTime) {
            this.token_e = token_e;
            this.expiryTime = expiryTime;
        }

        public String getToken() {
            return Encryption.decrypt(token_e, BuildConfig.key_oauth_token);
        }

        public boolean isAuthorized() {
            // Tokens that are null or expired cannot be used to query data.
            return token_e != null && isTokenValid();
        }

        public boolean isTokenValid() {
            final long nowTime = new Date().getTime();
            return nowTime < expiryTime;
        }
    }

    // These listeners only fire on successful authorization. This includes cases where no authorization is required.
    abstract public static class AuthorizationListener {
        abstract public void onAuthorization();
    }

    abstract public static class OAuthAuthorizationListener {
        abstract public void onAuthorization(OAuthToken oAuthToken);
    }
}

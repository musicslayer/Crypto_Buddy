package com.musicslayer.cryptobuddy.util;

import android.content.Context;
import android.content.DialogInterface;

import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.dialog.OAuthDialog;
import com.musicslayer.cryptobuddy.dialog.BaseDialogFragment;
import com.musicslayer.cryptobuddy.dialog.ProgressDialog;
import com.musicslayer.cryptobuddy.dialog.ProgressDialogFragment;
import com.musicslayer.cryptobuddy.serialize.Serialization;
import com.musicslayer.cryptobuddy.state.StateObj;

import org.json.JSONObject;

import java.util.Date;

// Handles common authentication cases.

public class AuthUtil {
    public static BaseDialogFragment fragment;

    public static String code;

    public static void authorizeOAuth(Context context) {
        StateObj.view = null;
        fragment.show(context, "oauth");
    }

    public static void restoreListeners(Context context, String authURLBase, String authURL, String tokenURLBase, String client_id, String client_secret, long expiryTimeDuration, AuthUtil.OAuthAuthorizationListener L) {
        ProgressDialogFragment progressDialogFragment = ProgressDialogFragment.newInstance(ProgressDialog.class);
        progressDialogFragment.setOnShowListener(new CrashDialogInterface.CrashOnShowListener(context) {
            @Override
            public void onShowImpl(DialogInterface dialog) {
                // Use the code to get access token that we can use to request user info.
                String token = null;

                String body = "{" +
                        "\"grant_type\": \"authorization_code\"," +
                        "\"code\": \"" + code + "\"," +
                        "\"client_id\": \"" + client_id + "\"," +
                        "\"client_secret\": \"" + client_secret + "\"," +
                        "\"redirect_uri\": \"urn:ietf:wg:oauth:2.0:oob\"" +
                        "}";

                String authResponse = RESTUtil.post(tokenURLBase, body);
                if(authResponse != null) {
                    try {
                        JSONObject authResponseJSON = new JSONObject(authResponse);
                        token = authResponseJSON.getString("access_token");
                    }
                    catch(Exception ignored) {
                    }
                }

                // Calculate expiration date now and create the token object.
                long expiryTime = new Date().getTime() + expiryTimeDuration;
                OAuthToken oAuthToken = new OAuthToken(token, expiryTime);
                ProgressDialogFragment.setValue(Serialization.serialize(oAuthToken));
            }
        });
        progressDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(context) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                OAuthToken oAuthToken = Serialization.deserialize(ProgressDialogFragment.getValue(), OAuthToken.class);

                if(oAuthToken.isAuthorized()) {
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
        BaseDialogFragment oauthDialogFragment = BaseDialogFragment.newInstance(OAuthDialog.class, authURLBase, authURL);
        oauthDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(context) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((OAuthDialog)dialog).isComplete) {
                    code = ((OAuthDialog)dialog).user_CODE;
                    progressDialogFragment.show(activity, "progress");
                }
            }
        });
        oauthDialogFragment.restoreListeners(context, "oauth");

        fragment = oauthDialogFragment;
    }

    // TODO handle security of client_id and client_secret, and the token.
    /*
    public static class OAuthInfo {
        public String client_id;
        public String client_secret;
        public OAuthInfo(String client_id, String client_secret) {
            this.client_id = client_id;
            this.client_secret = client_secret;
        }
    }

     */

    // This class wraps the OAuth token so it can be treated securely.
    // TODO Implement security, encryption, etc.
    public static class OAuthToken implements Serialization.SerializableToJSON {
        private final String token;
        private final long expiryTime;

        public String serializationVersion() { return "1"; }

        public String serializeToJSON() throws org.json.JSONException {
            return new Serialization.JSONObjectWithNull()
                    .put("token", Serialization.string_serialize(token))
                    .put("expiryTime", Serialization.long_serialize(expiryTime))
                    .toStringOrNull();
        }

        public static OAuthToken deserializeFromJSON1(String s) throws org.json.JSONException {
            Serialization.JSONObjectWithNull o = new Serialization.JSONObjectWithNull(s);
            String token = Serialization.string_deserialize(o.getString("token"));
            long expiryTime = Serialization.long_deserialize(o.getString("expiryTime"));
            return new OAuthToken(token, expiryTime);
        }

        private OAuthToken(String token, long expiryTime) {
            this.token = token;
            this.expiryTime = expiryTime;
        }

        public String getToken() {
            return token;
        }

        public boolean isAuthorized() {
            // Tokens that are null or expired cannot be used to query data.
            return token != null && isTokenValid();
        }

        public boolean isTokenValid() {
            // To avoid unnecessary web requests, just check the expiration date ourselves.
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

package com.musicslayer.cryptobuddy.util;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Parcel;
import android.os.Parcelable;

import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.data.bridge.DataBridge;
import com.musicslayer.cryptobuddy.dialog.OAuthBrowserDialog;
import com.musicslayer.cryptobuddy.dialog.BaseDialogFragment;
import com.musicslayer.cryptobuddy.dialog.ProgressDialog;
import com.musicslayer.cryptobuddy.dialog.ProgressDialogFragment;

import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

// Handles common authentication cases.

public class AuthUtil {
    public static String code;

    public static void authorizeOAuthBrowser(Context context, OAuthInfo oAuthInfo, OAuthAuthorizationListener L) {
        restoreListenersBrowser(context, oAuthInfo, L).show(context, "oauth_browser");
    }

    public static BaseDialogFragment restoreListenersBrowser(Context context, OAuthInfo oAuthInfo, OAuthAuthorizationListener L) {
        ProgressDialogFragment progressDialogFragment = ProgressDialogFragment.newInstance(ProgressDialog.class);
        progressDialogFragment.setOnShowListener(new CrashDialogInterface.CrashOnShowListener(context) {
            @Override
            public void onShowImpl(DialogInterface dialog) {
                ProgressDialogFragment.updateProgressTitle("Confirming Authorization...");

                // Use the code to get access token that we can use to request user info.
                OAuthToken oAuthToken = null;

                String body = "grant_type=authorization_code&code=" + code +
                        "&client_id=" + oAuthInfo.client_id +
                        "&client_secret=" + oAuthInfo.client_secret +
                        "&redirect_uri=" + oAuthInfo.redirect_uri;

                String authResponse = WebUtil.postCurl(oAuthInfo.tokenURLBase, body);
                if(authResponse != null) {
                    try {
                        JSONObject authResponseJSON = new JSONObject(authResponse);
                        String token = authResponseJSON.getString("access_token");

                        // These times are all in seconds. Note that some APIs do not return them in the response.
                        BigDecimal created_at;
                        if(authResponseJSON.has("created_at")) {
                            // Convert to milliseconds
                            created_at = new BigDecimal(authResponseJSON.getString("created_at")).multiply(new BigDecimal("1000"));
                        }
                        else {
                            // Just use the current time.
                            created_at = new BigDecimal(new Date().getTime());
                        }

                        BigDecimal expires_in;
                        if(authResponseJSON.has("expires_in")) {
                            // Convert to milliseconds
                            expires_in = new BigDecimal(authResponseJSON.getString("expires_in")).multiply(new BigDecimal("1000"));
                        }
                        else {
                            // Just use 1 hour.
                            expires_in = new BigDecimal(3600000);
                        }

                        BigDecimal expires_at = created_at.add(expires_in);

                        long expiryTime = expires_at.longValue();
                        oAuthToken = new OAuthToken(token, expiryTime);
                    }
                    catch(Exception ignored) {
                    }
                }

                ProgressDialogFragment.setValue(DataBridge.serialize(oAuthToken, OAuthToken.class));
            }
        });
        progressDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(context) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                OAuthToken oAuthToken = DataBridge.deserialize(ProgressDialogFragment.getValue(), OAuthToken.class);

                if(oAuthToken != null && oAuthToken.isAuthorized()) {
                    ToastUtil.showToast("authorization_successful");
                    L.onAuthorization(oAuthToken);
                }
                else {
                    ToastUtil.showToast("authorization_failed");
                }
            }
        });
        progressDialogFragment.restoreListeners(context, "progress");

        // Use this dialog to ask the user to authenticate via OAuth.
        BaseDialogFragment oauthBrowserDialogFragment = BaseDialogFragment.newInstance(OAuthBrowserDialog.class, oAuthInfo);

        oauthBrowserDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(context) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((OAuthBrowserDialog)dialog).isComplete) {
                    code = ((OAuthBrowserDialog)dialog).user_CODE;
                    progressDialogFragment.show(activity, "progress");
                }
            }
        });
        oauthBrowserDialogFragment.restoreListeners(context, "oauth_browser");

        return oauthBrowserDialogFragment;
    }

    public static class OAuthInfo implements Parcelable {
        public String authURLBase;
        public String tokenURLBase;
        public String client_id;
        public String client_secret;
        public String redirect_uri;
        public String response_type;
        public String grant_type;
        public String[] scopes;

        @Override
        public void writeToParcel(Parcel out, int flags) {
            out.writeString(authURLBase);
            out.writeString(tokenURLBase);
            out.writeString(client_id);
            out.writeString(client_secret);
            out.writeString(redirect_uri);
            out.writeString(response_type);
            out.writeString(grant_type);
            out.writeInt(scopes.length);
            out.writeStringArray(scopes);
        }

        public static final Parcelable.Creator<OAuthInfo> CREATOR = new Parcelable.Creator<OAuthInfo>() {
            @Override
            public OAuthInfo createFromParcel(Parcel in) {
                String authURLBase = in.readString();
                String tokenURLBase = in.readString();
                String client_id = in.readString();
                String client_secret = in.readString();
                String redirect_uri = in.readString();
                String response_type = in.readString();
                String grant_type = in.readString();

                int scopes_length = in.readInt();
                String[] scopes = new String[scopes_length];
                in.readStringArray(scopes);

                return new AuthUtil.OAuthInfo(authURLBase, tokenURLBase, client_id, client_secret, redirect_uri, response_type, grant_type, scopes);
            }

            @Override
            public OAuthInfo[] newArray(int size) {
                return new OAuthInfo[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        public OAuthInfo(String authURLBase, String tokenURLBase, String client_id, String client_secret, String redirect_uri, String response_type, String grant_type, String[] scopes) {
            this.authURLBase = authURLBase;
            this.tokenURLBase = tokenURLBase;
            this.client_id = client_id;
            this.client_secret = client_secret;
            this.redirect_uri = redirect_uri;
            this.response_type = response_type;
            this.grant_type = grant_type;
            this.scopes = scopes;
        }
    }

    public static class OAuthToken implements DataBridge.SerializableToJSON {
        private String token;
        private final long expiryTime;

        @Override
        public void serializeToJSON(DataBridge.Writer o) throws IOException {
            o.beginObject()
                    .serialize("token", token, String.class)
                    .serialize("expiryTime", expiryTime, Long.class)
                    .endObject();
        }

        public static OAuthToken deserializeFromJSON(DataBridge.Reader o) throws IOException {
            o.beginObject();
            String token = o.deserialize("token", String.class);
            long expiryTime = o.deserialize("expiryTime", Long.class);
            o.endObject();

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
            // Tokens that are null, empty, or expired cannot be used to query data.
            return token != null && !token.isEmpty() && isTokenValid();
        }

        public boolean isTokenValid() {
            final long nowTime = new Date().getTime();
            return nowTime < expiryTime;
        }

        public String getSafeInfo() {
            // Return info that does not include anything that could be insecure.
            return "Token Length = " + (getToken() == null ? "[Null]" : getToken().length()) + "\n" +
                    "Expiry Time = " + expiryTime + "\n" +
                    "Is Authorized = " + isAuthorized() + "\n" +
                    "Is Token Valid = " + isTokenValid() + "\n";
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

package com.musicslayer.cryptobuddy.util;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Parcel;
import android.os.Parcelable;

import com.musicslayer.cryptobuddy.BuildConfig;
import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.dialog.OAuthBrowserDialog;
import com.musicslayer.cryptobuddy.dialog.BaseDialogFragment;
import com.musicslayer.cryptobuddy.dialog.ProgressDialog;
import com.musicslayer.cryptobuddy.dialog.ProgressDialogFragment;
import com.musicslayer.cryptobuddy.encryption.Encryption;
import com.musicslayer.cryptobuddy.json.JSONWithNull;
import com.musicslayer.cryptobuddy.data.Serialization;

import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.Date;

// Handles common authentication cases.

public class AuthUtil {
    public static byte[] code_e; // Only encrypted code should be stored.

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

                String body = "{" +
                        "\"grant_type\": \"" + oAuthInfo.grant_type + "\"," +
                        "\"code\": \"" + Encryption.decrypt(code_e, BuildConfig.key_oauth_code) + "\"," +
                        "\"client_id\": \"" + oAuthInfo.client_id + "\"," +
                        "\"client_secret\": \"" + oAuthInfo.client_secret + "\"," +
                        "\"redirect_uri\": \"" + oAuthInfo.redirect_uri + "\"" +
                        "}";

                String authResponse = WebUtil.post(oAuthInfo.tokenURLBase, body);
                if(authResponse != null) {
                    try {
                        JSONObject authResponseJSON = new JSONObject(authResponse);
                        byte[] token_e = Encryption.encrypt(authResponseJSON.getString("access_token"), BuildConfig.key_oauth_token);

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
                        oAuthToken = new OAuthToken(token_e, expiryTime);
                    }
                    catch(Exception ignored) {
                    }
                }

                ProgressDialogFragment.setValue(Serialization.serialize(oAuthToken, OAuthToken.class));
            }
        });
        progressDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(context) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                OAuthToken oAuthToken = Serialization.deserialize(ProgressDialogFragment.getValue(), OAuthToken.class);

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
                    code_e = ((OAuthBrowserDialog)dialog).user_CODE_E;
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

    public static class OAuthToken implements Serialization.SerializableToJSON {
        private final byte[] token_e; // Only encrypted token should be stored.
        private final long expiryTime;

        public static String serializationType() {
            return "!OBJECT!";
        }

        public String serializeToJSON() throws org.json.JSONException {
            return new JSONWithNull.JSONObjectWithNull()
                    .putArray("token_e", toObjectArray(token_e), Byte.class)
                    .put("expiryTime", expiryTime, Long.class)
                    .toStringOrNull();
        }

        public static OAuthToken deserializeFromJSON(String s, String version) throws org.json.JSONException {
            JSONWithNull.JSONObjectWithNull o = new JSONWithNull.JSONObjectWithNull(s);
            byte[] token_e = toPrimitiveArray(o.getArray("token_e", Byte.class));
            long expiryTime = o.get("expiryTime", Long.class);
            return new OAuthToken(token_e, expiryTime);
        }

        private static Byte[] toObjectArray(byte[] primtiveArray) {
            Byte[] objectArray = new Byte[primtiveArray.length];
            int i = 0;
            for(byte b : primtiveArray) {
                objectArray[i++] = b;
            }
            return objectArray;
        }

        private static byte[] toPrimitiveArray(Byte[] objectArray) {
            byte[] primtiveArray = new byte[objectArray.length];
            int i = 0;
            for(Byte b : objectArray) {
                primtiveArray[i++] = b;
            }
            return primtiveArray;
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

        public String getSafeInfo() {
            // Return info that does not include anything that could be insecure.
            return "Token Length = " + (getToken() == null ? "[Null]" : getToken().length()) + "\n" +
                    "Encrypted Token Length = " + (token_e == null ? "[Null]" : token_e.length) + "\n" +
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

package com.musicslayer.cryptobuddy.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.musicslayer.cryptobuddy.crash.CrashRunnable;
import com.musicslayer.cryptobuddy.persistence.Settings;

import java.util.HashMap;

public class ToastUtil {
    final static HashMap<String, Toast> toastMap = new HashMap<>();

    @SuppressLint({"ShowToast"})
    public static void loadAllToasts(Context context) {
        toastMap.put("setting_message_test_short", Toast.makeText(context, "Short Test Message.", getToastDuration()));
        toastMap.put("setting_message_test_long", Toast.makeText(context, "Long Test Message.", getToastDuration()));

        toastMap.put("review", Toast.makeText(context, "Could not open Google Play app or website.", getToastDuration()));
        toastMap.put("email", Toast.makeText(context, "Your device does not have an email application.", getToastDuration()));
        toastMap.put("sms", Toast.makeText(context, "Your device does not have a text messaging application.", getToastDuration()));
        toastMap.put("copy", Toast.makeText(context, "Wallet address copied to clipboard.", getToastDuration()));
        toastMap.put("paste", Toast.makeText(context, "Wallet address pasted from clipboard.", getToastDuration()));
        toastMap.put("no_paste", Toast.makeText(context, "Cannot paste. Clipboard is empty or does not contain text.", getToastDuration()));

        toastMap.put("empty_address", Toast.makeText(context, "A crypto address cannot be empty.", getToastDuration()));
        toastMap.put("unrecognized_address", Toast.makeText(context, "This address is not recognized as a valid crypto address.", getToastDuration()));
        toastMap.put("no_address_data", Toast.makeText(context, "Could not access all address information. Check your internet connection and try again.", getToastDuration()));
        toastMap.put("no_price_data", Toast.makeText(context, "Could not access all price information. Check your internet connection and try again.", getToastDuration()));

        toastMap.put("multiple_qr_codes_read", Toast.makeText(context, "Multiple QR Codes Read. Please isolate a single QR code.", getToastDuration()));
        toastMap.put("no_camera_permission", Toast.makeText(context, "Camera permission is not granted.", getToastDuration()));
        toastMap.put("no_internet_permission", Toast.makeText(context, "Internet permission is not granted.", getToastDuration()));
        toastMap.put("google_play_missing", Toast.makeText(context, "Google Play services is missing.", getToastDuration()));
        toastMap.put("google_play_updating", Toast.makeText(context, "Google Play services is currently updating.", getToastDuration()));
        toastMap.put("google_play_needs_update", Toast.makeText(context, "Google Play services needs to be updated.", getToastDuration()));
        toastMap.put("google_play_disabled", Toast.makeText(context, "Google Play services is disabled.", getToastDuration()));
        toastMap.put("google_play_invalid", Toast.makeText(context, "Google Play services is invalid.", getToastDuration()));
        toastMap.put("unknown_google_play_error", Toast.makeText(context, "Unknown error accessing Google Play services.", getToastDuration()));

        toastMap.put("new_transaction_filtered", Toast.makeText(context, "Added transaction(s) are being filtered.", getToastDuration()));
        toastMap.put("new_address_filtered", Toast.makeText(context, "Added address is being filtered.", getToastDuration()));

        toastMap.put("portfolio_name_used", Toast.makeText(context, "A portfolio with this name already exists.", getToastDuration()));
        toastMap.put("address_in_portfolio", Toast.makeText(context, "This address is already in the portfolio.", getToastDuration()));

        toastMap.put("refresh", Toast.makeText(context, "Data has been refreshed.", getToastDuration()));

        toastMap.put("assets_same", Toast.makeText(context, "Assets must be different.", getToastDuration()));
        toastMap.put("cryptos_same", Toast.makeText(context, "Cryptos must be different.", getToastDuration()));

        toastMap.put("reset_settings", Toast.makeText(context, "Settings have been reset to default values.", getToastDuration()));
        toastMap.put("reset_stored_addresses", Toast.makeText(context, "Stored address history has been deleted.", getToastDuration()));
        toastMap.put("reset_portfolios", Toast.makeText(context, "Portfolios have been deleted.", getToastDuration()));
        toastMap.put("reset_downloaded_tokens", Toast.makeText(context, "Downloaded tokens have been deleted.", getToastDuration()));
        toastMap.put("reset_found_tokens", Toast.makeText(context, "Found tokens have been deleted.", getToastDuration()));
        toastMap.put("reset_custom_tokens", Toast.makeText(context, "Custom tokens have been deleted.", getToastDuration()));
        toastMap.put("reset_everything", Toast.makeText(context, "All stored app data has been reset.", getToastDuration()));

        toastMap.put("no_tokens", Toast.makeText(context, "There are currently no tokens in the app's database.", getToastDuration()));

        toastMap.put("tokens_not_downloaded", Toast.makeText(context, "Could not download all tokens.", getToastDuration()));
        toastMap.put("custom_token_added", Toast.makeText(context, "Custom token added.", getToastDuration()));
        toastMap.put("custom_token_exists", Toast.makeText(context, "Custom token has already been added or conflicts with a previously found token.", getToastDuration()));
        toastMap.put("tokens_deleted", Toast.makeText(context, "Tokens Deleted.", getToastDuration()));

        toastMap.put("restoring_purchases", Toast.makeText(context, "Restoring purchases. Please wait a few seconds.", getToastDuration()));
        toastMap.put("restoring_purchases_complete", Toast.makeText(context, "Purchases restored. You may need to restart the app.", getToastDuration()));

        toastMap.put("billing_connection_retry", Toast.makeText(context, "Reconnecting to Google Play Billing Service. Try again in a few seconds.", getToastDuration()));
        toastMap.put("billing_problem", Toast.makeText(context, "Cannot connect to Google Play Billing Service.", getToastDuration()));
        toastMap.put("query_problem", Toast.makeText(context, "Cannot query purchases. Try again in a few seconds.", getToastDuration()));
        toastMap.put("acknowledge_problem", Toast.makeText(context, "In-app purchase failed to confirm. Try restarting app.", getToastDuration()));

        toastMap.put("remove_ads_purchase", Toast.makeText(context, "Thank you for purchasing \"Remove Ads\"!", getToastDuration()));
        toastMap.put("unlock_tokens_purchase", Toast.makeText(context, "Thank you for purchasing \"Unlock Tokens\"!", getToastDuration()));
        toastMap.put("support_developers_purchase", Toast.makeText(context, "Thank you for your support!", getToastDuration()));

        toastMap.put("unlock_tokens_required", Toast.makeText(context, "In-app purchase required to unlock tokens.", getToastDuration()));

        // Internal Only!
        toastMap.put("lock_purchases", Toast.makeText(context, "Purchases locked.", getToastDuration()));
        toastMap.put("unlock_purchases", Toast.makeText(context, "Purchases unlocked.", getToastDuration()));
        toastMap.put("refund_purchases", Toast.makeText(context, "Refunding purchases. Please wait a few seconds.", getToastDuration()));
        toastMap.put("refund_purchase_complete", Toast.makeText(context, "Purchases refunded.", getToastDuration()));
    }

    private static int getToastDuration() {
        // Do this to avoid a Lint warning
        if(Settings.setting_message == Toast.LENGTH_SHORT) {
            return Toast.LENGTH_SHORT;
        }
        else {
            return Toast.LENGTH_LONG;
        }
    }

    public static void showToast(Context context, String key) {
        Toast toast = toastMap.get(key);

        if(toast != null) {
            // Toasts must always be shown on the UI Thread.
            // Use Looper so that we do not need access to the activity.
            new Handler(Looper.getMainLooper()).post(new CrashRunnable(context) {
                @Override
                public void runImpl() {
                    // At some point, Android changed toast behavior. getView being null is the only way to tell which behavior we will see.
                    if(toast.getView() == null) {
                        // New behavior - We cannot check if the toast is showing, but it is always OK to cancel and (re)show the toast.
                        toast.cancel();
                        toast.show();
                    }
                    else if(!toast.getView().isShown()) {
                        // Old behavior - We cannot cancel, so show the toast only if it isn't already showing.
                        toast.show();
                    }
                }
           });
        }
    }
}

package com.musicslayer.cryptobuddy.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.musicslayer.cryptobuddy.crash.CrashRunnable;
import com.musicslayer.cryptobuddy.settings.setting.MessageLengthSetting;

import java.util.HashMap;

public class ToastUtil {
    final static HashMap<String, Toast> toastMap = new HashMap<>();

    @SuppressLint({"ShowToast"})
    public static void loadAllToasts(Context context) {
        // Custom Assets
        toastMap.put("custom_fiat_added", Toast.makeText(context, "Custom fiat added.", getToastDuration()));
        toastMap.put("custom_coin_added", Toast.makeText(context, "Custom coin added.", getToastDuration()));
        toastMap.put("custom_token_added", Toast.makeText(context, "Custom token added.", getToastDuration()));
        toastMap.put("no_tokens", Toast.makeText(context, "There are currently no tokens in the app's database.", getToastDuration()));
        toastMap.put("tokens_not_downloaded", Toast.makeText(context, "Could not download all tokens.", getToastDuration()));

        // Address
        toastMap.put("address_data_downloaded", Toast.makeText(context, "Finished downloading address information.", getToastDuration()));
        toastMap.put("empty_address", Toast.makeText(context, "A crypto address cannot be empty.", getToastDuration()));
        toastMap.put("incomplete_address_data", Toast.makeText(context, "Could not access all address information. Check your internet connection and try again.", getToastDuration()));
        toastMap.put("unrecognized_address", Toast.makeText(context, "This address is not recognized as a valid crypto address.", getToastDuration()));

        // Exchange
        toastMap.put("authorization_failed", Toast.makeText(context, "Authorization did not complete successfully. Check your internet connection and try again.", getToastDuration()));
        toastMap.put("authorization_successful", Toast.makeText(context, "Authorization completed successfully.", getToastDuration()));
        toastMap.put("exchange_data_downloaded", Toast.makeText(context, "Finished downloading exchange information.", getToastDuration()));
        toastMap.put("incomplete_exchange_data", Toast.makeText(context, "Could not access all exchange information. Ensure exchanges are authorized, check your internet connection, and try again.", getToastDuration()));

        // Price
        toastMap.put("incomplete_price_data", Toast.makeText(context, "Could not access all price information. Some assets may not be supported, or check your internet connection and try again.", getToastDuration()));

        // Explorers
        toastMap.put("assets_same", Toast.makeText(context, "Assets must be different.", getToastDuration()));
        toastMap.put("new_transaction_filtered", Toast.makeText(context, "Added transaction(s) are being filtered.", getToastDuration()));
        toastMap.put("no_balances_found", Toast.makeText(context, "There are no balances.", getToastDuration()));
        toastMap.put("no_discrepancies_found", Toast.makeText(context, "There are no discrepancies.", getToastDuration()));
        toastMap.put("no_transactions_found", Toast.makeText(context, "There are no transactions.", getToastDuration()));

        // Portfolios
        toastMap.put("address_in_portfolio", Toast.makeText(context, "This address is already in the portfolio.", getToastDuration()));
        toastMap.put("exchange_in_portfolio", Toast.makeText(context, "This exchange is already in the portfolio.", getToastDuration()));
        toastMap.put("portfolio_name_used", Toast.makeText(context, "A portfolio with this name already exists.", getToastDuration()));

        // Email
        toastMap.put("cannot_attach", Toast.makeText(context, "Could not attach all files to the email.", getToastDuration()));

        // QR Codes
        toastMap.put("multiple_qr_codes_read", Toast.makeText(context, "Multiple QR Codes Read. Please isolate a single QR code.", getToastDuration()));

        // Clipboard
        toastMap.put("copy", Toast.makeText(context, "Text copied to clipboard.", getToastDuration()));
        toastMap.put("no_paste", Toast.makeText(context, "Cannot paste. Clipboard is empty or does not contain text.", getToastDuration()));
        toastMap.put("paste", Toast.makeText(context, "Text pasted from clipboard.", getToastDuration()));

        // Other Activities
        toastMap.put("email", Toast.makeText(context, "Your device does not have an email application.", getToastDuration()));
        toastMap.put("review", Toast.makeText(context, "Could not open Google Play app or website.", getToastDuration()));
        toastMap.put("sms", Toast.makeText(context, "Your device does not have a text messaging application.", getToastDuration()));
        toastMap.put("web_browser", Toast.makeText(context, "Your device does not have a web browser application.", getToastDuration()));

        // Permission
        toastMap.put("no_camera_permission", Toast.makeText(context, "Camera permission is not granted.", getToastDuration()));

        // Google Play
        toastMap.put("google_play_missing", Toast.makeText(context, "Google Play services is missing.", getToastDuration()));
        toastMap.put("google_play_updating", Toast.makeText(context, "Google Play services is currently updating.", getToastDuration()));
        toastMap.put("google_play_needs_update", Toast.makeText(context, "Google Play services needs to be updated.", getToastDuration()));
        toastMap.put("google_play_disabled", Toast.makeText(context, "Google Play services is disabled.", getToastDuration()));
        toastMap.put("google_play_invalid", Toast.makeText(context, "Google Play services is invalid.", getToastDuration()));
        toastMap.put("unknown_google_play_error", Toast.makeText(context, "Unknown error accessing Google Play services.", getToastDuration()));

        // Message Length Setting
        toastMap.put("setting_message_test_short", Toast.makeText(context, "Short Test Message.", getToastDuration()));
        toastMap.put("setting_message_test_long", Toast.makeText(context, "Long Test Message.", getToastDuration()));

        // Reset Data Setting and Deleting
        toastMap.put("nothing_to_delete", Toast.makeText(context, "There is nothing to delete.", getToastDuration()));
        toastMap.put("nothing_to_download", Toast.makeText(context, "There is nothing to download.", getToastDuration()));
        toastMap.put("nothing_to_remove", Toast.makeText(context, "There is nothing to remove.", getToastDuration()));
        toastMap.put("reset_settings", Toast.makeText(context, "Settings have been reset to default values.", getToastDuration()));
        toastMap.put("reset_address_history", Toast.makeText(context, "Address history has been deleted.", getToastDuration()));
        toastMap.put("reset_transaction_portfolios", Toast.makeText(context, "Transaction portfolios have been deleted.", getToastDuration()));
        toastMap.put("reset_address_portfolios", Toast.makeText(context, "Address portfolios have been deleted.", getToastDuration()));
        toastMap.put("reset_exchange_portfolios", Toast.makeText(context, "Exchange portfolios have been deleted.", getToastDuration()));
        toastMap.put("reset_fiats", Toast.makeText(context, "Fiats have been deleted.", getToastDuration()));
        toastMap.put("reset_coins", Toast.makeText(context, "Coins have been deleted.", getToastDuration()));
        toastMap.put("reset_tokens", Toast.makeText(context, "Tokens have been deleted.", getToastDuration()));
        toastMap.put("reset_everything", Toast.makeText(context, "All stored app data has been reset.", getToastDuration()));
        toastMap.put("reset_everything_fail", Toast.makeText(context, "Could not reset all stored app data.", getToastDuration()));

        // Purchases - Key must follow naming convention "<SKU>_purchase"
        toastMap.put("premium_purchase", Toast.makeText(context, "Thank you for purchasing \"Unlock Premium Features\"!", getToastDuration()));
        toastMap.put("remove_ads_purchase", Toast.makeText(context, "Thank you for purchasing \"Remove Ads\"!", getToastDuration()));
        toastMap.put("support_developers_purchase", Toast.makeText(context, "Thank you for your support!", getToastDuration()));

        // Purchase Required
        toastMap.put("unlock_exchange_integration_required", Toast.makeText(context, "In-app purchase required to unlock exchange integration.", getToastDuration()));
        toastMap.put("unlock_tokens_required", Toast.makeText(context, "In-app purchase required to unlock tokens.", getToastDuration()));

        // Google Play Billing Service
        toastMap.put("acknowledge_problem", Toast.makeText(context, "In-app purchase failed to confirm. Try restarting app.", getToastDuration()));
        toastMap.put("billing_connection_not_finished", Toast.makeText(context, "Connecting to Google Play Billing Service. Try again in a few seconds.", getToastDuration()));
        toastMap.put("billing_problem", Toast.makeText(context, "Cannot connect to Google Play Billing Service.", getToastDuration()));
        toastMap.put("restoring_purchases", Toast.makeText(context, "Restoring purchases. Please wait a few seconds.", getToastDuration()));

        // Internal Only
        toastMap.put("lock_purchases", Toast.makeText(context, "Purchases locked.", getToastDuration()));
        toastMap.put("refund_purchases", Toast.makeText(context, "Refunding purchases. Please wait a few seconds.", getToastDuration()));
        toastMap.put("refund_purchases_complete", Toast.makeText(context, "Purchases refunded.", getToastDuration()));
        toastMap.put("unlock_purchases", Toast.makeText(context, "Purchases unlocked.", getToastDuration()));
    }

    private static int getToastDuration() {
        // Do this to avoid a Lint warning
        if(MessageLengthSetting.value.equals(Toast.LENGTH_SHORT)) {
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

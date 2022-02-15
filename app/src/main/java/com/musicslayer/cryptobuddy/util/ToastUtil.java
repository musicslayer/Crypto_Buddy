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
        // Use a dummy value for the duration. When the toast is shown, we will set it according to the setting.
        int duration = Toast.LENGTH_SHORT;

        // Assets
        toastMap.put("custom_fiat_added", Toast.makeText(context, "Custom fiat added.", duration));
        toastMap.put("custom_coin_added", Toast.makeText(context, "Custom coin added.", duration));
        toastMap.put("custom_token_added", Toast.makeText(context, "Custom token added.", duration));
        toastMap.put("tokens_not_downloaded", Toast.makeText(context, "Could not download all tokens.", duration));
        toastMap.put("no_search_assets", Toast.makeText(context, "No Assets to Search.", duration));

        // Address
        toastMap.put("address_data_downloaded", Toast.makeText(context, "Finished downloading address information.", duration));
        toastMap.put("empty_address", Toast.makeText(context, "An address cannot be empty.", duration));
        toastMap.put("incomplete_address_data", Toast.makeText(context, "Could not access all address information. Check your internet connection and try again.", duration));
        toastMap.put("incomplete_reflections_data", Toast.makeText(context, "Could not access all reflections information. Check your internet connection and try again.", duration));
        toastMap.put("unrecognized_address", Toast.makeText(context, "This address is not recognized as a valid crypto address.", duration));
        toastMap.put("must_choose_address", Toast.makeText(context, "An address needs to be chosen.", duration));
        toastMap.put("must_download_data", Toast.makeText(context, "Data needs to be downloaded.", duration));

        // Exchange
        toastMap.put("authorization_failed", Toast.makeText(context, "Authorization did not complete successfully. Check your internet connection and try again.", duration));
        toastMap.put("authorization_successful", Toast.makeText(context, "Authorization completed successfully.", duration));
        toastMap.put("exchange_data_downloaded", Toast.makeText(context, "Finished downloading exchange information.", duration));
        toastMap.put("incomplete_exchange_data", Toast.makeText(context, "Could not access all exchange information. Ensure exchanges are authorized, check your internet connection, and try again.", duration));

        // Price
        toastMap.put("incomplete_price_data", Toast.makeText(context, "Could not access all price information. Some assets may not be supported, or check your internet connection and try again.", duration));

        // Explorers
        toastMap.put("assets_same", Toast.makeText(context, "Assets must be different.", duration));
        toastMap.put("must_choose_assets", Toast.makeText(context, "All assets must be chosen.", duration));
        toastMap.put("must_fill_inputs", Toast.makeText(context, "All red input fields must be filled with appropriate values.", duration));
        toastMap.put("new_transaction_filtered", Toast.makeText(context, "Added transaction(s) are being filtered.", duration));
        toastMap.put("no_balances_found", Toast.makeText(context, "There are no balances.", duration));
        toastMap.put("no_discrepancies_found", Toast.makeText(context, "There are no discrepancies.", duration));
        toastMap.put("no_transactions_found", Toast.makeText(context, "There are no transactions.", duration));

        // Portfolios
        toastMap.put("address_in_portfolio", Toast.makeText(context, "This address is already in the portfolio.", duration));
        toastMap.put("exchange_in_portfolio", Toast.makeText(context, "This exchange is already in the portfolio.", duration));
        toastMap.put("portfolio_name_used", Toast.makeText(context, "A portfolio with this name already exists.", duration));

        // Email
        toastMap.put("cannot_attach", Toast.makeText(context, "Could not attach all files to the email.", duration));

        // QR Codes
        toastMap.put("multiple_qr_codes_read", Toast.makeText(context, "Multiple QR Codes Read. Please isolate a single QR code.", duration));

        // Clipboard
        toastMap.put("clipboard_empty", Toast.makeText(context, "Cannot paste. Clipboard is empty.", duration));
        toastMap.put("clipboard_not_text", Toast.makeText(context, "Cannot paste. Clipboard does not contain text.", duration));
        toastMap.put("clipboard_text_too_large", Toast.makeText(context, "Cannot copy. Text is too large to place on clipboard.", duration));
        toastMap.put("copy", Toast.makeText(context, "Text copied to clipboard.", duration));
        toastMap.put("paste", Toast.makeText(context, "Text pasted from clipboard.", duration));

        // Other Activities
        toastMap.put("email", Toast.makeText(context, "Your device does not have an email application.", duration));
        toastMap.put("review", Toast.makeText(context, "Could not open Google Play app or website.", duration));
        toastMap.put("sms", Toast.makeText(context, "Your device does not have a text messaging application.", duration));
        toastMap.put("web_browser", Toast.makeText(context, "Your device does not have a web browser application.", duration));

        // Permission
        toastMap.put("no_camera_permission", Toast.makeText(context, "Camera permission is not granted.", duration));
        toastMap.put("no_external_read_permission", Toast.makeText(context, "External read permission is not granted.", duration));
        toastMap.put("no_external_write_permission", Toast.makeText(context, "External write permission is not granted.", duration));
        toastMap.put("no_external_read_write_permission", Toast.makeText(context, "External read and write permissions are not granted.", duration));

        // Google Play
        toastMap.put("google_play_missing", Toast.makeText(context, "Google Play services is missing.", duration));
        toastMap.put("google_play_updating", Toast.makeText(context, "Google Play services is currently updating.", duration));
        toastMap.put("google_play_needs_update", Toast.makeText(context, "Google Play services needs to be updated.", duration));
        toastMap.put("google_play_disabled", Toast.makeText(context, "Google Play services is disabled.", duration));
        toastMap.put("google_play_invalid", Toast.makeText(context, "Google Play services is invalid.", duration));
        toastMap.put("unknown_google_play_error", Toast.makeText(context, "Unknown error accessing Google Play services.", duration));

        // Message Length Setting
        toastMap.put("setting_message_test_short", Toast.makeText(context, "Short Test Message.", duration));
        toastMap.put("setting_message_test_long", Toast.makeText(context, "Long Test Message.", duration));

        // Reset Data Setting and Deleting
        toastMap.put("nothing_to_delete", Toast.makeText(context, "There is nothing to delete.", duration));
        toastMap.put("nothing_to_download", Toast.makeText(context, "There is nothing to download.", duration));
        toastMap.put("nothing_to_remove", Toast.makeText(context, "There is nothing to remove.", duration));
        toastMap.put("reset_settings", Toast.makeText(context, "Settings have been reset to default values.", duration));
        toastMap.put("reset_address_history", Toast.makeText(context, "Address history has been deleted.", duration));
        toastMap.put("reset_transaction_portfolios", Toast.makeText(context, "Transaction portfolios have been deleted.", duration));
        toastMap.put("reset_address_portfolios", Toast.makeText(context, "Address portfolios have been deleted.", duration));
        toastMap.put("reset_exchange_portfolios", Toast.makeText(context, "Exchange portfolios have been deleted.", duration));
        toastMap.put("reset_fiats", Toast.makeText(context, "Fiats have been deleted.", duration));
        toastMap.put("reset_coins", Toast.makeText(context, "Coins have been deleted.", duration));
        toastMap.put("reset_tokens", Toast.makeText(context, "Tokens have been deleted.", duration));
        toastMap.put("reset_everything", Toast.makeText(context, "All stored app data has been reset.", duration));
        toastMap.put("reset_everything_fail", Toast.makeText(context, "Could not reset all stored app data.", duration));

        // Purchases - Key must follow naming convention "<SKU>_purchase"
        toastMap.put("premium_purchase", Toast.makeText(context, "Thank you for purchasing \"Unlock Premium Features\"!", duration));
        toastMap.put("remove_ads_purchase", Toast.makeText(context, "Thank you for purchasing \"Remove Ads\"!", duration));
        toastMap.put("support_developers_purchase", Toast.makeText(context, "Thank you for your support!", duration));

        // Purchase Required
        toastMap.put("unlock_data_management_required", Toast.makeText(context, "In-app purchase required to unlock data management.", duration));
        toastMap.put("unlock_exchange_integration_required", Toast.makeText(context, "In-app purchase required to unlock exchange integration.", duration));
        toastMap.put("unlock_reflections_calculator_required", Toast.makeText(context, "In-app purchase required to unlock reflections calculator.", duration));
        toastMap.put("unlock_tokens_required", Toast.makeText(context, "In-app purchase required to unlock tokens.", duration));

        // Google Play Billing Service
        toastMap.put("acknowledge_problem", Toast.makeText(context, "In-app purchase failed to confirm. Try restarting app.", duration));
        toastMap.put("billing_connection_not_finished", Toast.makeText(context, "Connecting to Google Play Billing Service. Try again in a few seconds.", duration));
        toastMap.put("billing_problem", Toast.makeText(context, "Cannot connect to Google Play Billing Service.", duration));
        toastMap.put("restoring_purchases", Toast.makeText(context, "Restoring purchases. Please wait a few seconds.", duration));

        // Import and Export
        toastMap.put("export_file_failed", Toast.makeText(context, "Could not export to file.", duration));
        toastMap.put("export_file_success", Toast.makeText(context, "Export to file complete.", duration));
        toastMap.put("file_does_not_exist", Toast.makeText(context, "A file with this name does not exist.", duration));
        toastMap.put("import_clipboard_failed", Toast.makeText(context, "Could not import from clipboard. Text was not exported from this app.", duration));
        toastMap.put("import_clipboard_success", Toast.makeText(context, "Import from clipboard complete.", duration));
        toastMap.put("import_file_failed", Toast.makeText(context, "Could not import from file. File was not exported from this app.", duration));
        toastMap.put("import_file_success", Toast.makeText(context, "Import from file complete.", duration));

        // Internal Only
        toastMap.put("lock_purchases", Toast.makeText(context, "Purchases locked.", duration));
        toastMap.put("refund_purchases", Toast.makeText(context, "Refunding purchases. Please wait a few seconds.", duration));
        toastMap.put("refund_purchases_complete", Toast.makeText(context, "Purchases refunded.", duration));
        toastMap.put("unlock_purchases", Toast.makeText(context, "Purchases unlocked.", duration));
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
            toast.setDuration(getToastDuration());

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

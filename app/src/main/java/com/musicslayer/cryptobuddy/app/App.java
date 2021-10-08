package com.musicslayer.cryptobuddy.app;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.multidex.MultiDexApplication;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;

import org.apache.commons.io.FileUtils;

// The code in this class must be especially crash free because we cannot use CrashReporterDialog here, or even AlertDialog.
public class App extends MultiDexApplication {
    // This needs to be changed before creating a public release!
    public static boolean DEBUG = true;

    public static boolean isGooglePlayAvailable = true;
    public static boolean isAppInitialized = false;

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            ProviderInstaller.installIfNeeded(this);
        } catch (GooglePlayServicesRepairableException ignored) {
            isGooglePlayAvailable = false;
        } catch (GooglePlayServicesNotAvailableException ignored) {
            isGooglePlayAvailable = false;
        }

        try {
            // Needed for older Android versions.
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

            // Try to clear out previously created files from the cache.
            FileUtils.deleteQuietly(getApplicationContext().getCacheDir());
        }
        catch(Exception e) {
            // Try to proceed on in case the method is removed in a later Android version.
            ThrowableUtil.processThrowable(e);
        }
    }
}

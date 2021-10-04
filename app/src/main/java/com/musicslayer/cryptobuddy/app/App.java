package com.musicslayer.cryptobuddy.app;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.multidex.MultiDexApplication;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;
import com.musicslayer.cryptobuddy.util.ThrowableLogger;

// The code in this class must be especially crash free because we cannot use CrashDialog here.
public class App extends MultiDexApplication {
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
            // Needed for older Android versions
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        }
        catch(Exception e) {
            ThrowableLogger.processThrowable(e);
        }
    }
}

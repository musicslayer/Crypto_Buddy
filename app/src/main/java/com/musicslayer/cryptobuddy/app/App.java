package com.musicslayer.cryptobuddy.app;

import android.content.ContentResolver;
import android.os.Build;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.multidex.MultiDexApplication;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;

// The code in this class must be especially crash free because we cannot use CrashReporterDialog here, or even AlertDialog.
public class App extends MultiDexApplication {
    // This needs to be changed before creating a public release!
    public static boolean DEBUG = true;

    public static boolean isGooglePlayAvailable = true;
    public static boolean isAppInitialized = false;

    // Store this for use later when the context may not be available.
    public static String cacheDir;
    public static ArrayList<String> internalFilesDirs;
    public static ArrayList<String> externalFilesDirs;
    public static ContentResolver contentResolver;

    @Override
    public void onCreate() {
        super.onCreate();

        if(DEBUG) {
            try {
                Class.forName("dalvik.system.CloseGuard").getMethod("setEnabled", boolean.class).invoke(null, true);
            }
            catch(Exception e) {
                ThrowableUtil.processThrowable(e);
            }
        }

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

            cacheDir = this.getCacheDir().getAbsolutePath();

            // For now, there is only one of these folders.
            internalFilesDirs = new ArrayList<>();
            internalFilesDirs.add(this.getFilesDir().getAbsolutePath() + File.separatorChar);

            // Older APIs only support one external folder, but newer APIs may have more than one.
            externalFilesDirs = new ArrayList<>();
            if(Build.VERSION.SDK_INT >= 19) {
                for(File file : this.getExternalFilesDirs("documents")) {
                    externalFilesDirs.add(file.getAbsolutePath() + File.separatorChar);
                }
            }
            else {
                externalFilesDirs.add(this.getExternalFilesDir("documents").getAbsolutePath() + File.separatorChar);
            }

            contentResolver = this.getContentResolver();

            // Try to clear out previously created files from the cache.
            // Then access the cache again to make sure the folder is recreated.
            FileUtils.deleteQuietly(new File(cacheDir));
            this.getCacheDir();
        }
        catch(Exception e) {
            // Try to proceed on in case the method is removed in a later Android version.
            ThrowableUtil.processThrowable(e);
        }
    }
}

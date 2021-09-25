# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html
-keepattributes SourceFile,LineNumberTable

# Classes used by reflection
-keep public class * extends com.musicslayer.cryptobuddy.api.API
-keep public class * extends com.musicslayer.cryptobuddy.asset.Asset
-keep public class * extends com.musicslayer.cryptobuddy.asset.network.Network
-keep public class * extends com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager
-keep public class * extends com.musicslayer.cryptobuddy.dialog.BaseDialog
-keepclassmembers class * extends com.musicslayer.cryptobuddy.dialog.BaseDialog {
    public <init>(...);
}


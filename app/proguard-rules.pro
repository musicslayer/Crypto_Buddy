# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html
-keepattributes SourceFile,LineNumberTable

# Classes constructed by reflection
-keep public class * extends com.musicslayer.cryptobuddy.api.API
-keep public class * extends com.musicslayer.cryptobuddy.asset.Asset
-keep public class * extends com.musicslayer.cryptobuddy.asset.coinmanager.CoinManager
-keep public class * extends com.musicslayer.cryptobuddy.asset.exchange.Exchange
-keep public class * extends com.musicslayer.cryptobuddy.asset.fiatmanager.FiatManager
-keep public class * extends com.musicslayer.cryptobuddy.asset.network.Network
-keep public class * extends com.musicslayer.cryptobuddy.asset.tokenmanager.TokenManager
-keep public class * extends com.musicslayer.cryptobuddy.dialog.BaseDialog
-keep public class * extends com.musicslayer.cryptobuddy.data.persistent.app.PersistentAppDataStore
-keep public class * extends com.musicslayer.cryptobuddy.data.persistent.user.PersistentUserDataStore
-keep public class * extends com.musicslayer.cryptobuddy.settings.category.SettingsCategory
-keep public class * extends com.musicslayer.cryptobuddy.settings.setting.Setting

# Dialog Construction
-keepclassmembers class * extends com.musicslayer.cryptobuddy.dialog.BaseDialog {
    public <init>(...);
}

# Referentiation
-keepclassmembers class * {
    public static *** dereferenceFromJSON(...);
}

# Serialization
-keepclassmembers class * {
    public static *** deserializeFromJSON(...);
}

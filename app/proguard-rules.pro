# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Fortress Hypervisor specific rules

# Keep all classes in the hypervisor package
-keep class com.fortress.hypervisor.** { *; }

# Keep native methods for JNI
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep classes that implement Serializable
-keep class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Keep data class members
-keepclassmembers class kotlin.jvm.internal.Ref$ObjectRef {
    *** REF;
}

# Keep enum values and valueOf method
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep annotations
-keepattributes *Annotation*

# Keep source file and line number information for debugging
-keepattributes SourceFile,LineNumberTable

# Keep Kotlin metadata
-keep class kotlin.Metadata { *; }

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.HiltAndroidApp
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }

# Keep Compose runtime classes
-keep class androidx.compose.** { *; }

# Keep TensorFlow Lite classes
-keep class org.tensorflow.lite.** { *; }

# Keep WorkManager classes
-keep class androidx.work.** { *; }

# Security-related keep rules
-keep class java.security.** { *; }
-keep class javax.crypto.** { *; }
-keep class android.security.** { *; }

# VPN service keep rules
-keep class android.net.VpnService { *; }

# Accessibility service keep rules
-keep class android.accessibilityservice.AccessibilityService { *; }

# Device admin keep rules
-keep class android.app.admin.DeviceAdminReceiver { *; }

# Notification listener keep rules
-keep class android.service.notification.NotificationListenerService { *; }

# Obfuscation rules for security
-obfuscationdictionary proguard-dict.txt
-classobfuscationdictionary proguard-dict.txt
-packageobfuscationdictionary proguard-dict.txt

# Flatten package hierarchy
-flattenpackagehierarchy

# Repackage classes
-repackageclasses

# Remove logging calls
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# Keep custom exception messages
-keepattributes Exceptions

# Keep generic signatures for reflection
-keepattributes Signature

# Keep inner class information
-keepattributes InnerClasses

# Keep enclosing method information
-keepattributes EnclosingMethod
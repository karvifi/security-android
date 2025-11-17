# Firebase
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Kotlinx
-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }

# Okhttp
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# Retrofit
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }

# Gson
-keep class com.google.gson.** { *; }
-keep interface com.google.gson.** { *; }

# App classes
-keep class com.androidsecurity.** { *; }
-keep interface com.androidsecurity.** { *; }

# Remove logging
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}
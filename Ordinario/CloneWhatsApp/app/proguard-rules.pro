# Reglas de ProGuard/R8 para CloneWhatsApp

# Conservar modelos de datos POJO procesados por Gson
-keep class com.example.clonewhatsapp.User { *; }
-keep class com.example.clonewhatsapp.Conversation { *; }
-keep class com.example.clonewhatsapp.Message { *; }

# Gson
-keepattributes *Annotation*
-keepattributes Signature
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.google.gson.** { *; }

# OkHttp
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# Glide
-keep class com.bumptech.glide.** { *; }
-dontwarn com.bumptech.glide.**

# Firebase Messaging
-keep class com.google.firebase.messaging.** { *; }
-dontwarn com.google.firebase.messaging.**

# BuildConfig
-keep class com.example.clonewhatsapp.BuildConfig { *; }

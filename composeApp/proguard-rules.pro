# ------------------------------------------------------------------
# ProGuard / R8 Rules for FabSimple (Compose Multiplatform & Android)
# ------------------------------------------------------------------

# Preserve Line Numbers and Source File attributes for accurate stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Preserve annotations (e.g., @Serializable, @Composable)
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# ------------------------------------------------------------------
# Kotlinx Serialization Rules
# ------------------------------------------------------------------
-keepattributes *Annotation*,ElementValuePairs
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
    @kotlinx.serialization.Serializer <fields>;
}
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    public static *** serializer(...);
}
-keep class com.fabsimple.shared.domain.model.** { *; }

# ------------------------------------------------------------------
# Voyager Navigation Rules
# ------------------------------------------------------------------
-keep class * implements cafe.adriel.voyager.core.screen.Screen { *; }
-keep class com.fabsimple.app.presentation.screens.** { *; }

# ------------------------------------------------------------------
# Jetpack Compose & Material 3
# ------------------------------------------------------------------
-keep class androidx.compose.** { *; }

# ------------------------------------------------------------------
# MLKit & CameraX Rules
# ------------------------------------------------------------------
-keep class com.google.mlkit.** { *; }
-keep class androidx.camera.** { *; }

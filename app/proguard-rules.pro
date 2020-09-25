# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
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

-dontwarn java.lang.invoke.*
-dontwarn **$$Lambda$*

# RetroFit
-dontwarn retrofit.**
-keep class retrofit.** { *; }
-keepattributes Signature
-keepattributes Exceptions
-dontwarn javax.annotation.**

# Glide settings
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule {
 <init>(...);
}
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
-keep class com.bumptech.glide.load.data.ParcelFileDescriptorRewinder$InternalRewinder {
  *** rewind();
}

#UIL
-keep class com.nostra13.universalimageloader.** { *; }
-keepclassmembers class com.nostra13.universalimageloader.** {*;}

#JAudioTagger
-keep class org.jaudiotagger.** { *; }
-dontwarn org.jaudiotagger.**

#Navigation
-keepnames class androidx.navigation.fragment.NavHostFragment
-keep class * extends androidx.fragment.app.Fragment{}

#Models
-keepnames class * extends android.os.Parcelable
-keepnames class * extends java.io.Serializable
-keep class com.o4x.musical.model.** {*;}
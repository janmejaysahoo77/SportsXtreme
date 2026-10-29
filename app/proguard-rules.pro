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

# Firestore creates these Kotlin models through reflection when calling
# DocumentSnapshot.toObject(). Keep their constructors and properties in release
# builds so R8 does not remove or rename members needed by the mapper.
-keep class com.example.sportsxtreme.domain.model.Tournament { *; }
-keep class com.example.sportsxtreme.domain.model.TournamentRequirements { *; }

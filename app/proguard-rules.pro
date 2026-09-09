# Keep Hilt classes
-keep class dagger.hilt.** { *; }
-keep interface dagger.hilt.** { *; }

# Keep Room entities
-keep class androidx.room.** { *; }
-keep interface androidx.room.** { *; }

# Keep Kotlin metadata
-keep class kotlin.Metadata { *; }

# Keep annotations
-keepattributes *Annotation*

# Keep lifecycle components
-keep class androidx.lifecycle.** { *; }

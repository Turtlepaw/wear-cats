# Keep all classes in the application package
-keep class com.turtlepaw.cats.** { *; }

# Keep all classes in Android Wear Compose library
-keep class androidx.wear.compose.** { *; }

# Keep all classes in AndroidX Compose library
-keep class androidx.compose.** { *; }

# Keep utils
-keep class com.turtlepaw.cats.utils.** { *; }

# Keep services
-keep class com.turtlepaw.cats.services.CatDownloadWorker  { *; }
-keep class com.turtlepaw.cats.services.MyPetWorker  { *; }
-keep class androidx.health.services.client.** { *; }
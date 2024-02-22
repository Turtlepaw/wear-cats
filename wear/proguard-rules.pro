# Keep all classes in the application package
-keep class com.turtlepaw.sunlight.** { *; }

# Keep all classes in Android Wear Compose library
-keep class androidx.wear.compose.** { *; }

# Keep all classes in AndroidX Compose library
-keep class androidx.compose.** { *; }

# Keep broadcast receivers
-keep class com.turtlepaw.sunlight.services.** { *; }
-keep class com.turtlepaw.sunlight.services.SensorReceiver { *; }
-keep class com.turtlepaw.sunlight.services.TimeoutReceiver { *; }
# Light
-keep class com.turtlepaw.sunlight.services.LightLoggerService { *; }
-keep class com.turtlepaw.sunlight.services.LightWorker { *; }
# Keep utils
-keep class com.turtlepaw.sunlight.utils.** { *; }
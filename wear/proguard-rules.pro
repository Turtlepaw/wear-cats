# Debug
-printusage ./usage.txt
-printseeds ./seeds.txt

# Keep all classes in the application package
-keep class com.turtlepaw.sunlight.** { *; }

# Keep all classes in Android Wear Compose library
-keep class androidx.wear.compose.** { *; }

# Keep all classes in AndroidX Compose library
-keep class androidx.compose.** { *; }

# Keep broadcast receivers
-keep class com.turtlepaw.sunlight.services.** { *; }
-keep class com.turtlepaw.sunlight.services.Receiver { *; }
# Bedtime Mode
-keep class com.turtlepaw.sunlight.services.BedtimeModeReceiver { *; }
# Charging
-keep class com.turtlepaw.sunlight.services.ChargingReceiver { *; }

# Keep utils
-keep class com.turtlepaw.sunlight.utils.** { *; }
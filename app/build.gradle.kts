plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)

}

android {
    namespace = "com.example.meditrackapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.meditrackapp"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    implementation(libs.firebase.firestore)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)


    // 1. Fused Location Provider (for getLastLocation, FusedLocationProviderClient, etc.)
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // 2. Google Maps (to support the geo intent and any future map integration)
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    // END: ADD THESE LINES

    implementation("com.google.firebase:firebase-auth:24.0.1")
    implementation(platform("com.google.firebase:firebase-bom:34.2.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-firestore:24.11.1")

    implementation("androidx.cardview:cardview:1.0.0")


}
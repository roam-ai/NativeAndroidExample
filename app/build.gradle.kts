plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.roam.androidnativeselftracking"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.roam.androidnativeselftracking"
//        applicationId = "com.pawoints.curiouscat"
        minSdk = 21
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        debug {
            isMinifyEnabled = true
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )}
    }
    buildFeatures {
        viewBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

//    implementation ("com.roam.sdk:roam-android:0.1.40")
//    implementation ("com.roam.sdk:roam-batch-android:0.1.40")

//    implementation("com.roam.sdk:roam-android:0.1.41")
//    implementation("com.roam.sdk:roam-batch-android:0.1.41")

    implementation(files("libs/roam_0.1.41_v1.aar"))
    implementation(files("libs/roam-batch_0.1.41_v1.aar"))

    implementation ("com.auth0.android:jwtdecode:2.0.1")
    implementation ("com.google.android.gms:play-services-location:21.3.0")
    implementation ("com.squareup.retrofit2:retrofit:2.11.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation ("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.4")
    implementation ("org.eclipse.paho:org.eclipse.paho.android.service:1.1.1")
    implementation ("com.google.android.gms:play-services-ads-identifier:18.2.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
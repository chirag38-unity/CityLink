
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("com.google.firebase.firebase-perf")
    kotlin("kapt")
    id("com.google.dagger.hilt.android")
    id("androidx.navigation.safeargs.kotlin")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

android {
    signingConfigs {
        create("release") {
            storeFile = file("C:\\chirag\\APP-keystores\\citylink.jks")
            storePassword = "Chiru@2002"
            keyAlias = "releasekey"
            keyPassword = "Asekzz@38"
        }
    }
    namespace = "com.example.citylink"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.example.citylink"
        minSdk = 29
        targetSdk = 33
        versionCode = 1
        versionName = "1.0.1A"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    }

    buildTypes {
        release {

            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.annotation:annotation:1.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    //UI
    implementation("androidx.cardview:cardview:1.0.0")
    implementation ("com.firebaseui:firebase-ui-firestore:8.0.2")
    implementation ("com.github.Dimezis:BlurView:version-2.0.3")
    implementation ("com.ncorti:slidetoact:0.10.0")

    //SplashScreen & Animation
    implementation ("com.airbnb.android:lottie:6.1.0")

    // Coroutines
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Coroutine Lifecycle Scopes & Architectural Components
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
//    implementation ("androidx.lifecycle:lifecycle-extensions-ktx:2.2.0")
    kapt ("androidx.lifecycle:lifecycle-compiler:2.6.2")

    // Navigation Components
    implementation ("androidx.navigation:navigation-fragment-ktx:2.6.0")
    implementation ("androidx.navigation:navigation-ui-ktx:2.6.0")

    //Google Admob
    implementation ("com.google.android.gms:play-services-ads:22.4.0")

    // Google Maps Location Services
    implementation ("com.google.android.gms:play-services-location:21.0.1")
    implementation ("com.google.android.gms:play-services-maps:18.1.0")

    //Timber
    implementation ("com.jakewharton.timber:timber:5.0.1")

    //Services
    implementation ("android.arch.lifecycle:extensions:1.1.1")

    //Circle Image View
    implementation ("de.hdodenhof:circleimageview:3.1.0")
    implementation ("com.github.bumptech.glide:glide:4.16.0")

    //Firebase Auth
    implementation(platform("com.google.firebase:firebase-bom:32.2.2"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.android.gms:play-services-auth:20.7.0")

    //Firebase Firestore
    implementation("com.google.firebase:firebase-firestore-ktx")

    //Firebase Messaging
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation ("com.google.firebase:firebase-messaging-directboot:23.2.1")

    //Firebase Performance Monitoring---------------------------------------------------------------
    implementation("com.google.firebase:firebase-perf-ktx")

    //Retrofit
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.retrofit2:converter-scalars:2.9.0")

    //Socket.IO
    implementation ("io.socket:socket.io-client:2.0.0") {
        exclude( "org.json", "json" )
    }
    implementation ("com.google.code.gson:gson:2.9.0")
    implementation ("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.0")

    // Dagger Core
    implementation ("com.google.dagger:dagger:2.47")
    kapt("com.google.dagger:dagger-compiler:2.47")

    // Dagger Android
    api ("com.google.dagger:dagger-android:2.47")
    api ("com.google.dagger:dagger-android-support:2.47")
    kapt("com.google.dagger:dagger-android-processor:2.47")

    //Dagger-Hilt
    implementation("com.google.dagger:hilt-android:2.47")
    kapt("com.google.dagger:hilt-android-compiler:2.47")
    implementation("androidx.hilt:hilt-navigation-fragment:1.0.0")
    kapt("androidx.hilt:hilt-compiler:1.0.0")
}

kapt {
    correctErrorTypes = true
}
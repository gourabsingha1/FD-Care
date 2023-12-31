plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.fdcare"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.fdcare"
        minSdk = 24
        targetSdk = 34
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
    buildFeatures {
        viewBinding = true
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

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.firebase:firebase-auth-ktx:22.3.0")
    implementation("com.google.firebase:firebase-database-ktx:20.3.0")
    implementation("com.google.firebase:firebase-storage-ktx:20.3.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // profile pic
    implementation("de.hdodenhof:circleimageview:3.1.0")

    // navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.6")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.6")

    // fragments
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    implementation("androidx.fragment:fragment:1.7.0-alpha07")

    implementation("androidx.startup:startup-runtime:1.1.1")

    // load images in local storage
    implementation("com.github.bumptech.glide:glide:4.15.1")
    implementation("com.squareup.picasso:picasso:2.8")

    // google signin
    implementation("com.google.android.gms:play-services-auth:20.7.0")

    // country code picker
    implementation("com.hbb20:ccp:2.7.0")

    // send otp verification
    implementation("androidx.browser:browser:1.7.0")

    // okhttp
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // firebase notification
    implementation("com.google.firebase:firebase-messaging:23.4.0")
    implementation("com.google.firebase:firebase-messaging-ktx:23.4.0")
}
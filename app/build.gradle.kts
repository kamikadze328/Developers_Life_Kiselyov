plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
}

android {
    compileSdk = 36
    namespace = "com.kamikadze328.developerslife"

    defaultConfig {
        applicationId = "com.kamikadze328.developerslife"
        minSdk = 24
        targetSdk = 36
        versionCode = 10
        versionName = "1.5"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.material)
    implementation(libs.kotlin.stdlib)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.legacy.support.core.utils)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.kotlinx.serialization.json)

    //okhttp
    implementation(libs.okhttp3.okhttp)

    //glide
    implementation(libs.glide.glide)
    annotationProcessor(libs.glide.compiler)
    implementation(libs.wasabeef.glide.transformations)
} 
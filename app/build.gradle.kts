import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.devtools.ksp)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-parcelize")
    id ("com.google.dagger.hilt.android")

}

android {
    namespace = "ir.example1.weather"
    compileSdk = 36

    defaultConfig {
        applicationId = "ir.example1.weather"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "WEATHER_API_KEY",
            "\"${project.properties["WEATHER_API_KEY"]}\""
        )

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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
    buildFeatures {
        buildConfig = true
        compose= true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    ksp(libs.androidx.room.compiler)
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)



    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.coroutines.android)

    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)


    implementation(libs.androidx.datastore.preferences)


    implementation(libs.play.services.location)

    // Unit Test
    testImplementation(libs.truth)

    // Mocking
    testImplementation(libs.mockk)

    // Coroutines Test
    testImplementation(libs.kotlinx.coroutines.test)

    // Flow Test
    testImplementation(libs.turbine)


    testImplementation(libs.androidx.core.testing)
    testImplementation(kotlin("test"))

    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.mockwebserver)

    //for compose added
    implementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(libs.androidx.navigation.compose)

    debugImplementation(libs.androidx.ui.tooling)
    implementation(libs.androidx.hilt.navigation.compose)

    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.runtime.livedata)
    // Coil for image loading in Compose
    implementation(libs.coil.compose)
}


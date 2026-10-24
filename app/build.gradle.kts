plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

val vinilosApiBaseUrl = providers.gradleProperty("vinilosApiBaseUrl")
    .orElse("https://sadasdasdadsa/")
    .get()
require(vinilosApiBaseUrl.startsWith("http://") || vinilosApiBaseUrl.startsWith("https://")) {
    "vinilosApiBaseUrl must start with http:// or https://"
}
require(vinilosApiBaseUrl.endsWith("/")) { "vinilosApiBaseUrl must end with /" }
val escapedApiBaseUrl = vinilosApiBaseUrl.replace("\\", "\\\\").replace("\"", "\\\"")

android {
    namespace = "com.example.vinilosmobile"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.vinilosmobile"
        minSdk = 21
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
        // Override with -PvinilosApiBaseUrl=... when a backend is available.
        buildConfigField("String", "BACK_VYNILS_BASE_URL", "\"$escapedApiBaseUrl\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    implementation(libs.androidx.viewpager2)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.glide)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}

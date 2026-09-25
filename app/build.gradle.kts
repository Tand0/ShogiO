
plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.github.tand0.shogio"
    compileSdk {
        version = release(37) {
            minorApiLevel = 1
        }
    }
    //experimentalProperties["android.experimental.self-contained-modules"] = true
    //experimentalProperties["android.experimental.allowNamespaceCollisions"] = true

    defaultConfig {
        applicationId = "com.github.tand0.shogio"
        minSdk = 35
        targetSdk = 37
        versionCode = 3
        versionName = "1.0.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    packaging {
        jniLibs {
            useLegacyPackaging = false
        }
    }
    buildFeatures {
        mlModelBinding = true
    }
}

dependencies {
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)
    implementation(libs.monitor)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.fragment.ktx)

    // TensorFlow Liteのコアライブラリ
    implementation(libs.litert.main)
    implementation(libs.litert.api)

    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)

    testImplementation(libs.junit)
}

import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

val signingProps = Properties()
val signingPropsFile = rootProject.file("key.properties")
if (signingPropsFile.exists()) {
    signingProps.load(FileInputStream(signingPropsFile))
}

val envProps = Properties()
val envPropsFile = rootProject.file("env.properties")
if (envPropsFile.exists()) {
    envProps.load(FileInputStream(envPropsFile))
}

android {
    namespace = "com.kumpali.mockgps"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.kumpali.mockgps"
        minSdk = 28
        targetSdk = 37
        versionCode = 5
        versionName = "1.0.4"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        buildConfigField(
            "String",
            "PLACES_API_KEY",
            "\"${envProps.getProperty("MAPS_API_KEY", "")}\""
        )
    }

    signingConfigs {
        create("release") {
            if (signingPropsFile.exists()) {
                storeFile = rootProject.file(signingProps.getProperty("storeFile"))
                storePassword = signingProps.getProperty("storePassword")
                keyAlias = signingProps.getProperty("keyAlias")
                keyPassword = signingProps.getProperty("keyPassword")
            }
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    splits {
        abi {
            isEnable = true
            reset()
            include("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
            isUniversalApk = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.19.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.11.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.11.0")
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.fragment:fragment-ktx:1.9.0")
    implementation(platform("androidx.compose:compose-bom:2026.09.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.compose.material:material-icons-extended")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    androidTestImplementation(platform("androidx.compose:compose-bom:2026.09.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // OpenStreetMap rendering through osmdroid. No API key is required.
    implementation("org.osmdroid:osmdroid-android:6.1.20") {
        exclude(group = "androidx.vectordrawable", module = "vectordrawable-animated")
    }

    // Google Places search only; map rendering remains OpenStreetMap.
    implementation("com.google.android.libraries.places:places:3.5.0") {
        exclude(group = "androidx.vectordrawable", module = "vectordrawable")
        exclude(group = "androidx.vectordrawable", module = "vectordrawable-animated")
    }

    // Viewmodel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.11.0")

    //Gson
    implementation("com.google.code.gson:gson:2.14.0")

}

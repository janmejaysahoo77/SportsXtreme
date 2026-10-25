plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.firebase.crashlytics)
}

val uploadSigningPropertyNames = listOf(
    "SPORTSXTREME_UPLOAD_STORE_FILE",
    "SPORTSXTREME_UPLOAD_STORE_PASSWORD",
    "SPORTSXTREME_UPLOAD_KEY_ALIAS",
    "SPORTSXTREME_UPLOAD_KEY_PASSWORD"
)
val uploadSigningValues = uploadSigningPropertyNames.associateWith { propertyName ->
    providers.gradleProperty(propertyName).orNull?.takeIf(String::isNotBlank)
        ?: providers.environmentVariable(propertyName).orNull
}
val uploadSigningIsConfigured = uploadSigningPropertyNames.all { propertyName ->
    !uploadSigningValues[propertyName].isNullOrBlank()
}
val uploadKeystorePath = uploadSigningValues["SPORTSXTREME_UPLOAD_STORE_FILE"]

val verifyReleaseSigning = tasks.register("verifyReleaseSigning") {
    group = "verification"
    description = "Checks that release signing credentials and the upload keystore are configured."
    doLast {
        val missingProperties = uploadSigningPropertyNames.filter { propertyName ->
            uploadSigningValues[propertyName].isNullOrBlank()
        }
        if (missingProperties.isNotEmpty()) {
            throw GradleException(
                "Release signing is not configured. Missing: ${missingProperties.joinToString()}. " +
                    "Set these in your user-level Gradle properties or environment variables. " +
                    "See docs/android-release-signing.md. No release key was generated."
            )
        }

        val keystore = project.file(uploadKeystorePath!!)
        if (!keystore.isFile || !keystore.canRead()) {
            throw GradleException(
                "Release signing keystore is missing or unreadable at the configured path. " +
                    "Check SPORTSXTREME_UPLOAD_STORE_FILE and see docs/android-release-signing.md."
            )
        }
    }
}

tasks.configureEach {
    if (name == "preReleaseBuild") {
        dependsOn(verifyReleaseSigning)
    }
}

android {
    namespace = "com.example.sportsxtreme"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.sportsxtreme"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        resourceConfigurations += setOf("en")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            if (uploadSigningIsConfigured) {
                signingConfig = signingConfigs.create("releaseUpload") {
                    storeFile = project.file(uploadKeystorePath!!)
                    storePassword = uploadSigningValues["SPORTSXTREME_UPLOAD_STORE_PASSWORD"]!!
                    keyAlias = uploadSigningValues["SPORTSXTREME_UPLOAD_KEY_ALIAS"]!!
                    keyPassword = uploadSigningValues["SPORTSXTREME_UPLOAD_KEY_PASSWORD"]!!
                }
            }
            isMinifyEnabled = true
            isShrinkResources = true
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
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation("androidx.compose.runtime:runtime-android")
    implementation("androidx.compose.animation:animation")
    implementation("com.airbnb.android:lottie-compose:6.6.7")
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.androidx.splashscreen)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.database)
    implementation(libs.firebase.firestore)
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-functions")
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.storage)
    implementation(libs.google.play.services.auth.api.phone)
    implementation(libs.google.play.services.location)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)
    implementation(libs.googleid)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    implementation("io.coil-kt.coil3:coil-compose:3.3.0")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.3.0")
    implementation(libs.androidx.datastore.preferences)
    implementation("androidx.drawerlayout:drawerlayout:1.2.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("com.google.zxing:core:3.5.3")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

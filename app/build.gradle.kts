plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "io.github.ploufty.foteli"
    compileSdk = 35

    defaultConfig {
        applicationId = "io.github.ploufty.foteli"
        minSdk = 33
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
    }

    // La clé de signature n'est jamais dans le dépôt : GitHub Actions la fournit
    // par des variables d'environnement alimentées par les secrets du dépôt.
    val keystoreFile = System.getenv("FOTELI_KEYSTORE_FILE")
    signingConfigs {
        if (keystoreFile != null) {
            create("release") {
                storeFile = file(keystoreFile)
                storePassword = System.getenv("FOTELI_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("FOTELI_KEY_ALIAS")
                keyPassword = System.getenv("FOTELI_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            // Sans clé (premiers essais), l'APK est signée avec une clé de test :
            // elle s'installe, mais ne pourra pas être mise à jour par une version signée.
            signingConfig = signingConfigs.findByName("release") ?: signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
}

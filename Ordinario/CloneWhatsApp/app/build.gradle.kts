import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.clonewhatsapp"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.example.clonewhatsapp"
        minSdk = 27
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val localProperties = Properties()
        val localFile = rootProject.file("local.properties")
        if (localFile.exists()) {
            val reader = localFile.reader()
            localProperties.load(reader)
            reader.close()
        }

        val supabaseUrl = localProperties.getProperty("SUPABASE_URL") ?: "https://tu-proyecto.supabase.co"
        val supabaseKey = localProperties.getProperty("SUPABASE_KEY") ?: "tu-anon-key-publica"

        buildConfigField("String", "SUPABASE_URL", "\"$supabaseUrl\"")
        buildConfigField("String", "SUPABASE_KEY", "\"$supabaseKey\"")
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)

    // Dependencias para consumir Supabase REST API y multimedia en Java
    implementation(libs.okhttp)
    implementation(libs.gson)
    implementation("com.github.bumptech.glide:glide:4.16.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}

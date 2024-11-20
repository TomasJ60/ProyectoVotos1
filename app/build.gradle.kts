plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "co.edu.unipiloto.proyectovotos"
    compileSdk = 34

    defaultConfig {
        applicationId = "co.edu.unipiloto.proyectovotos"
        minSdk = 26
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation(platform("com.google.firebase:firebase-bom:33.2.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth:23.0.0")
    //implementation("com.google.firebase:firebase-firestore:24.1.0")
    implementation("com.google.firebase:firebase-storage:21.0.0")
    implementation("com.google.firebase:firebase-database:20.1.0")
    implementation("com.squareup.picasso:picasso:2.8")

    implementation ("com.google.android.libraries.places:places:3.5.0")
    implementation ("com.google.android.gms:play-services-maps:18.1.0")

    implementation ("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.12.0")
    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")

    implementation ("com.google.firebase:firebase-firestore:24.3.0")
    implementation ("org.apache.poi:poi:5.2.2")
    implementation ("org.apache.poi:poi-ooxml:5.2.2")


    implementation ("androidx.work:work-runtime-ktx:2.9.1")

}
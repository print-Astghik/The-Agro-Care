plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")

}


android {
    namespace = "app.psy.innergrowth"
    compileSdk = 35

    defaultConfig {

        applicationId = "app.psy.innergrowth"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "OPENWEATHERMAP_API_KEY", "\"${project.findProperty("OPENWEATHERMAP_API_KEY")}\"")

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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
    }

}



dependencies {


    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.activity:activity:1.8.0")
    implementation("androidx.activity:activity-ktx:1.7.2")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")





    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-analytics")


    implementation("com.google.android.gms:play-services-auth:20.7.0")
    implementation ("com.github.TutorialsAndroid:GButton:v1.0.19")

    implementation ("com.github.bumptech.glide:glide:4.14.2")
    implementation(libs.firebase.storage)
    implementation(libs.firebase.firestore)
    implementation(libs.monitor)
    implementation(libs.ext.junit)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.testng)
    annotationProcessor ("com.github.bumptech.glide:compiler:4.14.2")
    implementation ("com.github.clans:fab:1.6.4")

    implementation ("com.google.firebase:firebase-database:21.0.0")
    implementation ("com.google.firebase:firebase-storage:21.0.1")
    implementation(platform("com.google.firebase:firebase-bom:33.10.0"))

    testImplementation ("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testRuntimeOnly ("org.junit.jupiter:junit-jupiter-engine:5.9.2")

    implementation ("com.google.android.gms:play-services-location:21.0.1")

    implementation ("com.android.volley:volley:1.2.1")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")

    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")

    implementation ("com.google.android.gms:play-services-maps:18.2.0")
    implementation ("com.google.android.gms:play-services-location:21.0.1")

    implementation ("androidx.fragment:fragment-ktx:1.5.7")

    implementation ("com.squareup.okhttp3:okhttp:4.9.3")


    implementation ("com.google.mlkit:image-labeling:17.0.7")
    implementation ("com.squareup.okhttp3:okhttp:4.9.3")

    implementation ("com.squareup.okhttp3:okhttp:4.9.3")
    implementation ("com.google.code.gson:gson:2.8.6")





}
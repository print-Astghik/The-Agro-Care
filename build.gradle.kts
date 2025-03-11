buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.8.0")
        classpath("com.google.gms:google-services:4.3.15")
        classpath ("com.google.gms:google-services:4.4.2")
    // Firebase plugin
    }

}
plugins {
    id("com.google.gms.google-services") version "4.4.2" apply false

}


import com.android.build.api.dsl.ApplicationExtension
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-parcelize")
    alias(libs.plugins.ksp)
    alias(libs.plugins.navigation.safeArgs)
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
}

// Firebase (Analytics/Crashlytics) is optional: it activates only when
// app/google-services.json is present, so the project builds without it.
if (file("google-services.json").exists()) {
    apply(plugin = libs.plugins.google.services.get().pluginId)
    apply(plugin = libs.plugins.firebase.crashlytics.get().pluginId)
}

fun getProperties(fileName: String): Properties {
    val properties = Properties()
    val file = file(fileName)
    if (file.exists()) {
        file.inputStream().use { properties.load(it) }
    }
    return properties
}

fun getProperty(properties: Properties, name: String): String =
    properties.getProperty(name) ?: "$name missing"

configure<ApplicationExtension> {
    namespace = "github.o4x.m2"
    compileSdk = 36

    defaultConfig {
        applicationId = "github.o4x.m2"
        minSdk = 28
        targetSdk = 36
        versionCode = 13
        versionName = "0.1.1 Beta"

        vectorDrawables {
            useSupportLibrary = true
        }

        multiDexEnabled = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        resourceConfigurations += "en"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "(${defaultConfig.versionCode}) DEBUG "
        }
    }

    packaging {
        resources {
            excludes += setOf("META-INF/LICENSE", "META-INF/NOTICE")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlin {
        jvmToolchain(21)
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    lint {
        abortOnError = false
        disable += setOf("MissingTranslation", "InvalidPackage")
    }
}

configurations.all {
    exclude(group = "org.jetbrains.kotlin", module = "kotlin-android-extensions-runtime")
}


dependencies {
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)

    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(project(":recyclerview-fastscroll"))
    implementation(files("libs/jaudiotagger-2.2.4-SNAPSHOT.jar"))

    implementation(libs.androidx.annotation)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.palette.ktx)
    implementation(libs.androidx.percentlayout)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.viewpager)
    implementation(libs.material)
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    implementation(libs.core)
    implementation(libs.input)
    implementation(libs.files)
    implementation(libs.color)
    implementation(libs.datetime)
    implementation(libs.bottomsheets)
    implementation(libs.lifecycle)

    implementation(libs.library)
    implementation(libs.advrecyclerview)
    implementation(libs.androidx.gridlayout)
    implementation(libs.blurview)

    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)

    implementation(libs.gson)
    implementation(libs.licensesdialog)

    implementation(libs.glide)
    ksp(libs.glide.ksp)
    implementation(libs.okhttp3.integration)

    implementation(libs.org.eclipse.egit.github.core)
    implementation(libs.bouncescrollview)

    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.android)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.verticalseekbar)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    testImplementation(libs.androidx.room.testing)

    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.common.java8)
    implementation(libs.androidx.lifecycle.service)

    implementation(libs.utilcodex)
    implementation(libs.imagepicker)
}

import com.android.build.api.dsl.ApplicationExtension
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-parcelize")
    id("kotlin-kapt")
    alias(libs.plugins.navigation.safeArgs)
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
    namespace = "github.o4x.musical"
    compileSdk = 36

    defaultConfig {
        applicationId = "github.o4x.musical"
        minSdk = 28
        targetSdk = 36
        versionCode = 11
        versionName = "0.0.6 Beta"

        vectorDrawables {
            useSupportLibrary = true
        }

        multiDexEnabled = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        dataBinding = true
        buildConfig = true
        compose = true
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
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(project(":appthemehelper"))
    implementation(project(":recyclerview-fastscroll"))
    implementation(project(":material-cab"))
    implementation(files("libs/jaudiotagger-2.2.4-SNAPSHOT.jar"))

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    implementation(libs.androidx.annotation)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.legacy.support.core.ui)
    implementation(libs.androidx.media)
    implementation(libs.androidx.palette.ktx)
    implementation(libs.androidx.percentlayout)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.androidx.recyclerview)
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

    implementation(libs.retrofit)
    implementation(libs.adapter.rxjava2)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)

    implementation(libs.gson)
    implementation(libs.licensesdialog)

    implementation(libs.glide)
    kapt(libs.compiler)
    implementation(libs.okhttp3.integration)

    implementation(libs.org.eclipse.egit.github.core)
    implementation(libs.bouncescrollview)

    implementation(libs.koin.android)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.verticalseekbar)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)
    implementation(libs.androidx.room.rxjava2)
    implementation(libs.androidx.room.guava)
    testImplementation(libs.androidx.room.testing)

    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.common.java8)
    implementation(libs.androidx.lifecycle.service)

    implementation(libs.dagger)
    kapt(libs.dagger.compiler)

    implementation(libs.utilcodex)
    implementation(libs.imagepicker)
}

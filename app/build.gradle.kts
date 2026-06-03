plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.foxy.macscanner"
    compileSdk = 34 // متوافق مع أحدث إصدارات أندرويد

    defaultConfig {
        applicationId = "com.foxy.macscanner"
        minSdk = 26 // لضمان تشغيل التطبيق من أندرويد 8 فما فوق ليدعم الدوال الحديثة
        targetSdk = 34
        versionCode = 1
        versionName = "3.9" // نفس إصدار السكربت الأصلي

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true // تفعيل واجهات Jetpack Compose
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
    packaging {
    resources {
        excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

dependencies {
    // مكتبات أندرويد الأساسية ودورة الحياة
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")

    // مكتبات واجهات المستخدم (Jetpack Compose)
    implementation(platform("androidx.compose:compose-bom:2023.08.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    // مكتبة العمليات الموازية والخلفية (الـ Bots)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // مكتبة OkHttp لإرسال طلبات الـ HTTP والتعامل مع السيرفرات وتخطي الـ SSL
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
}

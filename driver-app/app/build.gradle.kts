plugins {
	id("com.android.application")
	id("org.jetbrains.kotlin.android")
	id("com.google.devtools.ksp")
	id("com.google.dagger.hilt.android")
}

android {
	namespace = "com.taxiplatform.driver"
	compileSdk = 34

	defaultConfig {
		applicationId = "com.taxiplatform.driver"
		minSdk = 26
		targetSdk = 34
		versionCode = 1
		versionName = "0.1.0"

		// Override with -PbackendBaseUrl=http://<host>:8080/ (e.g. 10.0.2.2 for the emulator).
		buildConfigField(
			"String",
			"BASE_URL",
			"\"${project.findProperty("backendBaseUrl") ?: "http://10.0.2.2:8080/"}\"",
		)
	}

	buildTypes {
		release {
			isMinifyEnabled = false
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

	composeOptions {
		kotlinCompilerExtensionVersion = "1.5.15"
	}

	packaging {
		resources {
			excludes += "/META-INF/{AL2.0,LGPL2.1}"
		}
	}
}

dependencies {
	implementation(platform("androidx.compose:compose-bom:2024.09.03"))
	implementation("androidx.core:core-ktx:1.13.1")
	implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
	implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.6")
	implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
	implementation("androidx.activity:activity-compose:1.9.2")
	implementation("androidx.navigation:navigation-compose:2.8.0")

	implementation("androidx.compose.ui:ui")
	implementation("androidx.compose.ui:ui-graphics")
	implementation("androidx.compose.ui:ui-tooling-preview")
	implementation("androidx.compose.material3:material3")

	implementation("com.google.dagger:hilt-android:2.52")
	ksp("com.google.dagger:hilt-android-compiler:2.52")
	implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

	implementation("com.squareup.retrofit2:retrofit:2.11.0")
	implementation("com.squareup.retrofit2:converter-moshi:2.11.0")
	implementation("com.squareup.moshi:moshi-kotlin:1.15.1")
	implementation("com.squareup.okhttp3:okhttp:4.12.0")
	implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

	implementation("androidx.datastore:datastore-preferences:1.1.1")

	implementation("com.google.android.gms:play-services-location:21.3.0")

	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

	testImplementation("junit:junit:4.13.2")
	testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
	testImplementation("app.cash.turbine:turbine:1.1.0")
	testImplementation("io.mockk:mockk:1.13.12")

	debugImplementation("androidx.compose.ui:ui-tooling")
}

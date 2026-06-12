import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.android.library)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    js(IR) {
        moduleName = "futureme-shared"
        useEsModules()
        browser()
        binaries.library()
        generateTypeScriptDefinitions()
    }

    sourceSets {
        commonMain {
            kotlin.srcDirs(
                "models/src/commonMain/kotlin",
                "calculators/src/commonMain/kotlin",
                "scenario-engine/src/commonMain/kotlin",
                "financial-gps/src/commonMain/kotlin",
                "goal-engine/src/commonMain/kotlin",
                "money-leak-detector/src/commonMain/kotlin",
                "life-event-planner/src/commonMain/kotlin",
                "life-readiness-engine/src/commonMain/kotlin",
                "banking-intelligence/src/commonMain/kotlin",
                "insights-engine/src/commonMain/kotlin",
                "mock-data/src/commonMain/kotlin",
                "ai-assistant/src/commonMain/kotlin",
                "design-system/src/commonMain/kotlin",
                "domain/src/commonMain/kotlin",
            )
            dependencies {
                implementation(libs.kotlinx.serialization.json)
            }
        }
        commonTest {
            kotlin.srcDirs("domain/src/commonTest/kotlin")
            dependencies {
                implementation(kotlin("test"))
            }
        }
        jsMain {
            kotlin.srcDirs("web-bridge/src/jsMain/kotlin")
        }
    }
}

android {
    namespace = "com.futureme.shared"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

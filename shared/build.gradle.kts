plugins {
    kotlin("multiplatform")
}

kotlin {
    jvm()


    js {
        browser {
            commonWebpackConfig {
                outputFileName = "class-file-visualizer.js"
            }
        }
        binaries.executable()
    }
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-io-core:0.8.2")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

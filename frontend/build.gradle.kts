import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kilua)
}

kotlin {
    js(IR) {
        useEsModules()
        browser {
            commonWebpackConfig {
                outputFileName = "main.bundle.js"
                sourceMaps = false
            }
        }
        binaries.executable()
    }
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        useEsModules()
        browser {
            commonWebpackConfig {
                outputFileName = "main.bundle.js"
                sourceMaps = true
                // https://kilua.gitbook.io/kilua-guide/1.-getting-started-1/debugging
                // devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                //     static = (static ?: mutableListOf()).apply {
                //         // Serve sources to debug inside browser
                //         add(project.projectDir.path)
                //     }
                // }
            }
        }
        binaries.executable()
    }
    
    sourceSets {
        commonMain.dependencies {
            implementation(projects.shared)

            implementation(libs.kilua)
            implementation(libs.kilua.routing)
            implementation(libs.kilua.jetpack)
            implementation(libs.kilua.imask)
            implementation(libs.kilua.bootstrap)
            implementation(libs.kilua.toastify)
            implementation(libs.kilua.tempus.dominus)
            implementation(libs.kilua.rsup.progress)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)

            implementation(libs.ktor.client.js)
            implementation(libs.kotlinx.rpc.krpc.client)
            implementation(libs.kotlinx.rpc.krpc.ktor.client)
            implementation(libs.kotlinx.rpc.krpc.serialization.json)
        }
    }
}

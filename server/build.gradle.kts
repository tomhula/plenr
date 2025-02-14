plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.serialization)
    alias(libs.plugins.kotlinx.rpc.plugin)
    application
}

application {
    mainClass.set("cz.tomashula.plenr.MainKt")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=${extra["io.ktor.development"] ?: "false"}")
}

tasks {
    val wasmJsBrowserWebpackTask = project(":frontend").tasks.getByName("wasmJsBrowserDevelopmentExecutableDistribution")

    val compileFrontendDev by registering(Copy::class) {
        val resourcesOutputPath = "frontend"

        dependsOn(wasmJsBrowserWebpackTask)

        from(wasmJsBrowserWebpackTask.outputs.files)

        // TEMP: This is a workaround, the wasmJsBrowserDevelopmentExecutableDistribution task for some reason does not include wasm source maps
        from(project(":frontend").layout.buildDirectory.file("compileSync/wasmJs/main/developmentExecutable/kotlin/main.bundle.wasm.map"))

        into(processResources.get().destinationDir.resolve(resourcesOutputPath))
        includeEmptyDirs = false
    }

    compileKotlin {
        // dependsOn(compileFrontendDev)
    }
}

ktor {
    fatJar {
        archiveFileName = rootProject.name + ".jar"
    }
}

dependencies {
    implementation(projects.shared)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)
    implementation(libs.logback)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.websockets)
    implementation(libs.kotlinx.rpc.krpc.server)
    implementation(libs.kotlinx.rpc.krpc.serialization.json)
    implementation(libs.kotlinx.rpc.krpc.ktor.server)

    implementation(libs.clikt)
    implementation(libs.mysql.connector.j)
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.kotlin.datetime)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.commons.email2.jakarta)
}

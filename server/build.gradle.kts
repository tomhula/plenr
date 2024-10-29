plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.serialization)
    application
}

repositories {
    google()
    mavenCentral()
}

application {
    mainClass.set("me.tomasan7.plenr.MainKt")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=${extra["io.ktor.development"] ?: "false"}")
}

tasks {
    val wasmJsBrowserWebpackTask = project(":frontend").tasks.getByName("wasmJsBrowserDevelopmentExecutableDistribution")

    val compileFrontendDev by registering(Copy::class) {
        val resourcesOutputPath = "frontend"

        dependsOn(wasmJsBrowserWebpackTask)
        from(wasmJsBrowserWebpackTask.outputs.files)
        into(processResources.get().destinationDir.resolve(resourcesOutputPath))
    }

    compileKotlin {
        dependsOn(compileFrontendDev)
    }
}

dependencies {
    implementation(projects.shared)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)
    implementation(libs.logback)
    implementation(libs.ktor.server.core.jvm)
    implementation(libs.ktor.server.netty.jvm)
    implementation(libs.ktor.server.status.pages.jvm)
    implementation(libs.ktor.server.content.negotiation.jvm)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.websockets.jvm)
    implementation(libs.clikt)
    implementation(libs.mysql.connector.j)
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.kotlin.datetime)
    implementation(libs.ktor.server.auth.jvm)
    implementation(libs.ktor.server.auth.jwt.jvm)
    implementation(libs.commons.email2.jakarta)
}
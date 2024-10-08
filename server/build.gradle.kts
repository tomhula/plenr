plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.serialization)
    application
}

application {
    mainClass.set("me.tomasan7.MainKt")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=${extra["io.ktor.development"] ?: "false"}")
}

dependencies {
    implementation(projects.shared)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.logback)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.clikt)

    testImplementation(libs.ktor.server.tests)
    testImplementation(libs.kotlin.test.junit)
}
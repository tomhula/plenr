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
    val jsBrowserDistributionTask = project(":frontend").tasks.getByName("jsBrowserDistribution")
    val jsBrowserDevDistributionTask = project(":frontend").tasks.getByName("jsBrowserDevelopmentExecutableDistribution")
    val resourcesFrontendDestination = "frontend"
    val processResourcesTask = processResources.get()

    val processFrontendDev by registeringCopyIntoResources(
        resourcesFrontendDestination,
        processResourcesTask,
        jsBrowserDevDistributionTask
    )

    val processFrontendProd by registeringCopyIntoResources(
        resourcesFrontendDestination,
        processResourcesTask,
        jsBrowserDistributionTask
    )

    shadowJar {
        dependsOn(processFrontendProd)
    }

    this.run.configure {
        workingDir = rootProject.projectDir.resolve("run")
        dependsOn(processFrontendDev)
    }
}

fun PolymorphicDomainObjectContainer<Task>.registeringCopyIntoResources(
    resourcesDestination: String,
    processResourcesTask: ProcessResources,
    outputTask: Task
) = registering(Copy::class) {
    dependsOn(outputTask)
    from(outputTask.outputs.files)
    into(processResourcesTask.destinationDir.resolve(resourcesDestination))
    includeEmptyDirs = false
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

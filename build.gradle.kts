val kotlin_version: String by project
val logback_version: String by project
val exposed_version: String by project
val sqlite_jdbc_version: String by project

plugins {
    kotlin("jvm") version "2.3.0"
    id("io.ktor.plugin") version "3.4.1"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.3.0"
}

group = "cat.montilivi"
version = "0.0.1"

application {
    mainClass = "io.ktor.server.cio.EngineMain"
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation("io.ktor:ktor-server-core")
    implementation("io.ktor:ktor-server-content-negotiation")
    implementation("io.ktor:ktor-serialization-kotlinx-json")
    implementation("io.ktor:ktor-server-cio")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("io.ktor:ktor-server-config-yaml")

    //HTML DSL
    implementation("io.ktor:ktor-server-html-builder")

    //XPOSED
    implementation("org.jetbrains.exposed:exposed-core:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-dao:$exposed_version")

    // Driver de SQLite
    implementation("org.xerial:sqlite-jdbc:$sqlite_jdbc_version")
    implementation("io.ktor:ktor-server-host-common:3.4.1")
    runtimeOnly("org.xerial:sqlite-jdbc:${sqlite_jdbc_version}")

    //Encriptador per als paswords
    implementation("org.mindrot:jbcrypt:0.4")

    //Autentificació
    implementation("io.ktor:ktor-server-auth")
    implementation("io.ktor:ktor-server-auth-jwt")

    //WebSockets
    implementation("io.ktor:ktor-server-websockets:3.4.1")

    //StatusPage
    implementation("io.ktor:ktor-server-status-pages:3.4.1")

    //HikariPC
    implementation("com.zaxxer:HikariCP:5.1.0")

    //CORS
    implementation("io.ktor:ktor-server-cors:3.4.1")

    //Documentació automàtica
    implementation("io.ktor:ktor-server-swagger:3.4.1")
    implementation("io.ktor:ktor-server-openapi:3.4.1")
    implementation("io.ktor:ktor-server-routing-openapi:3.4.1")

    testImplementation("io.ktor:ktor-server-test-host")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
    testImplementation("org.xerial:sqlite-jdbc:${sqlite_jdbc_version}")
}

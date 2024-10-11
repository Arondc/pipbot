import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.2.2"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.spring") version "1.9.22"
    kotlin("plugin.jpa") version "1.9.22"
}

group = "de.arondc"
version = "0.0.1-SNAPSHOT"

tasks.bootJar {
    archiveFileName.set("pipbot.jar")
}

tasks.jar {
    enabled = false
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
}

extra["springModulithVersion"] = "1.1.2"

dependencies {
    //Kotlin dependencies
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("io.github.microutils:kotlin-logging-jvm:2.0.11")

    //Database
    runtimeOnly("com.h2database:h2")

    //starters
    implementation("org.springframework.modulith:spring-modulith-starter-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.flywaydb:flyway-core")

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(module = "mockito-core")
        because("We use Mockk instead")
    }
    testImplementation("com.ninja-squad:springmockk:4.0.2")

    implementation("org.springframework.modulith:spring-modulith-starter-test") {
        exclude("commons-logging", "commons-logging")
    }
    //3rd party
    implementation("com.github.twitch4j:twitch4j:1.19.0") {
        exclude("commons-logging", "commons-logging")
    }
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.modulith:spring-modulith-bom:${property("springModulithVersion")}")
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "21"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

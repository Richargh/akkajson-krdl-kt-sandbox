import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.60"
}

repositories {
    mavenCentral()
}

dependencies {
    /** Language dependencies **/
    implementation(kotlin("reflect"))
    implementation(kotlin("stdlib-jdk8"))

    /** Main dependencies **/
    val akkaVersion: String by project
    implementation("com.typesafe.akka:akka-actor-typed_2.12:$akkaVersion")

    /** Test dependencies **/
    val junitVersion: String by project
    val assertjVersion: String by project
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.assertj:assertj-core:$assertjVersion")
    implementation("com.typesafe.akka:akka-testkit_2.12:$akkaVersion")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "1.8"
    }
}


tasks.withType<Test> {
    useJUnitPlatform { }
    testLogging {
        showExceptions = true
        showStandardStreams = true
        events("passed", "skipped", "failed")
    }
}

tasks.wrapper {
    gradleVersion = "6.0.1"
}

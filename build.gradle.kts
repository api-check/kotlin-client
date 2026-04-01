plugins {
    kotlin("jvm") version "1.9.22"
    id("maven-publish")
}

group = "nl.apicheck"
version = "2.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    
    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            pom {
                name.set("ApiCheck Kotlin Client")
                description.set("Kotlin client for ApiCheck - address validation, search, and verification")
                url.set("https://apicheck.nl")
                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        id.set("apicheck")
                        name.set("ApiCheck")
                        email.set("support@apicheck.nl")
                    }
                }
                scm {
                    url.set("https://github.com/api-check/kotlin-client")
                }
            }
        }
    }
}

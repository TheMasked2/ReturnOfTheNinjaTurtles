plugins {
    java
    id("org.springframework.boot") version "4.0.3"
    id("io.spring.dependency-management") version "1.1.6"
}
configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "net.bytebuddy") {
            useVersion("1.17.6") // This version officially supports Java 25
        }
    }
}
group = "com.turtleshop"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    // CRITICAL: You need this for the repositories to work
    implementation("org.springframework.boot:spring-boot-starter-jdbc")

    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")
    implementation(platform("org.springframework.boot:spring-boot-dependencies:4.0.3"))

    // Specify the version for Lombok (currently 1.18.36 is the latest stable)
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    implementation("org.bouncycastle:bcprov-jdk18on:1.78.1")

    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")

    runtimeOnly("org.postgresql:postgresql")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.test {
    useJUnitPlatform()
}
plugins {
    java
    id("org.springframework.boot") version "3.4.5"
    id("io.spring.dependency-management") version "1.1.6"
}
configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "net.bytebuddy") {
            useVersion("1.17.6") // This version officially supports Java 25
        }
    }
}
group = "org.turtleshop"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

repositories {
    mavenCentral()
}
val gatlingRuntime: Configuration by configurations.creating
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    // JDBC is Java version of Dapper. Need this for Repositories to work.
    implementation("org.springframework.boot:spring-boot-starter-jdbc")

    implementation("org.flywaydb:flyway-core:10.22.0")
    implementation("org.flywaydb:flyway-database-postgresql:10.22.0")

    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // NoSQL - MongoDB
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")

    // NoSQL - Neo4j
    implementation("org.springframework.boot:spring-boot-starter-data-neo4j")

    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")

    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    implementation("org.bouncycastle:bcprov-jdk18on:1.78.1")

    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")

    // Main test framework: JUnit 5, AssertJ, Mockito, Spring test tools
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    // Testcontainers: starts temporary Docker containers for tests
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")

    // Security testing: @WithMockUser, MockMvc security support
    testImplementation("org.springframework.security:spring-security-test")

    // Mockito JUnit 5 integration
    testImplementation("org.mockito:mockito-junit-jupiter")

    // Lombok support in tests
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")

    // JUnit launcher
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // PostgreSQL
    runtimeOnly("org.postgresql:postgresql")

    gatlingRuntime("io.gatling.highcharts:gatling-charts-highcharts:3.11.5")
    implementation("io.github.cdimascio:dotenv-java:3.0.2")
    testImplementation("io.github.cdimascio:dotenv-java:3.0.2")
}
sourceSets {
    create("gatling") {
        java.srcDir("src/gatling/java")
        resources.srcDir("src/gatling/resources")
        compileClasspath += configurations.getByName("testCompileClasspath") + gatlingRuntime
        runtimeClasspath += output + configurations.getByName("testRuntimeClasspath") + gatlingRuntime
    }
}
tasks.test {
    useJUnitPlatform()
    systemProperty("api.version", "1.44")
}
val gatlingSimulations = listOf(
    "org.turtleshop.api.performance.AdminCustomerAuditSimulation",
    "org.turtleshop.api.performance.CustomerRegisterAndCheckoutSimulation",
    "org.turtleshop.api.performance.RaceConditionSimulation",
    "org.turtleshop.api.performance.CatalogSearchAndPaginationSimulation",
)

val gatlingTaskNames = gatlingSimulations.map { simulationClass ->
    val simpleName = simulationClass.substringAfterLast(".")
    val taskName = "gatlingRun$simpleName"

    tasks.register<JavaExec>(taskName) {
        description = "Runs Gatling simulation $simpleName"
        group = "Load Test"

        dependsOn("compileGatlingJava", "processGatlingResources")

        val gatlingSourceSet = sourceSets.getByName("gatling")

        classpath = gatlingSourceSet.runtimeClasspath + gatlingRuntime
        mainClass.set("io.gatling.app.Gatling")

        args(
            "-s", simulationClass,
            "-rf", "${layout.buildDirectory.get().asFile}/reports/gatling/$simpleName"
        )
    }

    taskName
}

tasks.register("gatlingRun") {
    description = "Runs all Gatling simulations"
    group = "Load Test"

    dependsOn(gatlingTaskNames)
}
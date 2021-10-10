plugins {
    kotlin("jvm")
}

dependencies {
    // Modules
    implementation(project(":domain"))

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.5.31")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.5.31")

    // Exposed SQL
    implementation("org.jetbrains.exposed:exposed-jdbc:0.35.1")
    implementation("mysql:mysql-connector-java:8.0.25")
    implementation("org.springframework.boot:spring-boot-starter-jdbc:2.5.5")
    implementation("org.jetbrains.exposed:exposed-core:0.35.1")
    implementation("org.jetbrains.exposed:exposed-dao:0.35.1")
    implementation("org.jetbrains.exposed:exposed-jodatime:0.35.1")

    // Spring
    implementation("org.springframework.boot:spring-boot-starter:2.5.5")

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:0.11.2")
    implementation("io.jsonwebtoken:jjwt-impl:0.11.2")
}
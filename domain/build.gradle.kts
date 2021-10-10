plugins {
    kotlin("jvm")
}

dependencies {
    // Kotlin
    implementation(kotlin("reflect"))
    implementation(kotlin("stdlib-jdk8"))

    // Spring
    implementation("org.springframework.boot:spring-boot-starter")

    // Exposed datatypes
    implementation("org.jetbrains.exposed:exposed-jodatime:0.29.1")

    // JWT
    implementation("at.favre.lib:bcrypt:0.9.0")
    implementation("io.jsonwebtoken:jjwt-api:0.11.2")
    implementation("io.jsonwebtoken:jjwt-impl:0.11.2")
    implementation("io.jsonwebtoken:jjwt-jackson:0.11.2")
}
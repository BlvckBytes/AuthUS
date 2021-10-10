plugins {
    kotlin("jvm")
}

dependencies {
    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.5.31")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.5.31")

    // Spring
    implementation("org.springframework.boot:spring-boot-starter:2.5.5")

    // Exposed datatypes
    implementation("org.jetbrains.exposed:exposed-jodatime:0.35.1")

    // JWT
    implementation("at.favre.lib:bcrypt:0.9.0")
    implementation("io.jsonwebtoken:jjwt-api:0.11.2")
    implementation("io.jsonwebtoken:jjwt-impl:0.11.2")
    implementation("io.jsonwebtoken:jjwt-jackson:0.11.2")
}
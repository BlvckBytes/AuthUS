plugins {
    kotlin("jvm")
}

dependencies {
    // Modules
    implementation(project(":domain"))

    // Kotlin
    implementation(kotlin("reflect"))
    implementation(kotlin("stdlib"))

    // Spring web
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
}
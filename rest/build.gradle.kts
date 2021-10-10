plugins {
    kotlin("jvm")
}

dependencies {
    // Modules
    implementation(project(":domain"))

    // Kotlin
    implementation(kotlin("reflect"))
    implementation(kotlin("stdlib-jdk8"))

    // Spring javax validation
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("javax.validation:validation-api")

    // Spring web
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")

    // Jackson
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-joda")
}
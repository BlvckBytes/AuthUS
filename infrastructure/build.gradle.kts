plugins {
    kotlin("jvm")
}

dependencies {
    implementation(kotlin("reflect"))
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":domain"))

    implementation("org.jetbrains.exposed:exposed-jdbc:0.29.1")
    implementation("mysql:mysql-connector-java:8.0.23")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")

    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.jetbrains.exposed:exposed-core:0.29.1")
    implementation("org.jetbrains.exposed:exposed-dao:0.29.1")
    implementation("org.jetbrains.exposed:exposed-jodatime:0.29.1")
    implementation("io.jsonwebtoken:jjwt-api:0.11.2")
    implementation("io.jsonwebtoken:jjwt-impl:0.11.2")
}
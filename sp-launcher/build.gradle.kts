plugins {
    kotlin("jvm")
}

dependencies {
    // Modules
    implementation(project(":domain"))
    implementation(project(":infrastructure"))
    implementation(project(":rest"))
    implementation(project(":graphql"))

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.5.31")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.5.31")

    // Spring web
    implementation("org.springframework.boot:spring-boot-starter-web:2.5.5")
    implementation("org.springframework.boot:spring-boot-starter:2.5.5")

    // Spring junit testing with rest-assured and sql-containers
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
    testImplementation("io.rest-assured:rest-assured:4.4.0")
    testImplementation("org.hamcrest:hamcrest-all:1.3")
    testImplementation("org.testcontainers:mysql:1.16.0")
    testImplementation("org.springframework.boot:spring-boot-starter-test:2.5.5")
}

tasks.test {
    useJUnitPlatform()
}
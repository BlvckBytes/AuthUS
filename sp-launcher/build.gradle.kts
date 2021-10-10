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
    implementation(kotlin("reflect"))
    implementation(kotlin("stdlib"))

    // Spring web
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter")

    // Spring junit testing with rest-assured and sql-containers
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.3.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.3.1")
    testImplementation("io.rest-assured:rest-assured")
    testImplementation("org.hamcrest:hamcrest-all:1.3")
    testImplementation("org.testcontainers:mysql:1.15.2")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.test {
    useJUnitPlatform()
}
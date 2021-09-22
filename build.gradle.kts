plugins {
    kotlin("jvm") version "1.5.10"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("org.springframework.boot") version "2.5.1"
}

subprojects {
    apply(plugin="io.spring.dependency-management")
    apply(plugin="org.springframework.boot")

    repositories {
        mavenCentral()
        jcenter()
    }

    dependencyManagement {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:2.5.1")
        }
    }
}

allprojects {
    group = "me.blvckbytes"
    version = "0.0.1"

    repositories {
        mavenCentral()
        jcenter()
    }
}
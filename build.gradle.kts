plugins {
    java
    id("org.springframework.boot") version "4.0.5"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.diffplug.spotless") version "7.0.0.BETA4"
    checkstyle
}

group = "com.example"
version = "0.0.1-SNAPSHOT"
description = "sidequesting"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

extra["springAiVersion"] = "2.0.0-M4"

spotless {
    java {
        googleJavaFormat("1.35.0")
    }
}

checkstyle {
    toolVersion = "10.21.2"
    configFile = file("config/checkstyle/checkstyle.xml")
    isIgnoreFailures = false
    maxWarnings = 0
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webclient")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.ai:spring-ai-starter-model-anthropic")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    testImplementation("org.springframework.boot:spring-boot-starter-webclient-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webflux-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.ai:spring-ai-bom:${property("springAiVersion")}")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

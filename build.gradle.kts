// Flyway 와 MySQL 드라이버를 빌드 시점에 사용할 수 있게 함
buildscript {
    repositories{
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("org.flywaydb:flyway-mysql:10.8.1")
        classpath("com.mysql:mysql-connector-j:8.0.33")
    }
}

plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.4.3"
    id("io.spring.dependency-management") version "1.1.7"

    // Flyway DB 마이그레이션
    id("org.flywaydb.flyway") version "10.8.1"
    // JOOQ 코드생성
    id("nu.studer.jooq") version "9.0"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

val jooqVersion: String by extra("3.19.19")
val dbUrl = "jdbc:mysql://localhost:3308/jooq"
val dbUsername = "root"
val dbPassword = "1234"
val dbDriver = "com.mysql.cj.jdbc.Driver"

dependencies {
    // Spring Boo
    implementation("org.springframework.boot:spring-boot-starter-jooq")
    implementation("org.springframework.boot:spring-boot-starter-web")

    // Kotlin
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // Database & Flyway
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-mysql")
    implementation("com.mysql:mysql-connector-j")

    // jOOQ
    jooqGenerator("com.mysql:mysql-connector-j")
    jooqGenerator("org.jooq:jooq-meta-extensions:$jooqVersion")


    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly("com.h2database:h2")
}

flyway {
    driver = dbDriver
    url = dbUrl
    user = dbUsername
    password = dbPassword
    locations = arrayOf("classpath:db/migration")
}

jooq {
    version.set(jooqVersion)
    configurations {
        create("main") {
            generateSchemaSourceOnCompilation.set(true) // 컴파일 시 코드 생성
            jooqConfiguration.apply {
                jdbc.apply { // DB 연결 설정
                    driver = dbDriver
                    url = dbUrl
                    user = dbUsername
                    password = dbPassword
                }
                generator.apply { // 코드 생성 설정
                    name = "org.jooq.codegen.KotlinGenerator"
                    database.apply {
                        name = "org.jooq.meta.mysql.MySQLDatabase"
                        inputSchema = "jooq"
                        includes = ".*"
                        excludes = "flyway_schema_history"
                    }
                    generate.apply { // 생성 옵션 설정
                        isDeprecated = false
                        isRecords = true
                        isDaos = true
                        isFluentSetters = true
                        isJavaTimeTypes = true
                    }
                    target.apply { // 생성된 코드 위치 설정
                        packageName = "com.example.generated"
                        directory = "build/generated/jooq/main"
                    }
                }
            }
        }
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

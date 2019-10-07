plugins {
    id("com.github.johnrengelman.shadow") version "5.1.0"
    id("org.jetbrains.kotlin.jvm") version "1.2.40"
    id("io.gitlab.arturbosch.detekt") version "1.0.1"
}

extra["exposedVersion"] = "0.10.1"
extra["postgresDriverVersion"] = "42.2.1"
extra["javaSparkVersion"] = "2.7.1"
extra["apacheCommonsCliVersion"] = "1.4"
extra["jsonVersion"] = "20180130"
extra["slf4jVersion"] = "1.7.25"
extra["restAssuredVersion"] = "4.1.1"
extra["restAssuredKotlinExtVersion"] = "4.1.2"
extra["junitJupiterVersion"] = "5.5.2"
extra["mockkVersion"] = "1.9.3"
extra["jacksonModuleKotlinVersion"] = "2.10.0"

repositories {
    jcenter()
    maven { setUrl("https://dl.bintray.com/kotlin/exposed") }
}

dependencies {
    compile("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // Core.
    compile("org.jetbrains.exposed:exposed:${project.extra["exposedVersion"]}")
    compile("org.postgresql:postgresql:${project.extra["postgresDriverVersion"]}")
    compile("com.sparkjava:spark-core:${project.extra["javaSparkVersion"]}")

    // Util.
    compile("commons-cli:commons-cli:${project.extra["apacheCommonsCliVersion"]}")
    compile("org.json:json:${project.extra["jsonVersion"]}")

    // Logging.
    compile("org.slf4j:slf4j-log4j12:${project.extra["slf4jVersion"]}")
    compile("org.slf4j:slf4j-api:${project.extra["slf4jVersion"]}")

    // Test
    testImplementation("io.rest-assured:rest-assured:${project.extra["restAssuredVersion"]}")
    testImplementation("io.rest-assured:kotlin-extensions:${project.extra["restAssuredKotlinExtVersion"]}")
    testImplementation("org.junit.jupiter:junit-jupiter:${project.extra["junitJupiterVersion"]}")
    testImplementation("io.mockk:mockk:${project.extra["mockkVersion"]}")
    testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin:${project.extra["jacksonModuleKotlinVersion"]}")
}

tasks.shadowJar {
    archiveName = "meme-grid.jar"
    manifest {
        attributes["Main-Class"] = "com.edd.memegrid.LauncherKt"
    }
}

tasks.compileKotlin {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_1_8.toString()
}

tasks.compileTestKotlin {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_1_8.toString()
}

tasks.test {
    // enable JUnit Platform (a.k.a. JUnit 5) support
    useJUnitPlatform()

    // explicitly include tests
    include("**/*Test.class")

    // show standard out and standard error of the test JVM(s) on the console
    testLogging.showExceptions = true

    // listen to events in the test execution lifecycle
    beforeTest(closureOf<TestDescriptor> {
        logger.lifecycle("Running ${this.className}.${this.name}")
    })
}

tasks.wrapper {
    gradleVersion = "5.6.2"
}

tasks.detekt {
    input = files("src/main/kotlin")

    reports {
        xml {
            enabled = true
            destination = file("build/reports/detekt.xml")
        }
        html {

            enabled = true
            destination = file("build/reports/detekt.html")
        }
        txt {
            enabled = true
            destination = file("build/reports/detekt.txt")
        }
    }
}

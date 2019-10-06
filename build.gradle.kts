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

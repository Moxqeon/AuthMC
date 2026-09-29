import java.net.URI

plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("java-library")
    id("maven-publish")
    kotlin("jvm") version("2.4.0")
}

group = "org.moxqeon"
version = "1.0"

repositories {
    mavenLocal()
    maven {
        name = "Aliyun"
        url = URI("https://maven.aliyun.com/repository/public")
    }
    mavenCentral()
    maven {
        name = "CodeMC"
        url = URI("https://repo.codemc.io/repository/maven-public/")
    }
    maven {
        name = "DMulloy"
        url = URI("https://repo.dmulloy2.net/repository/public/")
    }
    maven {
        name = "Minebench"
        url = URI("https://repo.minebench.de/")
    }
}

dependencies {
    compileOnly("org.moxqeon:MoxLib:1.1.0")
    compileOnly(fileTree(mapOf("dir" to "../../BukkitLib", "include" to "*.jar")))
    compileOnly(kotlin("stdlib-jdk8"))
}

tasks {
    processResources {
        filteringCharset = "UTF-8"
        val props = mapOf("version" to version)
        inputs.properties(props)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
    compileJava {
        options.encoding = "UTF-8"
    }
}

kotlin {
    jvmToolchain(21)
}

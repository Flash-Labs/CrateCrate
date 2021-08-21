import org.spongepowered.gradle.plugin.config.PluginLoaders
import org.spongepowered.plugin.metadata.PluginDependency

plugins {
    `java-library`
    id("org.spongepowered.gradle.plugin") version "1.1.1"
}

group = "dev.flashlabs.cratecrate"
version = "0.1.0"

repositories {
    mavenCentral()
}

sponge {
    apiVersion("8.0.0")
    plugin("cratecrate") {
        loader(PluginLoaders.JAVA_PLAIN)
        displayName("CrateCrate")
        mainClass("dev.flashlabs.cratecrate.CrateCrate")
        description("The cratest crate plugin.")
        links {
            homepage("https://github.com/flash-labs/CrateCrate")
            source("https://github.com/flash-labs/CrateCrate")
            issues("https://github.com/flash-labs/CrateCrate/issues")
        }
        dependency("spongeapi") {
            loadOrder(PluginDependency.LoadOrder.AFTER)
        }
    }
}

val javaTarget = 16 // Sponge targets a minimum of Java 8
java {
    sourceCompatibility = JavaVersion.toVersion(javaTarget)
    targetCompatibility = JavaVersion.toVersion(javaTarget)
    if (JavaVersion.current() < JavaVersion.toVersion(javaTarget)) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(javaTarget))
    }
}

tasks.withType(JavaCompile::class).configureEach {
    options.apply {
        encoding = "utf-8" // Consistent source file encoding
        if (JavaVersion.current().isJava10Compatible) {
            release.set(javaTarget)
        }
    }
}

// Make sure all tasks which produce archives (jar, sources jar, javadoc jar, etc) produce more consistent output
tasks.withType(AbstractArchiveTask::class).configureEach {
    isReproducibleFileOrder = true
    isPreserveFileTimestamps = false
}
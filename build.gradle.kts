import me.drownek.plugwright.PlugwrightRunTask
import me.drownek.plugwright.PlugwrightTestTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.net.HttpURLConnection
import java.net.URI
import java.util.Base64

plugins {
    kotlin("jvm") version "2.3.0"
    id("java")
    id("java-library")
    id("maven-publish")
    id("com.gradleup.shadow") version "9.3.1"
    id("com.willfp.libreforge-gradle-plugin") version "2.0.0"
    id("io.github.drownek.plugwright") version "2.0.3"
}

group = "com.exanthiax"
version = findProperty("version")!!
val libreforgeVersion = findProperty("libreforge-version")
val ecoVersion = findProperty("eco-version")

base {
    archivesName.set(project.name)
}

dependencies {
    implementation(project(":eco-core:core-plugin"))
}

tasks.register<Copy>("dist") {
    dependsOn("libreforgeJar")
    from(tasks.named("libreforgeJar").map { it.outputs.files })
    into(layout.projectDirectory.dir("dist"))
    rename { "${rootProject.name}-${project.version}.jar" }
}

// The runnable eco plugin jar lives in maven-private. The copy in maven-public is
// the shaded API jar: no plugin.yml, so Paper cannot load it. Plugwright's own
// downloadPlugins can't be used here — it has no auth support, and it logs the URL,
// which would print credentials into CI logs.
val mavenUsername = providers.environmentVariable("MAVEN_USERNAME")
val mavenPassword = providers.environmentVariable("MAVEN_PASSWORD")
val hasMavenCredentials = mavenUsername.isPresent && mavenPassword.isPresent

val ecoPluginJar = layout.buildDirectory.file("test-plugins/eco.jar")

val downloadEcoPlugin = tasks.register("downloadEcoPlugin") {
    group = "verification"
    description = "Downloads the runnable eco plugin jar from the private Auxilor repository."

    inputs.property("ecoVersion", ecoVersion.toString())
    outputs.file(ecoPluginJar)

    // compileJava dependsOn(clean), so libreforgeJar drags `clean` into the graph and
    // it would otherwise wipe build/ — and this download with it — after we fetch it.
    mustRunAfter(tasks.named("clean"))

    onlyIf {
        hasMavenCredentials.also {
            if (!it) logger.warn("MAVEN_USERNAME/MAVEN_PASSWORD not set — skipping eco download.")
        }
    }

    doLast {
        val version = ecoVersion.toString()
        val target = ecoPluginJar.get().asFile
        target.parentFile.mkdirs()

        val auth = Base64.getEncoder()
            .encodeToString("${mavenUsername.get()}:${mavenPassword.get()}".toByteArray())

        val url = "https://repo.auxilor.io/repository/maven-private/" +
            "com/willfp/eco/$version/eco-$version-all.jar"

        val connection = (URI(url).toURL().openConnection() as HttpURLConnection).apply {
            setRequestProperty("Authorization", "Basic $auth")
            connectTimeout = 30_000
            readTimeout = 300_000
        }

        if (connection.responseCode != 200) {
            connection.disconnect()
            throw GradleException(
                "Failed to download eco $version from maven-private: HTTP ${connection.responseCode}. " +
                    "Check MAVEN_USERNAME/MAVEN_PASSWORD."
            )
        }

        connection.inputStream.use { input -> target.outputStream().use(input::copyTo) }
        connection.disconnect()

        logger.lifecycle("Resolved eco plugin: eco-$version-all.jar")
    }
}

plugwright {
    // Pinned to 1.21.8, the newest version eco can actually run on. eco is
    // Spigot-mapped and relies on Paper's plugin remapper to rewrite the
    // org.bukkit.craftbukkit.v1_21_R7 references in its NMS proxies. That remapper
    // is broken from 1.21.9 (PaperMC/Paper#13131) and removed outright in 26.1, so
    // eco fails to initialise on anything newer.
    // 1.21.8 also matches our api-version and is supported by mineflayer.
    minecraftVersion.set("1.21.8")
    testsDir.set(file("src/test/e2e"))
    acceptEula.set(true)

    // libreforge is already shaded into our own jar, so eco is the only runtime dep.
    writeFiles {
        file("plugins/eco.jar", ecoPluginJar.get().asFile)
    }
}

// Plugwright auto-detects `shadowJar`, but the jar we actually ship comes from
// `libreforgeJar`. Registered after the plugin's own afterEvaluate, so this wins.
afterEvaluate {
    val libreforgeJar = tasks.named("libreforgeJar")

    // The test server can't start without eco, so both tasks sit behind the
    // credentials that fetch it. Without them the build still runs, minus Plugwright.
    tasks.named<PlugwrightTestTask>("plugwrightTest") {
        dependsOn(libreforgeJar, downloadEcoPlugin)
        pluginJar.set(libreforgeJar.map { it.outputs.files.singleFile })
        onlyIf {
            hasMavenCredentials.also {
                if (!it) logger.warn("MAVEN_USERNAME/MAVEN_PASSWORD not set — skipping Plugwright.")
            }
        }
    }

    tasks.named<PlugwrightRunTask>("plugwrightRunServer") {
        dependsOn(libreforgeJar, downloadEcoPlugin)
        pluginJar.set(libreforgeJar.map { it.outputs.files.singleFile })
        onlyIf {
            hasMavenCredentials.also {
                if (!it) logger.warn("MAVEN_USERNAME/MAVEN_PASSWORD not set — skipping Plugwright.")
            }
        }
    }
}

java {
    withJavadocJar()
}

publishing {
    publications {
        // maven-private: only the shaded jar
        create<MavenPublication>("private") {
            artifactId = rootProject.name
        }
        // maven-releases + GitHub: full set (none, all, sources, javadoc)
        create<MavenPublication>("release") {
            artifactId = rootProject.name
            from(components["java"])
        }
    }
    repositories {
        maven {
            name = "Auxilor"
            url = uri("https://repo.auxilor.io/repository/maven-private/")
            credentials {
                username = System.getenv("MAVEN_USERNAME")
                password = System.getenv("MAVEN_PASSWORD")
            }
        }
        maven {
            name = "AuxilorReleases"
            url = uri("https://repo.auxilor.io/repository/maven-releases/")
            credentials {
                username = System.getenv("MAVEN_USERNAME")
                password = System.getenv("MAVEN_PASSWORD")
            }
        }
    }
}

afterEvaluate {
    publishing.publications.named<MavenPublication>("private") {
        artifact(tasks.named("libreforgeJar"))
    }
}

tasks.matching { it.name.startsWith("generatePomFileFor") }.configureEach {
    mustRunAfter(tasks.named("clean"))
}
tasks.register("publishToAuxilor") {
    dependsOn(
        "publishPrivatePublicationToAuxilorRepository",
        "publishReleasePublicationToAuxilorReleasesRepository",
    )
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "kotlin")
    apply(plugin = "maven-publish")
    apply(plugin = "com.gradleup.shadow")

    repositories {
        mavenLocal()
        mavenCentral()

        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://repo.auxilor.io/repository/maven-public/")
        maven("https://repo.codemc.org/repository/nms/")
        maven("https://jitpack.io")
    }

    dependencies {
        compileOnly("com.willfp:eco:$ecoVersion")
        compileOnly("org.jetbrains:annotations:26.0.2")
        compileOnly("org.jetbrains.kotlin:kotlin-stdlib:2.3.0")
    }

    tasks {
        shadowJar {
            exclude("META-INF/**")
            relocate("com.willfp.libreforge.loader", "com.exanthiax.ecocollections.libreforge.loader")
            relocate("com.willfp.ecomponent", "com.exanthiax.ecocollections.ecomponent")
            relocate("kotlin", "com.willfp.eco.libs.kotlin")
            relocate("kotlin.jvm", "com.willfp.eco.libs.kotlin.jvm")
            relocate("kotlin.coroutines", "com.willfp.eco.libs.kotlin.coroutines")
            relocate("kotlin.reflect", "com.willfp.eco.libs.kotlin.reflect")
        }

        compileKotlin {
            compilerOptions {
                jvmTarget = JvmTarget.JVM_21
            }
        }

        compileJava {
            options.isDeprecation = true
            options.encoding = "UTF-8"

            dependsOn(clean)
        }

        processResources {
            filesMatching(listOf("**plugin.yml", "**eco.yml")) {
                expand(
                    "version" to project.version,
                    "libreforgeVersion" to libreforgeVersion!!,
                    "pluginName" to rootProject.name
                )
            }
        }

        build {
            dependsOn(shadowJar)
        }

        withType<JavaCompile>().configureEach {
            options.release = 21
        }
    }

    java {
        withSourcesJar()
        toolchain {
            languageVersion = JavaLanguageVersion.of(25)
        }
    }
}

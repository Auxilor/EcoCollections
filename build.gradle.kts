import me.drownek.plugwright.PlugwrightRunTask
import me.drownek.plugwright.PlugwrightTestTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

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

plugwright {
    // 1.21.11 is the newest release mineflayer (via minecraft-data) can speak.
    // Paper 26.x needs protocol 776, which prismarine hasn't shipped data for yet.
    minecraftVersion.set("1.21.11")
    testsDir.set(file("src/test/e2e"))
    acceptEula.set(true)

    downloadPlugins {
        // eco is a hard depend; libreforge is already shaded into our own jar.
        url("https://repo.auxilor.io/repository/maven-public/com/willfp/eco/$ecoVersion/eco-$ecoVersion-all.jar")
    }
}

// Plugwright auto-detects `shadowJar`, but the jar we actually ship comes from
// `libreforgeJar`. Registered after the plugin's own afterEvaluate, so this wins.
afterEvaluate {
    val libreforgeJar = tasks.named("libreforgeJar")

    tasks.named<PlugwrightTestTask>("plugwrightTest") {
        dependsOn(libreforgeJar)
        pluginJar.set(libreforgeJar.map { it.outputs.files.singleFile })
    }

    tasks.named<PlugwrightRunTask>("plugwrightRunServer") {
        dependsOn(libreforgeJar)
        pluginJar.set(libreforgeJar.map { it.outputs.files.singleFile })
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

import java.text.SimpleDateFormat
import java.util.*

buildscript {
    repositories {
        mavenCentral()
        maven(url = "https://maven.fabricmc.net/")
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.0-Beta")
        classpath("org.spongepowered:mixingradle:0.7.+")
    }
}

apply(plugin = "kotlin")
apply(plugin = "org.spongepowered.mixin")

plugins {
    java
    idea
    `maven-publish`
    id("net.minecraftforge.gradle") version "[6.0,6.2)"
    id("org.jetbrains.kotlin.jvm") version "1.8.22"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.8.22"
    id("org.parchmentmc.librarian.forgegradle") version "1.+"
}

version = "1.20-0.1.0"
group = "com.ebicep"

val modid = "chatplus"
val vendor = "ebicep"

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))

println(
    "Java: " + System.getProperty("java.version") + " JVM: " + System.getProperty("java.vm.version") + "(" + System.getProperty(
        "java.vendor"
    ) + ") Arch: " + System.getProperty("os.arch")
)

minecraft {
    mappings("parchment", "2023.06.26-1.20.1")

    //accessTransformer(file("src/main/resources/META-INF/accesstransformer.cfg"))

    runs.all {
        mods {
            workingDirectory(project.file("run"))
            property("forge.logging.markers", "REGISTRIES")
            property("forge.logging.console.level", "debug")
            property("forge.enabledGameTestNamespaces", modid)
            property("terminal.jline", "true")
            mods {
                create(modid) {
                    source(sourceSets.main.get())
                }
            }
        }
    }

    runs.run {
        create("client") {
            property("log4j.configurationFile", "log4j2.xml")

            // for hotswap https://forge.gemwire.uk/wiki/Hotswap
            //jvmArg("-XX:+AllowEnhancedClassRedefinition")
        }
    }
}

sourceSets.main.configure { resources.srcDirs("src/generated/resources/") }

configurations {
    minecraftLibrary {
        exclude("org.jetbrains", "annotations")
    }
}

repositories {
    mavenCentral()
    maven {
        name = "Kotlin for Forge"
        url = uri("https://thedarkcolour.github.io/KotlinForForge/")
    }
    maven {
        url = uri("https://repo.essential.gg/repository/maven-public")
    }
}

dependencies {
    minecraft("net.minecraftforge:forge:1.20-46.0.1")
    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")
    implementation("thedarkcolour:kotlinforforge:4.3.0")
    implementation("gg.essential:vigilance-1.18.1-forge:284")
}

val Project.mixin: org.spongepowered.asm.gradle.plugins.MixinExtension
    get() = extensions.getByType()

mixin.run {
    add(sourceSets.main.get(), "chatplus.mixins.refmap.json")
    config("chatplus.mixins.json")
    val debug = this.debug as org.spongepowered.asm.gradle.plugins.struct.DynamicProperties
    debug.setProperty("verbose", true)
    debug.setProperty("export", true)
    setDebug(debug)
}

tasks.withType<Jar> {
    archiveBaseName.set(modid)
    manifest {
        val map = HashMap<String, String>()
        map["Specification-Title"] = modid
        map["Specification-Vendor"] = vendor
        map["Specification-Version"] = "1"
        map["Implementation-Title"] = project.name
        map["Implementation-Version"] = project.version.toString()
        map["Implementation-Vendor"] = vendor
        map["Implementation-Timestamp"] = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(Date())
        attributes(map)
    }
    finalizedBy("reobfJar")
}
tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
    }
}
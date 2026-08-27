plugins {
    java
    alias(libs.plugins.fabric.loom)
    alias(libs.plugins.minotaur)
}

val modrinthProjectID = property("modrinth.id").toString()
val modID = property("mod.id").toString()
val modGroup = property("mod.group").toString()
val version = property("mod.version").toString()

base.archivesName = "${modID}-${version}"

loom {
    splitEnvironmentSourceSets()

    runs {
        all {
            jvmArguments.addAll("-XX:+AllowEnhancedClassRedefinition", "-XX:+UseG1GC")
        }

        named("client") {
            jvmArguments.addAll("-Ddevauth.enabled=true")
        }

        //remove(runs["server"])
    }

    mods.create(modID) {
        sourceSet(sourceSets.getByName("main"))
        sourceSet(sourceSets.getByName("client"))
    }

    accessWidenerPath = file("src/main/resources/$modID.classtweaker")
}

repositories {
    mavenCentral()

    exclusiveContent {
        forRepository { maven("https://api.modrinth.com/maven") { name = "Modrinth" } }
        filter { includeGroup("maven.modrinth") }
    }

    exclusiveContent {
        forRepository { maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1") }
        filter { includeGroup("me.djtheredstoner") }
    }
}

dependencies {
    minecraft(libs.minecraft)

    implementation(libs.fabric.loader)
    implementation(libs.fabric.api)

    localRuntime(libs.devAuth)
    localRuntime(libs.modMenu)
    localRuntime(libs.noChatRestrictions)
    localRuntime(libs.sodium)
}

val resourcesVars = mapOf(
    "mod_id" to modID,
    "mod_group" to modGroup,
    "mod_version" to version,
    "fabric_loader_version" to libs.versions.fabric.loader.get(),
    "fabric_api_version" to libs.versions.fabric.api.get(),
    "minecraft_version" to libs.versions.minecraft.get(),
)

tasks.withType<ProcessResources> {
    inputs.properties(resourcesVars)
    filesMatching(listOf("fabric.mod.json", "*.mixins.json")) { expand(resourcesVars) }
}

java {
    withSourcesJar()

    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release = 25
}

tasks.jar { from("LICENSE") { rename { "${it}_${base.archivesName.get()}" } } }

modrinth {
    token = System.getenv("MODRINTH_TOKEN")
    projectId = modrinthProjectID
    versionNumber = version
    versionType = "release"
    uploadFile.set(tasks.jar)
    gameVersions.addAll(libs.versions.minecraft.get())
    loaders.add("fabric")
    dependencies {
        required.project("fabric-api")
    }
    syncBodyFrom = rootProject.file("README.md").readText()
}

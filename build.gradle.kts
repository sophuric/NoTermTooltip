plugins {
    java
    alias(libs.plugins.fabric.loom)
    alias(libs.plugins.modPublishPlugin)
}

val modID = property("mod.id").toString()
val modName = property("mod.name").toString()
val modGroup = property("mod.group").toString()
val version = property("mod.version").toString()
val githubRepo = property("repo.github").toString()

base.archivesName = "${modID}-${version}"

loom {
    splitEnvironmentSourceSets()

    runs {
        all {
            jvmArguments.addAll("-XX:+AllowEnhancedClassRedefinition", "-XX:+UseG1GC")
        }

        remove(runs["server"])
    }

    mods.create(modID) {
        sourceSet(sourceSets.getByName("client"))
    }

    accessWidenerPath = file("src/client/resources/$modID.classtweaker")
}

repositories {
    mavenCentral()

    exclusiveContent {
        forRepository { maven("https://repo.hypixel.net/repository/Hypixel/") { name = "Hypixel" } }
        filter { includeGroup("net.hypixel") }
    }

    exclusiveContent {
        forRepository { maven("https://api.modrinth.com/maven") { name = "Modrinth" } }
        filter { includeGroup("maven.modrinth") }
    }
}

dependencies {
    minecraft(libs.minecraft)

    implementation(libs.fabric.loader)
    implementation(libs.fabric.api)
    implementation(libs.hypixelModAPI)

    localRuntime(libs.hypixelModAPI.mod)
    localRuntime(libs.modMenu)
    localRuntime(libs.noChatRestrictions)
    localRuntime(libs.sodium)
}

val resourcesVars = mapOf(
    "mod_id" to modID,
    "mod_name" to modName,
    "mod_group" to modGroup,
    "mod_version" to version,
    "repo_github" to githubRepo,
    "fabric_loader_version" to libs.versions.fabric.loader.get(),
    "fabric_api_version" to libs.versions.fabric.api.get(),
    "minecraft_version" to libs.versions.minecraft.get(),
    "hypixel_mod_api_version" to libs.versions.hypixelModAPI.mod.get(),
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

publishMods.version = version
publishMods {
    file = tasks.jar.get().archiveFile
    modLoaders.add("fabric")

    type = STABLE
    displayName = "$modName $version for Minecraft ${libs.versions.minecraft.get()}"

    changelog = providers.fileContents(layout.projectDirectory.file("CHANGELOG.md")).asText

    modrinth {
        accessToken = providers.environmentVariable("MODRINTH_TOKEN")

        projectId = property("modrinth.id").toString()
        minecraftVersions.add(libs.versions.minecraft.get())

        requires("fabric-api")
    }

    github {
        accessToken = providers.environmentVariable("GITHUB_TOKEN")

        repository = githubRepo
        tagName = version
        commitish = providers.environmentVariable("GIT_BRANCH")
    }
}

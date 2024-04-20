plugins {
    id("com.palantir.git-version") version "3.0.0"
    val kotlinVersion by System.getProperties()
    kotlin("jvm") version kotlinVersion.toString()
    id("jacoco")
    id("maven-publish")
    id("org.jetbrains.dokka") version "1.9.20" apply false
}

// declare repositories in which to find dependencies (in a reusable way, since we need it twice)
val repoConfig: RepositoryHandler.() -> Unit = {
    mavenCentral()
}

// compileKotlin task appear to expect a top-level repositories configuration
repositories(repoConfig)

// set the project version from tags and commits in Git repository
val gitVersion: groovy.lang.Closure<String> by extra
version = gitVersion()

// We have to declare (and apply!) 'java-library' and Kotlin plugins above (in the top-level project) if we want to
// configure them (nicely) below in the subprojects section.  So, the following task configs are only here to suppress
// top-level build artifacts that are a side effect of doing this.
tasks {
    jar {
        enabled = false
    }
}

kotlin {
    jvmToolchain(21)
}

//
// configuration shared by all subprojects (does not include the root project)
//
subprojects {

    // Apply the java-library plugin to add support for Java Library
    apply(plugin = "java-library")
    apply(plugin = "jacoco")
    apply(plugin = "kotlin")
    apply(plugin = "maven-publish")
    apply(plugin = "org.jetbrains.dokka")

    // use the version from the rootProject (which we already determined via git)
    version = rootProject.version

    // declare repositories in which to find dependencies
    repositories(repoConfig)

    dependencies {
        // implementation dependencies are used internally, and not exposed to consumers on their own compile classpath
        implementation("io.github.microutils:kotlin-logging-jvm:3.0.5") // up-to-date as of 2024-04-20
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")

        // Use JUnit test framework
        testImplementation("org.junit.jupiter:junit-jupiter:5.10.2") // up-to-date as of 2024-04-20
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")

        // note that since this is a logging IMPLEMENTATION, it should ONLY be on the test classpath
        // clients of this library will provide their own SLF4J logging implementation
        testRuntimeOnly("ch.qos.logback:logback-classic:1.5.6") // up-to-date as of 2024-04-20
    }

    tasks {

        // build a tests jar
        register<Jar>("testJar") {
            description = "Assembles a jar archive containing the test classes and source code."
            group = "Build"
            duplicatesStrategy = DuplicatesStrategy.EXCLUDE
            from(sourceSets["test"].allSource)
            from(sourceSets["test"].output)
            archiveClassifier.set("tests")
        }

        jar {
            manifest {
                attributes("Implementation-Title" to project.name, "Implementation-Version" to project.version)
            }
        }

        test {
            useJUnitPlatform()
        }

        check {
            dependsOn(jacocoTestReport)
        }

    }

    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                from(components["java"]) // standard artifact
                artifact(tasks["kotlinSourcesJar"]) // sources artifact
                artifact(tasks["testJar"]) // tests artifact
            }
        }
    }

}

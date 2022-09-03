import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.palantir.git-version") version "0.15.0"
    kotlin("jvm") version "1.7.10"
//    id("java-library")
    id("jacoco")
    id("maven-publish")
    //id("org.jetbrains.dokka") version "0.9.18" apply false
}

// declare repositories in which to find dependencies (in a reusable way, since we need it twice)
val repoConfig: RepositoryHandler.() -> Unit = {
    mavenCentral()
    mavenLocal()
}

// compileKotlin task appear to expect a top-level repositories configuration
repositories(repoConfig)

// set the project version from tags and commits in Git repository
val gitVersion: groovy.lang.Closure<String> by extra
version = gitVersion()

// We have to declare (and apply!) 'java-library' and Kotlin plugins above (in the top-level project) if we want to
// configure them (nicely) below in the subprojects section.  So, the following task configs are only here to suppress
// top-level build artifacts that are a side-effect of doing this.
tasks {
    jar {
        enabled = false
    }
    inspectClassesForKotlinIC {
        enabled = false
    }
}

//kotlin {
//    jvmToolchain {
//        (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(18))
//    }
//}

//
// configuration shared by all subprojects (does not include the root project)
//
subprojects {

    // Apply the java-library plugin to add support for Java Library
    apply(plugin = "java-library")
    apply(plugin = "jacoco")
    apply(plugin = "kotlin")
    apply(plugin = "maven-publish")
    //apply(plugin = "org.jetbrains.dokka")

    // use the version from the rootProject (which we already determined via git)
    version = rootProject.version

    // declare repositories in which to find dependencies
    repositories(repoConfig)

    dependencies {
        val kotlinCoroutinesVersion: String by project

        // implementation dependencies are used internally, and not exposed to consumers on their own compile classpath
        implementation("io.github.microutils:kotlin-logging:2.1.23") // up-to-date as of 2022-08-13
        implementation(kotlin("stdlib-jdk8"))
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$kotlinCoroutinesVersion")

        // Use JUnit test framework
        testImplementation("org.junit.jupiter:junit-jupiter:5.9.0")

        // note that since this is a logging IMPLEMENTATION, it should ONLY be on the test classpath
        // clients of this library will provide their own SLF4J logging implementation
        testRuntimeOnly("ch.qos.logback:logback-classic:1.2.3") // up-to-date as of 2018-10-03

    }

//    java {
//        val targetJavaVersion: String by project
//        val targetJavaVersionObj = JavaVersion.toVersion(targetJavaVersion)
//        sourceCompatibility = targetJavaVersionObj
//        targetCompatibility = targetJavaVersionObj
//    }

    tasks {

        /*
         * Some Kotlin compiler configuration
         */
        val kotlinJvmTargetVersion: String by project
        withType<KotlinCompile>().configureEach {
            kotlinOptions {
                jvmTarget = kotlinJvmTargetVersion
            }
        }

        // build a tests jar
        register<Jar>("testJar") {
            description = "Assembles a jar archive containing the test classes and source code."
            group = "Build"
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

        //dokka {
        //    outputFormat = "html"
        //    outputDirectory = "$buildDir/javadoc"
        //}

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

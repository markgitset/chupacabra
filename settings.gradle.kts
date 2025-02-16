plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version("0.8.0")
}

rootProject.name = "chupacabra"
include(
    "chupacabra-core",
    "chupacabra-gson",
    "chupacabra-guava",
    "chupacabra-cli"
)
//val kotlinVersion: String by settings
//pluginManagement {
//    plugins {
//        kotlin("jvm") version kotlinVersion
//    }
//}

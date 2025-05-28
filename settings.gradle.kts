plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version("1.0.0")
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

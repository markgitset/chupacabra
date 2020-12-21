dependencies {

    // api dependencies are exported to consumers, i.e., found on their compile classpath
    api(project(":chupacabra-core"))
    api("com.google.code.gson:gson:2.8.5")

    // guava"s an optional dependency--only required if you use guava features of this library
    compileOnly("com.google.guava:guava:24.0-jre")
    testImplementation("com.google.guava:guava:24.0-jre") 

}

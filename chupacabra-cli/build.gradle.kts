dependencies {

    // api dependencies are exported to consumers, i.e., found on their compile classpath
    api(project(":chupacabra-guava"))
    api("me.tongfei:progressbar:0.7.2")
    api("com.github.ajalt:clikt:2.6.0")
    api("org.jline:jline:3.13.2")
    //implementation("ch.qos.logback:logback-classic:1.2.3")

}

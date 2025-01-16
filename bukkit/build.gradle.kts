repositories {
    maven {
        url = uri("http://sacredcraft.cn:8081/repository/releases")
        isAllowInsecureProtocol = true
    }
}

dependencies {
    compileOnly(project(":common"))
    compileOnly("ink.ptms.core:v12004:12004:mapped")
    compileOnly("ink.ptms.core:v12004:12004:universal")

    testImplementation(project(":common"))
    testImplementation("ink.ptms.core:v12004:12004:universal")
}

tasks.jar {
    from(project(":common").sourceSets["main"].output)
}
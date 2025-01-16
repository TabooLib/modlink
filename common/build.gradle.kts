dependencies {
    implementation("io.netty:netty-all:4.1.86.Final")
    testImplementation("io.netty:netty-all:4.1.86.Final")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")
}

tasks.test {
    useJUnitPlatform()
}
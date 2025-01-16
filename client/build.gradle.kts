dependencies {
    compileOnly(project(":common"))
}

tasks.jar {
    from(project(":common").sourceSets["main"].output)
}
@file:Suppress("PropertyName", "SpellCheckingInspection")

import org.gradle.kotlin.dsl.get
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import kotlin.toString

plugins {
    java
    `maven-publish`
    id("org.jetbrains.kotlin.jvm") version "1.8.22" apply false
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "maven-publish")
    apply(plugin = "org.jetbrains.kotlin.jvm")

    // 仓库
    repositories {
        mavenCentral()
    }

    // 依赖
    dependencies {
        compileOnly(kotlin("stdlib"))
    }

    // 编译配置
    java {
        targetCompatibility = JavaVersion.VERSION_1_8
        withSourcesJar()
        withJavadocJar()
    }
    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
            freeCompilerArgs = listOf("-Xjvm-default=all", "-Xextended-compiler-checks")
        }
    }

    // 发布配置
    publishing {
        publications {
            create<MavenPublication>("maven") {
                from(components["java"])
                groupId = rootProject.group.toString()
                artifactId = project.name
                version = rootProject.version.toString()
                println("Publish $groupId:$artifactId:$version")
            }
        }
        repositories {
            mavenLocal()
            maven {
                name = "TabooLib"
                url = uri("http://sacredcraft.cn:8081/repository/releases")
                credentials {
                    username = project.findProperty("taboolibUsername").toString()
                    password = project.findProperty("taboolibPassword").toString()
                }
                authentication {
                    create<BasicAuthentication>("basic")
                }
            }
        }
    }
}

gradle.buildFinished {
    buildDir.deleteRecursively()
}
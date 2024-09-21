/*
 * MIT License
 * Copyright 2024 Nils Jäkel
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the “Software”),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software.
 */

import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    org.jetbrains.kotlin.multiplatform
    org.jetbrains.dokka
}

version = System.getenv(/* name = */ "CI_COMMIT_TAG") ?: System.getenv(/* name = */ "CI_COMMIT_SHORT_SHA")?.let { "$it-dev" } ?: "0.0.0-DEV"

repositories {
    mavenCentral()
}

val jvmTargetVersion = JvmTarget.JVM_1_8
val targetJavaVersion = JavaVersion.VERSION_1_8
val dokkaConfiguration = extensions.create("dokkaConfiguration", DokkaConfiguration::class)

kotlin {
    explicitApi()
    withSourcesJar()

    jvm {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions.jvmTarget = jvmTargetVersion
    }

//    linuxX64 {
//        compilations {
//            getByName("main") {
//                cinterops {
//                    create("moktCppBindings") {
//                        defFile(Path("/home/nils/Development/Redtronics/Mokt/native-cinterop/aarch64-unknown-linux-gnu.def"))
//                        packageName("moktcpp")
//                    }
//                }
//            }
//        }
//    }
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        targetCompatibility = targetJavaVersion.toString()
        sourceCompatibility = targetJavaVersion.toString()
    }

    withType<KotlinCompile> {
        compilerOptions {
            jvmTarget = jvmTargetVersion
        }
    }

    withType<Test> {
        useJUnitPlatform()
        reports.junitXml.required = true
        systemProperty("gradle.build.dir", layout.buildDirectory.get().asFile.absolutePath)

        filter {
            isFailOnNoMatchingTests = false
        }

        testLogging {
            showExceptions = true
            showStandardStreams = true
            events = setOf(TestLogEvent.FAILED, TestLogEvent.PASSED, TestLogEvent.SKIPPED)
            exceptionFormat = TestExceptionFormat.FULL
        }
    }

    dokkaHtml {
        moduleName = dokkaConfiguration.name.getOrElse(Project.NAME)
        description = dokkaConfiguration.description.getOrElse(Project.DESCRIPTION)
        failOnWarning = true
    }
}
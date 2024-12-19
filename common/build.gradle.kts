
/*
 * MIT License
 * Copyright 2024 Nils Jäkel & David Ernst
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the “Software”),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software.
 */

import net.davils.kreate.feature.cinterop.Target

plugins {
    `mokt-multiplatform`
}

repositories {
    mavenCentral()
}

group = Project.GROUP

kreate {
    cinterop {
        enabled = true
        cBindVersion = "0.27.0"
        libCVersion = "0.2.169"
        applyTargetsWithoutRust = false
        targets(
            Target.LINUX,
            Target.WINDOWS,
            Target.IOS,
            Target.TVOS,
            Target.MACOS,
            Target.WATCHOS
        )
    }
}


kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(libs.kotlinx.coroutines.core)
                api(libs.kotlinx.coroutines.debug)

                api(libs.ktor.serialization.json)
                api(libs.ktor.client.core)
                api(libs.ktor.client.logging)
                api(libs.ktor.client.content.negotiation)

                api(libs.kotlin.reflect)
                api(libs.kotlinx.serialization.json)
                api(libs.kotlinx.datetime)
            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotest.assertions.core)
                implementation(libs.kotest.framework.engine)
                implementation(libs.kotest.property)
            }
        }

        jvmMain {
            dependencies {
                api(libs.kotlinx.coroutines.reactive)
                api(libs.ktor.client.cio)

                api(libs.slf4j.api)
                api(libs.logback.classic)
            }
        }

        jvmTest {
            dependencies {
                implementation(libs.kotest.runner.junit5)
            }
        }

        linuxMain {
            dependencies {
                api(libs.ktor.client.cio)
            }
        }

        mingwMain {
            dependencies {
                api(libs.ktor.client.winhttp)
            }
        }

        iosMain {
            dependencies {
                api(libs.ktor.client.darwin)
            }
        }

        macosMain {
            dependencies {
                api(libs.ktor.client.darwin)
            }
        }

        tvosMain {
            dependencies {
                api(libs.ktor.client.darwin)
            }
        }

        watchosMain {
            dependencies {
                api(libs.ktor.client.darwin)
            }
        }
    }
}
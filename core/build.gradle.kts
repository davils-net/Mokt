/*
 * MIT License
 * Copyright 2024 Davils
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the "Software”),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software.
 */

import net.davils.kreate.feature.cinterop.Target

plugins {
    `mokt-multiplatform`
}

group = Project.GROUP

kreate {
    cinterop {
        enabled = true
        applyTargetsWithoutRust = true
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
                implementation(project(":common"))
            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotest.assertions.core)
                implementation(libs.kotest.framework.engine)
                implementation(libs.kotest.property)
            }
        }

        jvmTest {
            dependencies {
                implementation(libs.kotest.runner.junit5)
            }
        }
    }
}

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

import net.davils.kreate.feature.core.License

plugins {
    org.jetbrains.kotlin.plugin.serialization
    org.jetbrains.kotlinx.atomicfu
    org.jetbrains.kotlin.multiplatform
    net.davils.kreate
}

repositories {
    mavenCentral()
}

kreate {
    core {
        name = Project.NAME
        description = Project.DESCRIPTION
        license = License.MIT
        allWarningsAsErrors = true
        isExplicitApiMode = true
    }

    publish {
        enabled = true
        inceptionYear = 2024
    }

    testing {
        enabled = true
        createTestReport = true
    }

    docs {
        enabled = true
        isMultiModuleMode = true
    }

    buildConstants {
        enabled = true
        buildPath = "generated/templates"
        onlyInternal = true
        sourceSets = kotlin.sourceSets.getByName("commonMain")
    }

    jv {
        enabled = true
        javaVersion = JavaVersion.VERSION_1_8
        withJavadocJar = true
        withSourcesJar = true
    }
}

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

import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile

plugins {
    idea
    org.jetbrains.gradle.plugin.`idea-ext`
}

tasks {
    val task = register("generateCInteropDefFiles") {
        group = Project.NAME.lowercase()
        description = "Generates cinterop def files for all supported operating system platforms."

        project.patchVersion()
        project.compileRustBindings()
        project.generateCInteropDefFiles()
    }
    onSyncExec(project, task.get(), rootProject.idea)

    withType<KotlinNativeCompile> {
        dependsOn("commonizeCInterop")
    }
}

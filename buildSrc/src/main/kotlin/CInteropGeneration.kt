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

import org.gradle.api.NamedDomainObjectCollection
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.gradle.api.Project as GradleProject
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTargetWithHostTests
import java.nio.file.Path

/**
 * Compiles the C++ bindings for the cinterop.
 *
 * @since 0.0.1
 * @author Nils Jäkel
 */
internal fun GradleProject.compileRustBindings() {
    val workDir = file("../mokt-rust-bindings")

    exec {
        workingDir = workDir
        commandLine = listOf("cargo", "build", "--release")
    }
}

/**
 * Generates cinterop def files for all supported operating system platforms.
 *
 * @since 0.0.1
 * @author Nils Jäkel
 */
internal fun GradleProject.generateCInteropDefFiles() {
    val rustBindingsDir = file("../mokt-rust-bindings")
    val includeDir = rustBindingsDir.resolve("include")
    val nativeCInteropDir = file("../native-cinterop")

    val hFiles = includeDir.list()?.joinToString(" ") ?: throw Exception("No include files found")

    val defFile = nativeCInteropDir.resolve("cinterop.def")
    if (!defFile.exists()) {
        defFile.createNewFile()
    }

    when (os) {
        OsType.WINDOWS -> {
            defFile.writeText(
                """
                    headers = $hFiles
                    staticLibraries = ${Project.NAME.lowercase()}_rust_bindings.lib
                    compilerOpts = -I${includeDir.absolutePath.replace("\\", "/")}
                    libraryPaths = ${rustBindingsDir.resolve("target/release").absolutePath.replace("\\", "/")}
                """.trimIndent()
            )
        }

        else -> {
            defFile.writeText(
                """
                    headers = $hFiles
                    staticLibraries = libmokt_rust_bindings.a
                    compilerOpts = -I$includeDir
                    libraryPaths = ${rustBindingsDir.resolve("target/release")}
                """.trimIndent()
            )
        }
    }
}


/**
 * Applies the cinterop generation for the given [architectures] to the given [KotlinNativeTargetWithHostTests].
 *
 * @since 0.0.1
 * @author Nils Jäkel
 */
fun KotlinNativeTargetWithHostTests.applyCInteropGeneration(path: Path) {
    compilations.getByName("main") {
        cinterops {
            create("moktRustBindings") {
                defFile(path)
                packageName("${Project.GROUP}.cinterop")
            }
        }
    }
}

fun KotlinNativeTarget.applyCInteropGeneration(path: Path) {
    compilations.getByName("main") {
        cinterops {
            create("moktRustBindings") {
                defFile(path)
                packageName("${Project.GROUP}.cinterop")
            }
        }
    }
}

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

package dev.redtronics.mokt

public actual fun getEnv(key: String): String? {
    val value = js("process.env[key]") as? String ?: return null
    return value
}

public actual val os: OsType by lazy {
    val platform = js("navigator.platform").toString().lowercase()
    when {
        platform.contains("win") -> OsType.WINDOWS
        platform.contains("android") -> OsType.ANDROID
        platform.contains("linux") || platform.contains("aix") -> OsType.LINUX
        else -> OsType.UNKNOWN
    }
}

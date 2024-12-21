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

package net.davils.mokt

public expect fun getEnv(key: String): String?

public enum class OsType(public val value: String) {
    WINDOWS("windows"),
    LINUX("linux"),
    ANDROID("android"),
    MACOS("macos"),
    TVOS("tvos"),
    WATCHOS("watchos"),
    IOS("ios"),
    UNKNOWN("unknown");

    public companion object {
        public fun byName(name: String): OsType = entries.firstOrNull { it.value == name } ?: UNKNOWN
    }
}

public expect val os: OsType

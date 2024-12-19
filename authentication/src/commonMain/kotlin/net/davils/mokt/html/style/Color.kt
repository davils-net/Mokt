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

package net.davils.mokt.html.style

public class Color(private val hex: String) {
    init {
        require(hex.startsWith("#") && hex.length == 7) { "Invalid hex color: $hex" }
    }
    public fun hex(): String = hex
}

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

package net.davils.mokt.network

import io.ktor.http.*
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import net.davils.cinterop.open_url

@OptIn(ExperimentalForeignApi::class)
public actual suspend fun openInBrowser(url: Url): Unit = withContext(Dispatchers.IO) {
    open_url(url.toString())
}
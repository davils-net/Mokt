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

package net.davils.mokt.builder.mojang

import io.ktor.client.*
import io.ktor.client.statement.*
import kotlinx.serialization.json.Json

/**
 * Base class for all mojang authentication builders.
 *
 * @since 0.0.1
 * @author Nils Jäkel
 * */
public abstract class GameAuthBuilder {
    /**
     * The http client from the configured provider.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    internal abstract val httpClient: HttpClient

    /**
     * The json serializer instance from the configured provider.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    internal abstract val json: Json

    /**
     * The method to request the authentication data from the provider.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    internal abstract suspend fun requestAuthData(): HttpResponse
}

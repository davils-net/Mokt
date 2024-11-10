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

package dev.redtronics.mokt.payload

import kotlinx.serialization.Serializable

/**
 * Represents the payload for the Mojang endpoint.
 *
 * @property identityToken The identity token of the payload.
 *
 * @since 0.0.1
 * @author Nils Jäkel
 * */
@Serializable
internal data class MojangPayload(
    val identityToken: String
)

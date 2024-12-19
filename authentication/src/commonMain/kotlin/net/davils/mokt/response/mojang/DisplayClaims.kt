/*
 * MIT License
 * Copyright 2024 Nils Jäkel & David Ernst
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the "Software”),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software.
 */

package net.davils.mokt.response.mojang

import kotlinx.serialization.Serializable

/**
 * Represents the Display Claims from the auth endpoints.
 *
 * @property xui A [List] of User Hash Strings.
 *
 * @since 0.0.1
 * @author Nils Jäkel
 * */
@Serializable
public data class DisplayClaims(
    public val xui: List<Uhs>
)

/**
 * Represents the User Hash String (UHS).
 *
 * @property uhs The User Hash String.
 *
 * @since 0.0.1
 * @author Nils Jäkel
 */
@Serializable
public data class Uhs(
    public val uhs: String
)
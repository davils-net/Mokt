/*
 * MIT License
 * Copyright 2024 Nils Jäkel  & David Ernst
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the "Software”),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software.
 */

@file:Suppress("MemberVisibilityCanBePrivate")

package net.davils.mokt.builder.grant

import io.ktor.http.*
import net.davils.mokt.Microsoft

/**
 * Microsoft grant code builder.
 * Configures the code grant flow for the Microsoft provider.
 *
 * @since 0.0.1
 * @author Nils Jäkel
 * */
public class GrantCodeBuilder internal constructor(
    /**
     * The microsoft provider instance.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    override val provider: Microsoft
) : GrantAuth<Microsoft>() {

    /**
     * The authorize endpoint url of the microsoft provider.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    override val authorizeEndpointUrl: Url
        get() = Url("https://login.microsoftonline.com/${provider.tenant.value}/oauth2/v2.0/authorize")
}

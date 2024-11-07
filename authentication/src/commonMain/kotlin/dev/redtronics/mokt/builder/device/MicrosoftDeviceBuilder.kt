/*
 * MIT License
 * Copyright 2024 Nils Jäkel
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the "Software”),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software.
 */

package dev.redtronics.mokt.builder.device

import dev.redtronics.mokt.Microsoft
import io.ktor.http.*

/**
 * Microsoft device authentication builder.
 * Configures the device authentication flow for the Microsoft provider.
 *
 * @since 0.0.1
 * @author Nils Jäkel
 * */
public class MicrosoftDeviceBuilder internal constructor(override val provider: Microsoft) : DeviceAuth<Microsoft>() {
    /**
     * The url of the device authentication endpoint to get the device and user code.
     * Also, the url would automatically resolve to the correct realm.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    override val deviceCodeEndpointUrl: Url
        get() = Url("https://login.microsoftonline.com/${provider.tenant.value}/oauth2/v2.0/devicecode")
}

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

package net.davils.mokt

import io.ktor.client.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import net.davils.mokt.Tenant.*
import net.davils.mokt.builder.device.MicrosoftDeviceBuilder
import net.davils.mokt.builder.grant.GrantCodeBuilder
import net.davils.mokt.network.client
import net.davils.mokt.network.defaultJson

/**
 * Microsoft authentication provider.
 * Interacts with the Microsoft API via device authentication or code grant flow.
 *
 * @since 0.0.1
 * @author Nils Jäkel
 * */
public class Microsoft internal constructor(
    /**
     * The client id for the Microsoft provider.
     * If the client id is not set, the provider will try to get the client id
     * from the environment `MS_CLIENT_ID.`
     *
     * @throws IllegalArgumentException If the client id is not valid or null.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    override val clientId: String,

    /**
     * The client secret for the Microsoft provider.
     * If the client secret is not set, the provider will try to get the client secret
     * from the environment `MS_CLIENT_SECRET.`
     *
     * @throws IllegalArgumentException If the client secret is not valid or null.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    override val clientSecret: String?
) : Provider() {
    override val name: String
        get() = "Microsoft"
    override var httpClient: HttpClient = client
    override var json: Json = defaultJson

    init {
        val isClientIdValid = Regex("[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}").matches(clientId)
        if (!isClientIdValid) throw IllegalArgumentException("Client id is not valid")
    }

    /**
     * The [Tenant] value in the path of the request URL can be used to control
     * who can sign in to the application.
     *
     * For guest scenarios where you sign in a user from one tenant
     * into another tenant, you must provide the tenant identifier to sign them into the target tenant.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public var tenant: Tenant = CONSUMERS

    /**
     * The url of the microsoft's token endpoint to get the access token.
     * It would automatically resolve by on the [tenant] value.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    override val tokenEndpointUrl: Url
        get() = Url("https://login.microsoftonline.com/${tenant.value}/oauth2/v2.0/token")

    /**
     * Uses microsoft's code grant flow.
     *
     * The OAuth 2.0 authorization code grant, also known as the authorization code flow,
     * enables a client application to obtain authorized access to protected resources,
     * such as web APIs.
     *
     * The authorization code flow requires a user agent that supports redirection from
     * the authorization server (Microsoft Identity Platform) back to your application.
     * This can be a web browser, a desktop application, or a mobile application that
     * allows a user to sign in to your application and access their data.
     *
     * @param builder The builder to configure the OAuth 2.0 flow.
     * @return The last result of the builder [T].
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public suspend fun <T> grant(builder: suspend GrantCodeBuilder.() -> T): T {
        val grantCodeBuilder = GrantCodeBuilder(this)
        return builder(grantCodeBuilder)
    }

    /**
     * Uses microsoft's device authentication flow.
     *
     * The OAuth 2.0 device authorization grant is designed for Internet
     * connected devices that either lack a browser to perform a user-agent-
     * based authorization or are input constrained to the extent that
     * requiring the user to input text in order to authenticate during the
     * authorization flow is impractical.
     *
     * It enables OAuth clients on such
     * devices (like smart TVs, media consoles, digital picture frames, and
     * printers) to obtain user authorization to access protected resources
     * by using a user agent on a separate device.
     *
     * @param builder The builder to configure the device flow.
     * @return The result of the builder [T].
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public suspend fun <T> device(builder: suspend MicrosoftDeviceBuilder.() -> T): T {
        val deviceBuilder = MicrosoftDeviceBuilder(this)
        return builder(deviceBuilder)
    }
}

/**
 * The [Tenant] in the request path can be set to specify which users can sign in to the application.
 * Valid identifiers are [COMMON], [ORGANIZATIONS], [CONSUMERS].
 *
 * @property value The name of the tenant.
 *
 * @since 0.0.1
 * @author Nils Jäkel
 */
public enum class Tenant(public val value: String) {
    CONSUMERS("consumers"),
    ORGANIZATIONS("organizations"),
    COMMON("common");
}

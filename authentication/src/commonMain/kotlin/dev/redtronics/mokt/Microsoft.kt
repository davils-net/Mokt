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

@file:Suppress("MemberVisibilityCanBePrivate")

package dev.redtronics.mokt

import dev.redtronics.mokt.Tenant.*
import dev.redtronics.mokt.builder.GrantCodeBuilder
import dev.redtronics.mokt.builder.device.MsDeviceBuilder
import dev.redtronics.mokt.network.client
import dev.redtronics.mokt.network.defaultJson
import io.ktor.client.*
import io.ktor.http.*
import kotlinx.serialization.json.Json

/**
 * Microsoft authentication provider.
 * Interacts with the Microsoft API via device authentication or code grant flow.
 *
 * @since 0.0.1
 * @author Nils Jäkel
 * */
public class Microsoft internal constructor() : Provider {
    override val name: String
        get() = "Microsoft"

    /**
     * The http client used by the Microsoft provider.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    override var httpClient: HttpClient = client

    /**
     * The json used by the Microsoft provider.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    override var json: Json = defaultJson

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
    override var clientId: String? = getEnv("MS_CLIENT_ID")

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
    public var tenant: Tenant = Tenant.CONSUMERS

    /**
     * Scopes are used to specify which resources or permissions your application is requesting access to.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public var scopes: List<Scope> = Scope.allScopes

    /**
     * The url of the microsoft token endpoint.
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    override val tokenEndpointUrl: Url
        get() = Url("https://login.microsoftonline.com/${tenant.value}/oauth2/v2.0/token")

    /**
     * Detects which authentication method is used.
     * If no authentication method used, the auth method will be set to null.
     *
     * @see AuthMethod
     *
     * @since 0.0.1
     * @author Nils Jäkel
     * */
    public var authMethod: AuthMethod? = null
        private set

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
    public suspend fun <T> codeGrant(builder: suspend GrantCodeBuilder.() -> T): T {
        authMethod = AuthMethod.GRANT_CODE
        GrantCodeBuilder(this).apply { return builder() }
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
    public suspend fun <T> device(builder: suspend MsDeviceBuilder.() -> T): T {
        authMethod = AuthMethod.DEVICE_AUTH
        MsDeviceBuilder(this).apply { return builder() }
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

/**
 * The [Scope] in the request path can be set to specify which resources or permissions your
 * application is requesting access to.
 *
 * @since 0.0.1
 * @author Nils Jäkel
 */
public enum class Scope(public val value: String) {
    OPENID("openid"),
    PROFILE("profile"),
    EMAIL("email"),
    OFFLINE_ACCESS("offline_access"),
    XBOX_LIVE_SIGNIN("XBoxLive.signin");

    public companion object {
        /**
         * A list of all available scopes.
         *
         * @since 0.0.1
         * @author Nils Jäkel
         * */
        public val allScopes: List<Scope>
            get() = entries.toList()
    }
}
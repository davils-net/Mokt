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

package dev.redtronics.mokt.server

import dev.redtronics.mokt.flows.GrantAuthData
import dev.redtronics.mokt.response.GrantCodeResponse
import dev.redtronics.mokt.response.device.CodeError
import dev.redtronics.mokt.response.device.CodeErrorResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.routing.*
import kotlinx.coroutines.channels.Channel
import kotlinx.html.HTML

/**
 * Routing module for the ktor server to handle the redirect from the grant authentication flow by any
 * provider.
 *
 * @param redirectPath The path to be routed to. E.g. "/callback"
 * @param channel The coroutine channel to send the grant code response to.
 * @param successPage The success page to be displayed if the request was successful.
 * @param failurePage The failure page to be displayed if the request was not successful.
 * @param onRequestError The function to be called if an error occurs during the request.
 *
 * @since 0.0.1
 * @author Nils Jäkel
 */
internal fun Application.grantRouting(
    redirectPath: String,
    channel: Channel<GrantCodeResponse?>,
    successPage: HTML.() -> Unit,
    failurePage: HTML.() -> Unit,
    onRequestError: suspend (err: CodeErrorResponse) -> Unit,
) {
    routing {
        get(redirectPath) {
            val code = call.request.queryParameters["code"]
            val state = call.request.queryParameters["state"]

            if (code == null || state == null) {
                val errorCode = handleErrorRedirect(call, channel, failurePage)
                onRequestError(errorCode)
                return@get
            }

            val grantCodeResponse = GrantCodeResponse(code, state)
            call.respondHtml(HttpStatusCode.OK, successPage)

            channel.send(grantCodeResponse)
            channel.close()
        }
    }
}

/**
 * Routing module for the ktor server to handle the redirect from the grant authentication flow by any
 * provider.
 *
 * @param redirectPath The path to be routed to. E.g. "/callback"
 * @param channel The coroutine channel to send the grant code response to.
 * @param successPage The success page to be displayed if the request was successful.
 * @param failurePage The failure page to be displayed if the request was not successful.
 * @param onRequestError The function to be called if an error occurs during the request.
 *
 * @since 0.0.1
 * @author Nils Jäkel
 * */
internal fun <T : GrantAuthData> Application.grantRoutingWithFlow(
    redirectPath: String,
    channel: Channel<GrantCodeResponse?>,
    successPage: HTML.() -> Unit,
    failurePage: HTML.() -> Unit,
    flowData: T,
    onRequestError: suspend (err: CodeErrorResponse, flowData: T) -> Unit,
) {
    routing {
        get(redirectPath) {
            val code = call.request.queryParameters["code"]
            val state = call.request.queryParameters["state"]

            if (code == null || state == null) {
                val errorCode = handleErrorRedirect(call, channel, failurePage)
                onRequestError(errorCode, flowData)
                return@get
            }

            val grantCodeResponse = GrantCodeResponse(code, state)
            call.respondHtml(HttpStatusCode.OK, successPage)

            channel.send(grantCodeResponse)
            channel.close()
        }
    }
}

/**
 * Handles the error redirect from the grant authentication flow by any provider.
 *
 * @param call The routing call.
 * @param channel The coroutine channel to send the grant code response to.
 * @param failurePage The failure page to be displayed if the request was not successful.
 *
 * @since 0.0.1
 * @author Nils Jäkel
 * */
private suspend fun handleErrorRedirect(
    call: RoutingCall,
    channel: Channel<GrantCodeResponse?>,
    failurePage: HTML.() -> Unit,
): CodeErrorResponse {
    val parameters = call.request.queryParameters
    val oauthErrorCode = CodeErrorResponse(
        error = CodeError.byName(parameters["error"]!!),
        errorDescription = parameters["error_description"]!!
    )

    call.respondHtml(HttpStatusCode.ExpectationFailed, failurePage)
    channel.send(null)
    channel.close()

    return oauthErrorCode
}

/**
 * Routing module for the ktor server to display the device code in the browser.
 *
 * @param userCode The user code to be displayed.
 * @param displayPath The path to be routed to.
 * @param userCodePage The page to be displayed.
 *
 * @since 0.0.1
 * @author Nils Jäkel
 */
internal fun Application.userCodeRouting(
    userCode: String,
    displayPath: String,
    userCodePage: HTML.(userCode: String) -> Unit,
) {
    routing {
        get(displayPath) {
            call.respondHtml(HttpStatusCode.OK) {
                userCodePage(userCode)
            }
        }
    }
}

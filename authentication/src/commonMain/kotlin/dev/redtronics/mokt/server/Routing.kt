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
 * Module for routing the oauth local redirect.
 *
 * @since 0.0.1
 * @author Nils Jäkel
 */
internal fun Application.grantRouting(
    redirectPath: String,
    channel: Channel<GrantCodeResponse>,
    successPage: HTML.() -> Unit,
    failurePage: HTML.() -> Unit,
    onRequestError: suspend (err: CodeErrorResponse) -> Unit,
) {
    routing {
        get(redirectPath) {
            val code = call.request.queryParameters["code"]
            val state = call.request.queryParameters["state"]

            if (code == null || state == null) {
                val queryParams = call.request.queryParameters
                val oauthErrorCode = CodeErrorResponse(
                    error = CodeError.byName(queryParams["error"]!!),
                    errorDescription = queryParams["error_description"]!!
                )

                call.respondHtml(HttpStatusCode.ExpectationFailed, failurePage)
                onRequestError(oauthErrorCode)
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
 * Module for routing the user code display page.
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

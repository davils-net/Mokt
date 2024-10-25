package dev.redtronics

import dev.redtronics.mokt.MSTenant
import dev.redtronics.mokt.Microsoft
import dev.redtronics.mokt.auth
import io.ktor.http.*
import kotlinx.coroutines.supervisorScope

suspend fun main(): Unit = supervisorScope {
    auth<Microsoft> {
        tenant = MSTenant.CONSUMERS
        clientId = "aa54f7f4-45f6-4f78-b95f-3b6cc98e0b7f"

        codeGrant {
            localRedirectUrl = Url("http://localhost:59187/callback")

            val code = requestGrantCode()
            requestAccessToken(code)
        }
    }
}
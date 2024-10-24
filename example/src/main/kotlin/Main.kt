package dev.redtronics

import dev.redtronics.mokt.Microsoft
import dev.redtronics.mokt.auth
import kotlinx.coroutines.supervisorScope

suspend fun main(): Unit = supervisorScope {
    auth<Microsoft> {
        clientId = "aa54f7f4-45f6-4f78-b95f-3b6cc98e0b7f"

        device {
            val code = requestAuthorizationCode()

            displayCode(code!!.userCode) {
                displayUserCodeInBrowser()
            }

            val accessResponse = requestAccessToken(code)
            println(accessResponse?.accessToken)
        }
    }
}

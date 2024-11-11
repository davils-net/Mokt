package dev.redtronics

import dev.redtronics.mokt.network.openInBrowser
import io.ktor.http.*
import kotlinx.coroutines.supervisorScope

suspend fun main(): Unit = supervisorScope {
//    keycloakAuth(
//        "test-read-token",
//        "I5Xx0DCnUvmK5D484wppaiEmpaLRJeAx",
//        "huebcraft",
//        Url("https://keycloak.huebcraft.net")
//    ) {
//        scopes = listOf(Scope.PROFILE, Scope.OPENID, Scope.EMAIL)
//         grant {
//             val code = this.requestGrantCode() {
//                 println(it.errorDescription)
//             }
//             val token = requestAccessToken(code)
//             println(token)
//         }
//
//    }
    openInBrowser(Url("https://code.redtronics.dev"))
}
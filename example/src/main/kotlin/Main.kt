package dev.redtronics

import dev.redtronics.mokt.keycloakAuth
import kotlinx.coroutines.supervisorScope

suspend fun main(): Unit = supervisorScope {
    keycloakAuth {
        domain = "redtronics.dev"
        realm = "huebcraft"


    }
}
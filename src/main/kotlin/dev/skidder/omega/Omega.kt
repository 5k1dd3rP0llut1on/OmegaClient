package dev.skidder.omega

import dev.skidder.omega.manager.Managers
import kotlinx.coroutines.runBlocking

object Omega {

    fun onInit() = runBlocking {
        Managers.onInit()
    }

}
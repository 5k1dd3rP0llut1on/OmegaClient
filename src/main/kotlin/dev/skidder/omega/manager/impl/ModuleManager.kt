package dev.skidder.omega.manager.impl

import dev.skidder.omega.event.impl.KeyEvent
import dev.skidder.omega.event.safeEventListener
import dev.skidder.omega.manager.AbstractManager
import dev.skidder.omega.module.Module
import dev.skidder.omega.module.impl.client.*
import java.util.concurrent.CopyOnWriteArrayList

object ModuleManager: AbstractManager() {

    private val modules = CopyOnWriteArrayList<Module>()

    override fun onInit() {
        loadModules()

        safeEventListener<KeyEvent>(true) { event ->
            modules.forEach {
                if (event.key == it.keyBind) {
                    it.toggle()
                }
            }
        }
    }

    private fun loadModules() {
        add(Notifications)
    }

    private fun add(module: Module) {
        modules.add(module)
    }

}
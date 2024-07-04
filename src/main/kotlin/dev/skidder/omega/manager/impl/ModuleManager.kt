package dev.skidder.omega.manager.impl

import dev.skidder.omega.event.impl.KeyEvent
import dev.skidder.omega.event.safeEventListener
import dev.skidder.omega.manager.AbstractManager
import dev.skidder.omega.module.Module
import dev.skidder.omega.module.impl.client.ClickGui
import dev.skidder.omega.module.impl.client.Notifications
import dev.skidder.omega.module.impl.combat.CrystalBasePlacer
import java.util.concurrent.CopyOnWriteArrayList

object ModuleManager : AbstractManager() {

    val modules = CopyOnWriteArrayList<Module>()

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
        add(ClickGui)
        add(CrystalBasePlacer)
    }

    private fun add(module: Module) {
        modules.add(module)
    }

}
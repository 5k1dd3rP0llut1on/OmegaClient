package dev.skidder.omega.module.impl.client

import dev.skidder.omega.gui.clickgui.ClickGuiScreen
import dev.skidder.omega.module.Category
import dev.skidder.omega.module.Module
import dev.skidder.omega.util.threads.runSafe
import org.lwjgl.glfw.GLFW

object ClickGui : Module(
    name = "ClickGui",
    category = Category.CLIENT,
    description = "Click Gui for omega client.",
    defaultKeyBind = GLFW.GLFW_KEY_RIGHT_SHIFT
) {
    init {
        onEnable {
            runSafe { mc.setScreen(ClickGuiScreen) }
        }
        onDisable {
            runSafe { mc.setScreen(null) }
        }
    }
}
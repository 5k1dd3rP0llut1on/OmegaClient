package dev.skidder.omega.gui.clickgui

import dev.skidder.omega.gui.CategoryPanel
import dev.skidder.omega.module.Category
import dev.skidder.omega.module.impl.client.ClickGui
import dev.skidder.omega.util.graphics.Render2DUtils
import dev.skidder.omega.util.graphics.color.ColorRGB
import dev.skidder.omega.util.threads.runSafe
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text

object ClickGuiScreen : Screen(Text.of("ClickGUIScreen")) {

    var func: () -> Unit = {}

    private val frames: MutableList<CategoryPanel> = arrayListOf()

    init {
        var offset = 20
        for (category in Category.entries) {
            frames.add(CategoryPanel(category, offset, 30, 120, 14))
            offset += 125
        }
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        runSafe {
            frames.forEach {
                it.render(context, mouseX, mouseY, delta)
                it.updatePosition(mouseX, mouseY)
            }

            func.invoke()
        }

    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        frames.forEach { it.mouseClicked(mouseX, mouseY, button) }
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        frames.forEach { it.mouseReleased(mouseX, mouseY, button) }
        return super.mouseReleased(mouseX, mouseY, button)
    }

    override fun mouseScrolled(
        mouseX: Double,
        mouseY: Double,
        horizontalAmount: Double,
        verticalAmount: Double
    ): Boolean {
        frames.forEach { it.mouseScrolled(verticalAmount) }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)
    }

    override fun keyReleased(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        frames.forEach { it.keyReleased(keyCode, scanCode, modifiers) }
        return super.keyReleased(keyCode, scanCode, modifiers)
    }

    override fun close() {
        ClickGui.disable()
        super.close()
    }

    override fun shouldPause(): Boolean {
        return false
    }

    override fun shouldCloseOnEsc(): Boolean {
        return true
    }

}
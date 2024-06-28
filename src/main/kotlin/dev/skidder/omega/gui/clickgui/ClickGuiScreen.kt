package dev.skidder.omega.gui.clickgui

import dev.skidder.omega.module.Category
import dev.skidder.omega.util.Wrapper
import dev.skidder.omega.util.graphics.Render2DUtils
import dev.skidder.omega.util.graphics.color.ColorRGB
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text

object ClickGuiScreen : Screen(Text.empty()) {
    val animationTime = 0L
    val showingCategory = Category.CLIENT

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        width = 500
        height = 550
        context.matrices.push()
        Render2DUtils.drawRect(
            context.matrices,
            Wrapper.mc.window.width / 2f - width / 2f,
            Wrapper.mc.window.height / 2f - height / 2f,
            width.toFloat(),
            height.toFloat(),
            ColorRGB(255, 255, 255)
        )
        context.matrices.pop()
    }
}
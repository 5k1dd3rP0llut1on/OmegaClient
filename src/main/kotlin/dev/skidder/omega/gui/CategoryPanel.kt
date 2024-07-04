package dev.skidder.omega.gui

import dev.skidder.omega.manager.impl.ModuleManager
import dev.skidder.omega.module.Category
import dev.skidder.omega.module.impl.client.ClickGui
import dev.skidder.omega.util.Wrapper.mc
import dev.skidder.omega.util.graphics.Render2DUtils
import dev.skidder.omega.util.graphics.color.ColorRGB
import dev.skidder.omega.util.graphics.font.TextUtils.drawString
import net.minecraft.client.gui.DrawContext

class CategoryPanel(
    val category: Category,
    var x: Int,
    var y: Int,
    var width: Int,
    var height: Int
) {

    private var dragging = false
    private var extended = true
    private var dragX = 0
    private var dragY = 0

    private val moduleButtons: MutableList<ModuleButton> = mutableListOf()

    init {
        var offset = height

        ModuleManager.modules
            .filter { it.category == category }
            .forEach {
                moduleButtons.add(ModuleButton(it, this, offset))
                offset += height
            }

        ModuleManager
    }

    fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        Render2DUtils.renderRoundedQuad(
            context.matrices, ColorRGB(80, 80, 80, 255),
            x.toFloat(), y.toFloat(), x + width.toFloat(), y + height.toFloat(), 2.0, 2.0
        )

        val offset = (height / 2) - mc.textRenderer.fontHeight / 2

        drawString(
            context, category.displayString,
            x + (width / 2.0f - mc.textRenderer.getWidth(category.displayString) / 2), y + offset.toFloat(),
            ColorRGB(255, 255, 255), ClickGui.shadow
        )

        drawString(
            context, if (extended) "-" else "+",
            x + width - (offset) - mc.textRenderer.getWidth("+").toFloat(), y + offset.toFloat(),
            ColorRGB(255, 255, 255), ClickGui.shadow
        )

        if (extended) {
            updateButtons()
            moduleButtons.forEach { it.render(context, mouseX, mouseY, delta) }
        }

    }

    fun mouseClicked(mouseX: Double, mouseY: Double, button: Int) {
        if (isHovered(mouseX, mouseY)) {
            when (button) {
                0 -> {  // 左键
                    dragging = true
                    dragX = (mouseX - x).toInt()
                    dragY = (mouseY - y).toInt()
                }

                1 -> {  // 右键
                    extended = !extended
                }
            }

        }
        if (extended) {
            moduleButtons.forEach { it.mouseClicked(mouseX, mouseY, button) }
        }
    }

    fun mouseReleased(mouseX: Double, mouseY: Double, button: Int) {
        if (button == 0 && dragging) dragging = false
        moduleButtons.forEach { it.mouseReleased(mouseX, mouseY, button) }
    }

    fun mouseScrolled(amount: Double) {
        y += amount.toInt() * 4
    }

    private fun isHovered(mouseX: Double, mouseY: Double): Boolean {
        return mouseX > x && mouseX < (x + width) && mouseY > y && mouseY < (y + height)
    }

    fun updatePosition(mouseX: Int, mouseY: Int) {
        if (dragging) {
            x = mouseX - dragX
            y = mouseY - dragY
        }
    }

    fun updateButtons() {
        var offset = height

        for (modButton in moduleButtons) {
            modButton.offset = offset

            if (modButton.extended) {

                modButton.components
                    .filter { it.setting.visibility.invoke() }
                    .forEach { _ ->
                        offset += height
                    }
            }

            offset += height
        }
    }

    fun keyReleased(keyCode: Int, scanCode: Int, modifiers: Int) {
        moduleButtons.forEach { it.keyReleased(keyCode, scanCode, modifiers) }
    }

}
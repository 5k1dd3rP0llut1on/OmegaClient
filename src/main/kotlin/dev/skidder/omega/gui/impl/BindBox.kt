package dev.skidder.omega.gui.impl

import dev.skidder.omega.gui.Component
import dev.skidder.omega.gui.ModuleButton
import dev.skidder.omega.module.impl.client.ClickGui
import dev.skidder.omega.settings.AbstractSetting
import dev.skidder.omega.settings.KeyBindSetting
import dev.skidder.omega.util.Wrapper.mc
import dev.skidder.omega.util.graphics.Render2DUtils
import dev.skidder.omega.util.graphics.color.ColorRGB
import dev.skidder.omega.util.graphics.font.TextUtils
import net.minecraft.client.gui.DrawContext
import org.lwjgl.glfw.GLFW

class BindBox(
    setting: AbstractSetting<*>,
    parent: ModuleButton,
    offset: Int
) : Component(setting, parent, offset) {

    private val bindSet: KeyBindSetting = setting as KeyBindSetting
    private var binding = false

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        if (!bindSet.visibility.invoke()) return

        Render2DUtils.renderRoundedQuad(
            context.matrices,
            if (isHovered(mouseX.toDouble(), mouseY.toDouble())) ColorRGB(100, 100, 100, 255)
            else ColorRGB(80, 80, 80, 255),
            parent.parent.x.toFloat(), parent.parent.y.toFloat() + parent.offset + offset,
            parent.parent.x + parent.parent.width.toFloat(),
            parent.parent.y + parent.offset + offset + parent.parent.height.toFloat(),
            2.0, 2.0
        )

        val textOffset = (parent.parent.height / 2) - mc.textRenderer.fontHeight / 2

        TextUtils.drawString(
            context,
            if (binding) "${bindSet.name}: ..." else "${bindSet.name}: ${bindSet.value.keyName}",
            parent.parent.x + textOffset.toFloat(), parent.parent.y + parent.offset + offset + textOffset.toFloat(),
            ColorRGB(255, 255, 255), ClickGui.shadow
        )
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int) {
        if (!bindSet.visibility.invoke()) return
        if (isHovered(mouseX, mouseY) && button == 0) binding = !binding
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int) {
        if (!bindSet.visibility.invoke()) return
    }

    override fun keyReleased(keyCode: Int, scanCode: Int, modifiers: Int) {
        if (!bindSet.visibility.invoke()) return
        if (binding) {
            if (keyCode == GLFW.GLFW_KEY_DELETE) bindSet.value.keyCode = -1
            else bindSet.value.keyCode = keyCode
            binding = false
        }
    }
}
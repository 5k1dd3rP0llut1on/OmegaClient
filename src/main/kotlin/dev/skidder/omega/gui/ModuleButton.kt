package dev.skidder.omega.gui

import dev.skidder.omega.gui.clickgui.ClickGuiScreen
import dev.skidder.omega.gui.impl.BindBox
import dev.skidder.omega.gui.impl.CheckBox
import dev.skidder.omega.gui.impl.ModeBox
import dev.skidder.omega.gui.impl.Slider
import dev.skidder.omega.module.Module
import dev.skidder.omega.module.impl.client.ClickGui
import dev.skidder.omega.settings.BooleanSetting
import dev.skidder.omega.settings.EnumSetting
import dev.skidder.omega.settings.KeyBindSetting
import dev.skidder.omega.settings.NumberSetting
import dev.skidder.omega.util.Wrapper.mc
import dev.skidder.omega.util.graphics.Render2DUtils
import dev.skidder.omega.util.graphics.color.ColorRGB
import dev.skidder.omega.util.graphics.font.TextUtils
import net.minecraft.client.gui.DrawContext

class ModuleButton(val module: Module, val parent: CategoryPanel, var offset: Int) {

    val components: MutableList<Component> = arrayListOf()
    var extended: Boolean = false

    init {
        var setOffset = parent.height
        for (setting in module.settings) {
            when (setting) {
                is NumberSetting -> {
                    components.add(Slider(setting, this, setOffset))
                    setOffset += parent.height
                }

                is BooleanSetting -> {
                    components.add(CheckBox(setting, this, setOffset))
                    setOffset += parent.height
                }

                is EnumSetting<*> -> {
                    components.add(ModeBox(setting, this, setOffset))
                    setOffset += parent.height
                }

                is KeyBindSetting -> {
                    components.add(BindBox(setting, this, setOffset))
                    setOffset += parent.height
                }
            }
        }
    }

    fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        Render2DUtils.renderRoundedQuad(
            context.matrices,
            if (isHovered(mouseX.toDouble(), mouseY.toDouble())) ColorRGB(100, 100, 100, 255)
            else ColorRGB(80, 80, 80, 255),
            parent.x.toFloat(), parent.y.toFloat() + offset,
            parent.x + parent.width.toFloat(), parent.y + offset + parent.height.toFloat(),
            2.0, 2.0
        )

        val textOffset = (parent.height / 2) - mc.textRenderer.fontHeight / 2

        TextUtils.drawString(
            context, module.name.toString(),
            parent.x + textOffset.toFloat(), parent.y + offset + textOffset.toFloat(),
            if (module.isEnabled) ColorRGB(130, 180, 210)
            else ColorRGB(255, 255, 255),
            ClickGui.shadow
        )

        TextUtils.drawString(
            context, if (extended) "-" else "+",
            parent.x + parent.width - mc.textRenderer.getWidth("+") - ((parent.height / 2.0f) - mc.textRenderer.fontHeight / 2.0f),
            parent.y + offset + textOffset.toFloat(),
            if (module.isEnabled) ColorRGB(130, 180, 210)
            else ColorRGB(255, 255, 255),
            ClickGui.shadow
        )

        if (extended) {
            refreshComponentsOffset()
            components.forEach { it.render(context, mouseX, mouseY, delta) }
        }

        if (isHovered(mouseX.toDouble(), mouseY.toDouble())) {
            ClickGuiScreen.func = {
                Render2DUtils.drawRect(
                    context.matrices,
                    0f,
                    mc.window.scaledHeight - (mc.textRenderer.fontHeight + 2.0f),
                    mc.textRenderer.getWidth(module.description.toString()) + 4.0f,
                    mc.textRenderer.fontHeight + 2.0f,
                    ColorRGB(50, 50, 50, 255)
                )

                TextUtils.drawString(
                    context, module.description.toString(),
                    2.0f, mc.window.scaledHeight - (mc.textRenderer.fontHeight + 2.0f) + 1.0f,
                    ColorRGB(255, 255, 255), ClickGui.shadow
                )
            }
        }
    }

    fun mouseClicked(mouseX: Double, mouseY: Double, button: Int) {
        if (isHovered(mouseX, mouseY)) {
            if (button == 0) {
                module.toggle()
            } else {
                extended = !extended
                parent.updateButtons()
            }
        }

        if (extended) {
            components.forEach { it.mouseClicked(mouseX, mouseY, button) }
        }
    }

    fun mouseReleased(mouseX: Double, mouseY: Double, button: Int) {
        if (extended) {
            components.forEach { it.mouseReleased(mouseX, mouseY, button) }
        }
    }

    fun isHovered(mouseX: Double, mouseY: Double): Boolean {
        return mouseX > parent.x && mouseX < (parent.x + parent.width)
                && mouseY > parent.y + offset && mouseY < (parent.y + offset + parent.height)
    }

    private fun refreshComponentsOffset() {
        var setOffset = parent.height
        for (comp in components) {
            if (!comp.setting.visibility.invoke()) continue
            comp.offset = setOffset
            setOffset += parent.height
        }
    }

    fun keyReleased(keyCode: Int, scanCode: Int, modifiers: Int) {
        components.forEach { it.keyReleased(keyCode, scanCode, modifiers) }
    }

}
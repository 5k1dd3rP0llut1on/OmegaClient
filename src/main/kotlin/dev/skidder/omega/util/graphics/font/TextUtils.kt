package dev.skidder.omega.util.graphics.font

import dev.skidder.omega.event.SafeClientEvent
import dev.skidder.omega.util.graphics.color.ColorRGB
import net.minecraft.client.gui.DrawContext
import java.awt.Color

object TextUtils {
    fun SafeClientEvent.drawString(drawContext: DrawContext, text: String, x: Float, y: Float, color: ColorRGB, shadow: Boolean) {
        drawString(drawContext, text, x, y, color.toColor(), shadow)
    }

    fun SafeClientEvent.drawString(drawContext: DrawContext, text: String, x: Float, y: Float, color: Color, shadow: Boolean) {
        val matrixStack = drawContext.matrices
        matrixStack.push()
        drawContext.drawText(mc.textRenderer, text, x.toInt(), y.toInt(), color.rgb, shadow)
        matrixStack.pop()
    }

    fun SafeClientEvent.drawString(drawContext: DrawContext, text: String, x: Double, y: Double, color: Int, shadow: Boolean) {
        val matrixStack = drawContext.matrices
        matrixStack.push()
        drawContext.drawText(mc.textRenderer, text, x.toInt(), y.toInt(), color, shadow)
        matrixStack.pop()
    }

    fun SafeClientEvent.drawStringWithScale(drawContext: DrawContext, text: String, x: Float, y: Float, color: ColorRGB, shadow: Boolean, scale: Float) {
        drawStringWithScale(drawContext, text, x, y, color.toColor(), shadow, scale)
    }

    fun SafeClientEvent.drawStringWithScale(drawContext: DrawContext, text: String, x: Float, y: Float, color: Color, shadow: Boolean, scale: Float) {
        val matrixStack = drawContext.matrices
        matrixStack.push()
        matrixStack.scale(scale, scale, 1.0f)

        matrixStack.translate((x / scale) - x, (y / scale) - y, 0.0f)


        drawContext.drawText(mc.textRenderer, text, x.toInt(), y.toInt(), color.rgb, shadow)
        matrixStack.pop()
    }

    fun SafeClientEvent.drawStringWithScale(drawContext: DrawContext, text: String, x: Float, y: Float, color: Int, shadow: Boolean, scale: Float) {
        val matrixStack = drawContext.matrices
        matrixStack.push()
        matrixStack.scale(scale, scale, 1.0f)


        matrixStack.translate((x / scale) - x, (y / scale) - y, 0.0f)

        drawContext.drawText(mc.textRenderer, text, x.toInt(), y.toInt(), color, shadow)
        matrixStack.pop()
    }
}
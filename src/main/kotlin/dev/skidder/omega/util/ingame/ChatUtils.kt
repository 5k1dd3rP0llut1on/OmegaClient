package dev.skidder.omega.util.ingame

import dev.skidder.omega.event.SafeClientEvent
import net.minecraft.text.Text
import net.minecraft.util.Formatting

object ChatUtils {

    fun SafeClientEvent.sendMessage(message: CharSequence) {
        mc.inGameHud.chatHud.addMessage(
            Text.of("${Formatting.AQUA}[Omega] ${Formatting.WHITE}" + message.toString())
        )
    }
}
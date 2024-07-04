package dev.skidder.omega.module

import dev.skidder.omega.util.interfaces.DisplayEnum

enum class Category(override val displayName: CharSequence): DisplayEnum {

    COMBAT("Combat"),
    MISC("Misc"),
    MOVEMENT("Movement"),
    PLAYER("Player"),
    RENDER("Render"),
    CLIENT("Client"),
    HUD("HUD")

}
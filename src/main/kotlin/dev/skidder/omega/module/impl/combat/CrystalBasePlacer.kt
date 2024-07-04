package dev.skidder.omega.module.impl.combat

import dev.skidder.omega.event.impl.TickEvent
import dev.skidder.omega.event.safeEventListener
import dev.skidder.omega.module.Category
import dev.skidder.omega.module.Module
import dev.skidder.omega.util.player.InventoryUtils.findItemInHotbar
import dev.skidder.omega.util.time.TimerUtils
import net.minecraft.block.Blocks
import net.minecraft.item.EndCrystalItem
import net.minecraft.item.Items
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult

object CrystalBasePlacer : Module(
    name = "CrystalBasePlacer",
    description = "Auto Place CrystalBase.",
    category = Category.COMBAT
) {
    private val backDelay by setting("BackDelay", 100, 50..1000)
    private val backTimer = TimerUtils()
    private var backed = true
    private var oldSlot = 0

    init {
        safeEventListener<TickEvent.Pre> {
            if (!backed) {
                if (backTimer.passed(backDelay)) {
                    player.inventory.selectedSlot = oldSlot
                    player.useBook(player.mainHandStack, Hand.MAIN_HAND)
                    backed = true
                }
            } else {
                (mc.crosshairTarget as? BlockHitResult)?.blockPos?.let { blockPos ->
                    if (world.getBlockState(blockPos).block == Blocks.OBSIDIAN || world.getBlockState(blockPos).block == Blocks.BEDROCK || world.isAir(
                            blockPos
                        )
                    ) return@safeEventListener
                    if (player.mainHandStack.item !is EndCrystalItem) return@safeEventListener
                    if (mc.options.useKey.isPressed) {
                        findItemInHotbar(Items.OBSIDIAN)?.let { slot ->
                            oldSlot = player.inventory.selectedSlot
                            player.inventory.selectedSlot = slot
                            player.useBook(player.mainHandStack, Hand.MAIN_HAND)
                            backed = false
                            backTimer.reset()
                        }
                    }
                }
            }
        }
    }
}
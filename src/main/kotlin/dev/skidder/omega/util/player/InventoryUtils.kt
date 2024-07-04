package dev.skidder.omega.util.player

import dev.skidder.omega.event.SafeClientEvent
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket
import net.minecraft.screen.slot.SlotActionType

object InventoryUtils {

    fun SafeClientEvent.findItemInHotbar(item: Item): Int? {
        for (i in 0 until 9) {
            if (player.inventory.getStack(i).item == item) return i
        }
        return null
    }

    fun SafeClientEvent.switchTo(slot: Int) {
        if (player.inventory.selectedSlot == slot) return
        player.inventory.selectedSlot = slot
        player.networkHandler.sendPacket(UpdateSelectedSlotC2SPacket(slot))
    }

    fun SafeClientEvent.moveTo(from: Int, to: Int) {
        val handler = player.currentScreenHandler
        val stack = Int2ObjectArrayMap<ItemStack>()
        stack.put(to, handler.getSlot(to).stack)
        connection.sendPacket(
            ClickSlotC2SPacket(
                handler.syncId,
                handler.revision,
                PlayerInventory.MAIN_SIZE + from,
                to,
                SlotActionType.SWAP,
                handler.cursorStack.copy(),
                stack
            )
        )
    }

    fun SafeClientEvent.findItemInInv(item: Item): Int? {
        for (i in 0..player.inventory.size()) {
            val stack = player.inventory.getStack(i)
            if (stack.item == item) return i
        }
        return null
    }

    fun SafeClientEvent.getItemCount(item: Item): Int {
        var count = 0
        for (i in 0..player.inventory.size()) {
            val stack = player.inventory.getStack(i)
            if (stack.item == item) ++count
        }
        return count
    }

    fun SafeClientEvent.spoofHotbar(slot: Int, func: () -> Unit) {
        val oldSlot = player.inventory.selectedSlot
        switchTo(slot)
        func.invoke()
        switchTo(oldSlot)
    }

    fun SafeClientEvent.spoofHotbarBypass(slot: Int, func: () -> Unit) {
        move(player.inventory.selectedSlot, slot)
        func.invoke()
        move(player.inventory.selectedSlot, slot)
    }

    fun SafeClientEvent.move(from: Int, to: Int) {
        val handler = player.currentScreenHandler
        val stack = Int2ObjectArrayMap<ItemStack>()
        stack.put(to, handler.getSlot(to).stack)
        connection.sendPacket(
            ClickSlotC2SPacket(
                handler.syncId,
                handler.revision,
                PlayerInventory.MAIN_SIZE + from,
                to,
                SlotActionType.SWAP,
                handler.cursorStack.copy(),
                stack
            )
        )
    }

    private fun SafeClientEvent.doMove(from: Int, to: Int) {
        val handler = player.currentScreenHandler
        val stack = Int2ObjectArrayMap<ItemStack>()
        stack.put(to, handler.getSlot(to).stack)
        connection.sendPacket(
            ClickSlotC2SPacket(
                handler.syncId,
                handler.revision,
                PlayerInventory.MAIN_SIZE + from,
                to,
                SlotActionType.SWAP,
                handler.cursorStack.copy(),
                stack
            )
        )
    }

    val SafeClientEvent.inventoryAndHotbarSlots: Map<Int, ItemStack>
        get() = getInventorySlots(9)

    fun SafeClientEvent.getInventorySlots(current: Int): Map<Int, ItemStack> {
        var currentSlot = current
        val fullInventorySlots: MutableMap<Int, ItemStack> = HashMap()
        while (currentSlot <= 44) {
            fullInventorySlots[currentSlot] = player.inventory.getStack(currentSlot)
            currentSlot++
        }
        return fullInventorySlots
    }

    interface Searcher {
        fun isValid(stack: ItemStack?): Boolean
    }

}
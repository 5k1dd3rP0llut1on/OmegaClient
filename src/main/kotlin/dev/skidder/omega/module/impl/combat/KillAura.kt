package dev.skidder.omega.module.impl.combat

import dev.skidder.omega.event.SafeClientEvent
import dev.skidder.omega.event.impl.TickEvent
import dev.skidder.omega.event.safeEventListener
import dev.skidder.omega.module.Category
import dev.skidder.omega.module.Module
import dev.skidder.omega.util.math.RotationUtils.getRotationTo
import dev.skidder.omega.util.math.distanceSqTo
import dev.skidder.omega.util.math.distanceSqToCenter
import dev.skidder.omega.util.math.sq
import dev.skidder.omega.util.threads.runSafe
import net.minecraft.entity.Entity
import net.minecraft.entity.mob.MobEntity
import net.minecraft.entity.passive.AnimalEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ShieldItem
import net.minecraft.item.SwordItem
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket
import net.minecraft.util.Hand
import kotlin.math.max

object KillAura : Module(name = "KillAura", category = Category.COMBAT, description = "") {
    private var animals by setting("Animals", false)
    private var mobs by setting("Mobs", false)
    private var swapWeapon by setting("SwapWeapon", false)
    private var onlySword by setting("OnlySword", true)
    private var range by setting("Range", 3.5, 1.0..8.0)
    private var autoBlock by setting("AutoBlock", false)
    private var abRange by setting("ABRange", 3.5, 1.0..10.0) { autoBlock }
    private var pauseInHit by setting("ABPauseInHit", false)
//    private var render by setting("Render", false)
//    private var color by setting("Color", ColorRGB(255, 255, 255, 80))

    private var target: Entity? = null
    private var stop = false

    init {
        safeEventListener<TickEvent.Post> {
            target = getEntityTarget(
                if (autoBlock) max(range, abRange) else range,
                mob = mobs,
                ani = animals
            )
            target?.let { target ->
                val weaponSlot = findSword()
                if (onlySword && player.mainHandStack.item !is SwordItem) return@safeEventListener
                if (swapWeapon) {
                    weaponSlot?.let { swordSlot ->
                        if (player.inventory.selectedSlot != swordSlot) {
                            player.inventory.selectedSlot = swordSlot
                        }
                    }
                }
                connection.sendPacket(
                    PlayerMoveC2SPacket.Full(
                        player.x,
                        player.y,
                        player.z,
                        getRotationTo(target.pos).x,
                        getRotationTo(target.pos).y,
                        player.isOnGround
                    )
                )
                if (autoBlock && player.distanceSqTo(target.pos) <= abRange.sq) {
                    if (player.offHandStack.item is ShieldItem) {
                        if (player.offHandStack.item is ShieldItem) {
                            playerController.interactItem(player, Hand.OFF_HAND)
                        }
                    }
                }
                if (!delayCheck() || player.distanceSqTo(target.pos) > range.sq) return@safeEventListener
                if (autoBlock && pauseInHit) {
                    if (player.offHandStack.item is ShieldItem) {
                        playerController.stopUsingItem(player)
                    }
                }
                connection.sendPacket(PlayerInteractEntityC2SPacket.attack(target, player.isSneaking))
                player.swingHand(Hand.MAIN_HAND)
                player.resetLastAttackedTicks()
                stop = true
            }
            if (target == null) {
                if (autoBlock && stop) {
                    playerController.stopUsingItem(player)
                }
            }
        }

//        safeEventListener<Render3DEvent> { event ->
//            if (render) {
//                if (player.mainHandStack.item !is SwordItem) return@safeEventListener
//                target?.let {
//                    Render3DEngine.drawFilledBox(it.boundingBox, color.toColor())
//                }
//            }
//        }

        onDisable {
            runSafe {
                if (target == null && autoBlock) playerController.stopUsingItem(player)
            }
        }
    }

    private fun SafeClientEvent.findSword(): Int? {
        for (i in 0 until 9) {
            if (player.inventory.getStack(i).item is SwordItem) return i
        }
        return null
    }

    private fun SafeClientEvent.getEntityTarget(
        range: Double,
        mob: Boolean = true,
        ani: Boolean = true
    ): Entity? {
        for (ent in world.entities.filter {
            player.distanceSqToCenter(it.blockPos) <= range.sq && it.isAlive && (it !is PlayerEntity || it != player)
        }.sortedBy { player.distanceSqToCenter(it.blockPos) }) {
            if (ent is PlayerEntity || (mob && ent is MobEntity) || (ani && ent is AnimalEntity)) return ent
        }
        return null
    }

    private fun SafeClientEvent.delayCheck(): Boolean {
        return player.getAttackCooldownProgress(1F) >= 1
    }
}
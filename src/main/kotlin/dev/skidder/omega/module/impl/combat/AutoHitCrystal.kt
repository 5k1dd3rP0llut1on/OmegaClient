package dev.skidder.omega.module.impl.combat

import dev.skidder.omega.event.impl.TickEvent
import dev.skidder.omega.event.safeEventListener
import dev.skidder.omega.module.Category
import dev.skidder.omega.module.Module
import dev.skidder.omega.util.math.toBlockPos
import dev.skidder.omega.util.time.TimerUtils
import net.minecraft.block.GrassBlock
import net.minecraft.entity.decoration.EndCrystalEntity
import net.minecraft.util.Hand
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.Box

object AutoHitCrystal : Module(
    name = "AutoHitCrystal",
    category = Category.COMBAT,
    description = "Auto Hit Crystal."
) {
    private val delay by setting("HitDelay", 50, 0..250)
    private val timer = TimerUtils()
    private var isSet = false

    init {
        safeEventListener<TickEvent.Pre> {
            fun doAttack(entity: EndCrystalEntity) {
                if (timer.tickAndReset(delay)) {
                    playerController.attackEntity(player, entity)
                    player.swingHand(Hand.MAIN_HAND)
                }
            }
            if (mc.options.rightKey.isPressed) {
                mc.crosshairTarget?.let { result ->
                    if (result.type == HitResult.Type.ENTITY) {
                        if ((result as EntityHitResult).entity is EndCrystalEntity) {
                            doAttack(result.entity as EndCrystalEntity)
                        }
                    }
                    if (result.type == HitResult.Type.BLOCK) {
                        result.pos?.let { pos ->
                            if (world.entities.any {
                                    it is EndCrystalEntity && it.boundingBox.intersects(
                                        Box(
                                            pos.toBlockPos().up()
                                        )
                                    )
                                }) {
                                doAttack(world.entities.filter {
                                    it is EndCrystalEntity && it.boundingBox.intersects(
                                        Box(
                                            pos.toBlockPos().up()
                                        )
                                    )
                                }.first() as EndCrystalEntity)
                            }
                            if (world.getBlockState(pos.toBlockPos()).block !is GrassBlock) {
                                mc.options.useKey.isPressed = true
                                isSet = true
                            }
                        }
                    }
                }
            } else if (isSet) {
                mc.options.useKey.isPressed = false
            }
        }
    }
}
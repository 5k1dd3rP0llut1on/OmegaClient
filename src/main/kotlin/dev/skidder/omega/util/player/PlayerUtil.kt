package dev.skidder.omega.util.player

import dev.skidder.omega.event.SafeClientEvent
import dev.skidder.omega.util.block.isFullBox
import dev.skidder.omega.util.entity.EntityUtils.eyePosition
import dev.skidder.omega.util.math.EnumSet
import dev.skidder.omega.util.math.toVec3dCenter
import dev.skidder.omega.util.math.vector.Vec3f
import net.minecraft.block.Blocks
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import java.util.*

object PlayerUtil {
    fun SafeClientEvent.isMoving(): Boolean = player.input.movementForward != 0.0f || player.input.movementSideways != 0.0f
    fun SafeClientEvent.getMiningSide(pos: BlockPos): Direction? {
        val eyePos = player.eyePosition

        return getVisibleSides(pos)
            .filter { !world.getBlockState(pos.offset(it)).isFullBox }
            .minByOrNull { eyePos.squaredDistanceTo(getHitVec(pos, it)) }
    }

    fun SafeClientEvent.getVisibleSides(pos: BlockPos, assumeAirAsFullBox: Boolean = false): Set<Direction> {
        val visibleSides = EnumSet<Direction>()

        val eyePos = player.eyePosition
        val blockCenter = pos.toVec3dCenter()
        val blockState = world.getBlockState(pos)
        val isFullBox = assumeAirAsFullBox && blockState.block == Blocks.AIR || blockState.isFullBox

        return visibleSides
            .checkAxis(eyePos.x - blockCenter.x, Direction.WEST, Direction.EAST, !isFullBox)
            .checkAxis(eyePos.y - blockCenter.y, Direction.DOWN, Direction.UP, true)
            .checkAxis(eyePos.z - blockCenter.z, Direction.NORTH, Direction.SOUTH, !isFullBox)
    }

    private fun EnumSet<Direction>.checkAxis(
        diff: Double,
        negativeSide: Direction,
        positiveSide: Direction,
        bothIfInRange: Boolean
    ) =
        this.apply {
            when {
                diff < -0.5 -> {
                    add(negativeSide)
                }

                diff > 0.5 -> {
                    add(positiveSide)
                }

                else -> {
                    if (bothIfInRange) {
                        add(negativeSide)
                        add(positiveSide)
                    }
                }
            }
        }


    fun getHitVec(pos: BlockPos, facing: Direction): Vec3d {
        val vec = facing.vector
        return Vec3d(vec.x * 0.5 + 0.5 + pos.x, vec.y * 0.5 + 0.5 + pos.y, vec.z * 0.5 + 0.5 + pos.z)
    }

    fun getHitVecOffset(facing: Direction): Vec3f {
        val vec = facing.vector
        return Vec3f(vec.x * 0.5f + 0.5f, vec.y * 0.5f + 0.5f, vec.z * 0.5f + 0.5f)
    }
}
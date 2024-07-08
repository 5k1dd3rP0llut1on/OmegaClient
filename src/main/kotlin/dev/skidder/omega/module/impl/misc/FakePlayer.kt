package dev.skidder.omega.module.impl.misc

import com.mojang.authlib.GameProfile
import dev.skidder.omega.module.Category
import dev.skidder.omega.module.Module
import dev.skidder.omega.util.threads.runSafe
import net.minecraft.client.network.OtherClientPlayerEntity
import net.minecraft.entity.Entity
import java.util.*

object FakePlayer : Module(
    name = "FakePlayer",
    description = "Spawns a fake Player",
    category = Category.MISC
) {
    private var health by setting("Health", 12, 0..36)
    private var fakePlayer: OtherClientPlayerEntity? = null

    init {
        onEnable {
            runSafe {
                fakePlayer = OtherClientPlayerEntity(
                    world,
                    GameProfile(UUID.fromString("60569353-f22b-42da-b84b-d706a65c5ddf"), "Omega Client")
                )
                fakePlayer?.let { fakePlayer ->
                    fakePlayer.copyPositionAndRotation(player)
                    for (potionEffect in player.activeStatusEffects) {
                        fakePlayer.addStatusEffect(potionEffect.value)
                    }
                    fakePlayer.health = health.toFloat()
                    fakePlayer.inventory.clone(player.inventory)
                    fakePlayer.yaw = player.yaw
                    world.addEntity(fakePlayer)
                }
            }
        }
        onDisable {
            runSafe {
                fakePlayer?.let {
                    it.kill()
                    it.setRemoved(Entity.RemovalReason.KILLED)
                    it.onRemoved()
                }
            }
        }
    }
}
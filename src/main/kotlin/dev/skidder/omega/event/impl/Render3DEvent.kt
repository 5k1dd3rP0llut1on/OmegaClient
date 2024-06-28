package dev.skidder.omega.event.impl

import dev.skidder.omega.event.Event
import net.minecraft.client.util.math.MatrixStack

class Render3DEvent(val matrices: MatrixStack): Event()
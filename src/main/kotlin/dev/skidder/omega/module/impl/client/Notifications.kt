package dev.skidder.omega.module.impl.client

import dev.skidder.omega.module.Category
import dev.skidder.omega.module.Module

object Notifications: Module(
    name = "Notifications",
    description = "Logging when module toggling",
    category = Category.CLIENT,
    defaultEnable = true
)
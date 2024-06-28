package dev.skidder.omega.event.impl

import dev.skidder.omega.event.Event
import dev.skidder.omega.settings.KeyBind

class KeyEvent(val key: KeyBind): Event()
package dev.skidder.omega.event.impl

import dev.skidder.omega.event.Event

sealed class TickEvent {

    object Post: Event()
    object Pre: Event()

}
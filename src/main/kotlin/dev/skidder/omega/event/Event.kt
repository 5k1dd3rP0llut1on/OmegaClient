package dev.skidder.omega.event

open class Event {
    fun post() {
        EventBus.post(this)
    }
}

open class CancellableEvent : Event() {
    var isCancelled = false
        private set

    fun cancel() {
        isCancelled = true
    }
}
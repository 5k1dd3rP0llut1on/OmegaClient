package dev.skidder.omega.util.interfaces

interface Description {
    val description: CharSequence

    fun descriptionAsString() = description.toString()
}
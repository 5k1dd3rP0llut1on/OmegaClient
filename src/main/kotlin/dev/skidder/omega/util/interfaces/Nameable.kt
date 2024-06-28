package dev.skidder.omega.util.interfaces

interface Nameable {
    val name: CharSequence

    fun nameAsString() = name.toString()
}
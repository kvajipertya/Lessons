package com.kvajipertya.lessons

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
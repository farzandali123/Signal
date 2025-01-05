package com.example.signal
import java.io.Serializable

data class Status(
    val imageUrl: String,
    val timestamp: Long,
    val userName: String = ""
) : Serializable



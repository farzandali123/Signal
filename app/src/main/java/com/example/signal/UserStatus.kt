
package com.example.signal
import java.io.Serializable

data class UserStatus(
    val userName: String,
    val statuses: List<Status>
) : Serializable


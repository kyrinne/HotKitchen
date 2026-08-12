package hotkitchen.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val email: String,
    val userType: String,
    val password: String,
    val name: String? = null,
    val phoneNumber: String? = null,
    val address: String? = null,
)

@Serializable
data class UserProfile(
    val name: String,
    val userType: String,
    val phone: String,
    val email: String,
    val address: String,
)

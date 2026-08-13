package model

import kotlinx.serialization.Serializable

@Serializable
data class Meal(
    val mealId: Int,
    val title: String,
    val price: Float,
    val imageUrl: String,
    val categoryIds: List<Int>,
)

@Serializable
data class Category(
    val categoryId: Int,
    val title: String,
    val description: String,
)

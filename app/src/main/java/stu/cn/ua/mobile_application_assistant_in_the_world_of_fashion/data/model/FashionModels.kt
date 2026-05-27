package stu.cn.ua.mobile_application_assistant_in_the_world_of_fashion.data.model

data class FashionItem(
    val id: String,
    val name: String,
    val description: String,
    val imageUrl: String,
    val category: String // upper, lower, accessories, etc.
)

data class FashionEpoch(
    val id: String,
    val title: String,
    val description: String,
    val years: String
)

data class Stylist(
    val id: String,
    val name: String,
    val specialization: String,
    val contactInfo: String
)

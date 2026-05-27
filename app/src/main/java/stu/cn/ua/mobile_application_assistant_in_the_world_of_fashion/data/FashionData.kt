package stu.cn.ua.mobile_application_assistant_in_the_world_of_fashion.data

import stu.cn.ua.mobile_application_assistant_in_the_world_of_fashion.R

data class Century(
    val id: String,
    val nameRes: Int,
    val shortName: String
)

data class ClothingPart(
    val id: String,
    val nameRes: Int
)

data class FashionLook(
    val id: String,
    val centuryId: String,
    val partId: String,
    val titleRes: Int,
    val descriptionRes: Int,
    val fullDescriptionRes: Int = 0,
    val imageRes: Int, 
    var isExpanded: Boolean = false
)

data class Stylist(
    val id: String,
    val nameRes: Int,
    val bioRes: Int,
    val imageRes: Int,
    val bookingUrl: String,
    var isLiked: Boolean = false
)

object FashionDataSource {
    val centuries = listOf(
        Century("ancient_medieval", R.string.century_ancient, "V-XV"),
        Century("renaissance_baroque", R.string.century_renaissance, "XVI-XVII"),
        Century("xviii", R.string.century_xviii, "XVIII"),
        Century("xix", R.string.century_xix, "XIX"),
        Century("xx_early", R.string.century_xx_early, "XXp"),
        Century("xx_mid", R.string.century_xx_mid, "XXs"),
        Century("xx_late", R.string.century_xx_late, "XXk"),
        Century("xxi", R.string.century_xxi, "XXI")
    )

    val parts = listOf(
        ClothingPart("all", R.string.history_all_looks),
        ClothingPart("dresses", R.string.history_dresses),
        ClothingPart("pants_skirts", R.string.history_pants),
        ClothingPart("tops", R.string.part_tops),
        ClothingPart("outerwear", R.string.part_outerwear),
        ClothingPart("hats", R.string.part_hats),
        ClothingPart("accessories", R.string.part_accessories)
    )

    val looks = listOf(
        // DRESSES (ancient_medieval)
        FashionLook("1", "ancient_medieval", "dresses", R.string.look_1_title, R.string.look_1_desc, R.string.look_1_full, android.R.drawable.ic_menu_gallery),
        FashionLook("2", "ancient_medieval", "dresses", R.string.look_2_title, R.string.look_2_desc, R.string.look_2_full, android.R.drawable.ic_menu_gallery),
        FashionLook("3", "ancient_medieval", "dresses", R.string.look_3_title, R.string.look_3_desc, R.string.look_3_full, android.R.drawable.ic_menu_gallery),
        FashionLook("4", "ancient_medieval", "dresses", R.string.look_4_title, R.string.look_4_desc, R.string.look_4_full, android.R.drawable.ic_menu_gallery),
        FashionLook("5", "ancient_medieval", "dresses", R.string.look_5_title, R.string.look_5_desc, R.string.look_5_full, android.R.drawable.ic_menu_gallery),

        // PANTS & SKIRTS (ancient_medieval)
        FashionLook("6", "ancient_medieval", "pants_skirts", R.string.look_6_title, R.string.look_6_desc, R.string.look_6_full, android.R.drawable.ic_menu_gallery),
        FashionLook("7", "ancient_medieval", "pants_skirts", R.string.look_7_title, R.string.look_7_desc, R.string.look_7_full, android.R.drawable.ic_menu_gallery),
        FashionLook("8", "ancient_medieval", "pants_skirts", R.string.look_8_title, R.string.look_8_desc, R.string.look_8_full, android.R.drawable.ic_menu_gallery),
        FashionLook("9", "ancient_medieval", "pants_skirts", R.string.look_9_title, R.string.look_9_desc, R.string.look_9_full, android.R.drawable.ic_menu_gallery),
        FashionLook("10", "ancient_medieval", "pants_skirts", R.string.look_10_title, R.string.look_10_desc, R.string.look_10_full, android.R.drawable.ic_menu_gallery)
    )

    val stylists = listOf(
        Stylist("s1", R.string.stylist_1_name, R.string.stylist_1_bio, R.drawable.stylist_4, "https://calendly.com/style-elena"),
        Stylist("s2", R.string.stylist_2_name, R.string.stylist_2_bio, R.drawable.stylist_2, "https://calendly.com/style-mark"),
        Stylist("s3", R.string.stylist_3_name, R.string.stylist_3_bio, R.drawable.stylist_3, "https://calendly.com/style-katya"),
        Stylist("s4", R.string.stylist_4_name, R.string.stylist_4_bio, R.drawable.stylist_1, "https://calendly.com/style-viktor")
    )
}

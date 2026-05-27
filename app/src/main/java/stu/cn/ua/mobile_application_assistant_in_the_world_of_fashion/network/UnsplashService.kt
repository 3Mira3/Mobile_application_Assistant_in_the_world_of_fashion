package stu.cn.ua.mobile_application_assistant_in_the_world_of_fashion.network

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface UnsplashService {
    @GET("photos/random")
    suspend fun getRandomFashionPhoto(
        @Header("Authorization") auth: String = "Client-ID JnDZL7ShcsOEjN-FnI5MkDsnJLxLKu-IEvtwa03xSog",
        @Header("Accept-Version") version: String = "v1",
        @Header("User-Agent") userAgent: String = "FashionAssistantApp/1.0",
        @Query("query") query: String = "high fashion couture 2026",
        @Query("orientation") orientation: String = "squarish"
    ): UnsplashPhoto

    @GET("search/photos")
    suspend fun searchFashionPhotos(
        @Header("Authorization") auth: String = "Client-ID JnDZL7ShcsOEjN-FnI5MkDsnJLxLKu-IEvtwa03xSog",
        @Header("Accept-Version") version: String = "v1",
        @Header("User-Agent") userAgent: String = "FashionAssistantApp/1.0",
        @Query("query") query: String = "fashion trend",
        @Query("per_page") perPage: Int = 15
    ): UnsplashSearchResponse
}

data class UnsplashPhoto(
    val id: String,
    val urls: UnsplashUrls,
    val description: String?,
    val alt_description: String?,
    val user: UnsplashUser
)

data class UnsplashUrls(
    val raw: String,
    val full: String,
    val regular: String,
    val small: String,
    val thumb: String
)

data class UnsplashUser(
    val name: String,
    val username: String
)

data class UnsplashSearchResponse(
    val results: List<UnsplashPhoto>
)

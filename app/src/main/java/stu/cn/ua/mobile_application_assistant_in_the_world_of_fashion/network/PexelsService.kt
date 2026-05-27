package stu.cn.ua.mobile_application_assistant_in_the_world_of_fashion.network

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface PexelsService {
    @GET("search")
    suspend fun searchPhotos(
        @Header("Authorization") apiKey: String,
        @Query("query") query: String,
        @Query("per_page") perPage: Int = 15,
        @Query("page") page: Int = 1
    ): PexelsResponse
}

data class PexelsResponse(
    val photos: List<PexelsPhoto>
)

data class PexelsPhoto(
    val id: Int,
    val src: PexelsSource
)

data class PexelsSource(
    val medium: String,
    val large: String,
    val original: String
)

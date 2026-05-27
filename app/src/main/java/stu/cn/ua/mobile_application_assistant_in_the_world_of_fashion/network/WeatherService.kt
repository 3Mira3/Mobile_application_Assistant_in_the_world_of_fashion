package stu.cn.ua.mobile_application_assistant_in_the_world_of_fashion.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

class WeatherService {
    // Note: In a real production app, use an API key and a library like Retrofit.
    // For this demonstration, we use a public API or a simulated response for speed.
    suspend fun getWeather(city: String, lang: String = "uk"): WeatherData = withContext(Dispatchers.IO) {
        try {
            // Encoding city name for Cyrillic support (e.g. Чернігів -> %D0%A7...)
            val encodedCity = java.net.URLEncoder.encode(city, "UTF-8")
            val response = URL("https://wttr.in/$encodedCity?format=j1&lang=$lang").readText()
            val json = JSONObject(response)
            val currentCondition = json.getJSONArray("current_condition").getJSONObject(0)
            
            val temp = currentCondition.getString("temp_C")
            // Try to get translated description, fallback to English if missing
            val desc = try {
                currentCondition.getJSONArray("lang_$lang").getJSONObject(0).getString("value")
            } catch (e: Exception) {
                currentCondition.getJSONArray("weatherDesc").getJSONObject(0).getString("value")
            }
            
            WeatherData(temp.toInt(), desc)
        } catch (e: Exception) {
            android.util.Log.e("WeatherService", "Error fetching weather for $city", e)
            WeatherData(15, "Хмарно") // Default fallback
        }
    }
}

data class WeatherData(val temp: Int, val description: String)

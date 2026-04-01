package nl.apicheck.client

import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class ApiClient(
    private val apiKey: String,
    private val referer: String? = null,
    timeout: Int = 10
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(timeout.toLong(), TimeUnit.SECONDS)
        .readTimeout(timeout.toLong(), TimeUnit.SECONDS)
        .build()
    
    private val gson = Gson()
    
    companion object {
        private const val API_ENDPOINT = "https://api.apicheck.nl"
    }
    
    // Lookup API
    suspend fun lookup(country: String, postalcode: String, number: String): JsonObject {
        val url = "$API_ENDPOINT/lookup/v1/postalcode/${country.lowercase()}?postalcode=$postalcode&number=$number"
        val json = fetch(url)
        return json.getAsJsonObject("data")
    }
    
    suspend fun getNumberAdditions(country: String, postalcode: String, number: String): JsonObject {
        val url = "$API_ENDPOINT/lookup/v1/address/${country.lowercase()}?postalcode=$postalcode&number=$number&fields=[\"numberAdditions\"]"
        val json = fetch(url)
        return json.getAsJsonObject("data")
    }
    
    // Search API
    suspend fun globalSearch(country: String, query: String, limit: Int = 10): JsonObject {
        val url = "$API_ENDPOINT/search/v1/global/${country.lowercase()}?query=$query&limit=$limit"
        val json = fetch(url)
        return json.getAsJsonObject("data")
    }
    
    // Verify API
    suspend fun verifyEmail(email: String): JsonObject {
        val url = "$API_ENDPOINT/verify/v1/email/?email=$email"
        val json = fetch(url)
        return json.getAsJsonObject("data")
    }
    
    suspend fun verifyPhone(number: String): JsonObject {
        val url = "$API_ENDPOINT/verify/v1/phone/?number=$number"
        val json = fetch(url)
        return json.getAsJsonObject("data")
    }
    
    private fun fetch(url: String): JsonObject {
        val request = Request.Builder()
            .url(url)
            .header("Accept", "application/json")
            .header("X-API-KEY", apiKey)
            .apply {
                if (referer != null) header("Referer", referer)
            }
            .build()
        
        client.newCall(request).execute().use { response ->
            val body = response.body?.string() ?: throw ApiCheckException("Empty response")
            return gson.fromJson(body, JsonObject::class.java)
        }
    }
}

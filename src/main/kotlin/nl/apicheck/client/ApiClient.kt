package nl.apicheck.client

import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
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
        
        val COUNTRIES_ALL = listOf("nl", "be", "lu", "de", "fr", "cz", "fi", "it", "no", "pl", "pt", "ro", "es", "ch", "at", "dk", "gb", "se")
        val COUNTRIES_LOOKUP = listOf("nl", "lu")
    }
    
    private fun encode(value: String): String = URLEncoder.encode(value, StandardCharsets.UTF_8.name())
    
    // ============================================
    // Lookup API (Netherlands, Luxembourg only)
    // ============================================
    
    suspend fun lookup(country: String, postalcode: String, number: String, numberAddition: String? = null): JsonObject {
        val params = buildString {
            append("postalcode=")
            append(encode(postalcode))
            append("&number=")
            append(encode(number))
            if (numberAddition != null) {
                append("&numberAddition=")
                append(encode(numberAddition))
            }
        }
        val url = "$API_ENDPOINT/lookup/v1/postalcode/${country.lowercase()}?$params"
        val json = fetch(url)
        return json.getAsJsonObject("data")
    }
    
    suspend fun getNumberAdditions(country: String, postalcode: String, number: String): JsonObject {
        val url = "$API_ENDPOINT/lookup/v1/address/${country.lowercase()}?postalcode=${encode(postalcode)}&number=${encode(number)}&fields=[\"numberAdditions\"]"
        val json = fetch(url)
        return json.getAsJsonObject("data")
    }
    
    // ============================================
    // Search API (18 European countries)
    // ============================================
    
    suspend fun globalSearch(
        country: String, 
        query: String, 
        limit: Int = 10,
        cityId: Int? = null,
        streetId: Int? = null,
        postalcodeId: Int? = null,
        localityId: Int? = null,
        municipalityId: Int? = null
    ): JsonObject {
        val params = buildString {
            append("query=")
            append(encode(query))
            append("&limit=")
            append(limit)
            cityId?.let { append("&city_id=$it") }
            streetId?.let { append("&street_id=$it") }
            postalcodeId?.let { append("&postalcode_id=$it") }
            localityId?.let { append("&locality_id=$it") }
            municipalityId?.let { append("&municipality_id=$it") }
        }
        val url = "$API_ENDPOINT/search/v1/global/${country.lowercase()}?$params"
        val json = fetch(url)
        return json.getAsJsonObject("data")
    }
    
    suspend fun searchCity(country: String, name: String, limit: Int = 10, cityId: Int? = null): JsonObject {
        val params = buildString {
            append("name=")
            append(encode(name))
            append("&limit=")
            append(limit)
            cityId?.let { append("&city_id=$it") }
        }
        val url = "$API_ENDPOINT/search/v1/city/${country.lowercase()}?$params"
        val json = fetch(url)
        return json.getAsJsonObject("data")
    }
    
    suspend fun searchStreet(country: String, name: String, limit: Int = 10, cityId: Int? = null): JsonObject {
        val params = buildString {
            append("name=")
            append(encode(name))
            append("&limit=")
            append(limit)
            cityId?.let { append("&city_id=$it") }
        }
        val url = "$API_ENDPOINT/search/v1/street/${country.lowercase()}?$params"
        val json = fetch(url)
        return json.getAsJsonObject("data")
    }
    
    suspend fun searchPostalcode(country: String, name: String, limit: Int = 10, cityId: Int? = null): JsonObject {
        val params = buildString {
            append("name=")
            append(encode(name))
            append("&limit=")
            append(limit)
            cityId?.let { append("&city_id=$it") }
        }
        val url = "$API_ENDPOINT/search/v1/postalcode/${country.lowercase()}?$params"
        val json = fetch(url)
        return json.getAsJsonObject("data")
    }
    
    suspend fun searchLocality(country: String, name: String, limit: Int = 10): JsonObject {
        val url = "$API_ENDPOINT/search/v1/locality/${country.lowercase()}?name=${encode(name)}&limit=$limit"
        val json = fetch(url)
        return json.getAsJsonObject("data")
    }
    
    suspend fun searchMunicipality(country: String, name: String, limit: Int = 10): JsonObject {
        val url = "$API_ENDPOINT/search/v1/municipality/${country.lowercase()}?name=${encode(name)}&limit=$limit"
        val json = fetch(url)
        return json.getAsJsonObject("data")
    }
    
    suspend fun searchAddress(
        country: String,
        streetId: Int? = null,
        cityId: Int? = null,
        postalcodeId: Int? = null,
        localityId: Int? = null,
        municipalityId: Int? = null,
        number: String? = null,
        numberAddition: String? = null,
        limit: Int = 10
    ): JsonObject {
        val params = buildString {
            streetId?.let { append("street_id=$it&") }
            cityId?.let { append("city_id=$it&") }
            postalcodeId?.let { append("postalcode_id=$it&") }
            localityId?.let { append("locality_id=$it&") }
            municipalityId?.let { append("municipality_id=$it&") }
            number?.let { append("number=${encode(it)}&") }
            numberAddition?.let { append("numberAddition=${encode(it)}&") }
            append("limit=$limit")
        }
        val url = "$API_ENDPOINT/search/v1/address/${country.lowercase()}?$params"
        val json = fetch(url)
        return json.getAsJsonObject("data")
    }
    
    // ============================================
    // Verify API
    // ============================================
    
    suspend fun verifyEmail(email: String): JsonObject {
        val url = "$API_ENDPOINT/verify/v1/email/?email=${encode(email)}"
        val json = fetch(url)
        return json.getAsJsonObject("data")
    }
    
    suspend fun verifyPhone(number: String): JsonObject {
        val url = "$API_ENDPOINT/verify/v1/phone/?number=${encode(number)}"
        val json = fetch(url)
        return json.getAsJsonObject("data")
    }
    
    // ============================================
    // Internal helpers
    // ============================================
    
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

class ApiCheckException(message: String) : Exception(message)

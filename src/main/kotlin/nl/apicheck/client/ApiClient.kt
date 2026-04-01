package nl.apicheck.client

import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
        private val LOOKUP_COUNTRIES = setOf("nl", "lu")
        private val SEARCH_COUNTRIES = setOf("nl", "be", "lu", "fr", "de", "cz", "fi", "it", "no", "pl", "pt", "ro", "es", "ch", "at", "dk", "gb", "se")
    }
    
    // Lookup API
    suspend fun lookup(country: String, postalcode: String, number: String): LookupResponse = withContext(Dispatchers.IO) {
        val c = country.lowercase()
        if (c !in LOOKUP_COUNTRIES) {
            throw UnsupportedCountryException("Country '$country' not supported for lookup", country)
        }
        
        val url = "$API_ENDPOINT/lookup/v1/address/?country=$c&postalcode=$postalcode&number=$number"
        val json = fetch(url)
        parseLookup(json)
    }
    
    suspend fun getNumberAdditions(country: String, postalcode: String, number: String): NumberAdditionsResponse = withContext(Dispatchers.IO) {
        val c = country.lowercase()
        if (c !in LOOKUP_COUNTRIES) {
            throw UnsupportedCountryException("Country '$country' not supported", country)
        }
        
        val url = "$API_ENDPOINT/lookup/v1/numberadditions/?country=$c&postalcode=$postalcode&number=$number"
        val json = fetch(url)
        NumberAdditionsResponse(
            number = json.get("number").asString,
            numberAdditions = json.getAsJsonArray("numberAdditions").map { it.asString }
        )
    }
    
    // Search API
    suspend fun globalSearch(country: String, query: String, limit: Int? = null): GlobalSearchResponse = withContext(Dispatchers.IO) {
        val c = country.lowercase()
        if (c !in SEARCH_COUNTRIES) {
            throw UnsupportedCountryException("Country '$country' not supported", country)
        }
        
        var url = "$API_ENDPOINT/search/v1/global/?country=$c&query=$query"
        if (limit != null) url += "&limit=$limit"
        
        val json = fetch(url)
        GlobalSearchResponse(
            results = json.getAsJsonArray("results").map { result ->
                val obj = result.asJsonObject
                SearchResult(
                    id = obj.get("id").asInt,
                    name = obj.get("name").asString,
                    type = obj.get("type")?.asString,
                    latitude = obj.get("latitude")?.asDouble,
                    longitude = obj.get("longitude")?.asDouble
                )
            },
            count = json.get("count").asInt
        )
    }
    
    // Verify API
    suspend fun verifyEmail(email: String): EmailVerificationResponse = withContext(Dispatchers.IO) {
        val url = "$API_ENDPOINT/verify/v1/email/?email=$email"
        val json = fetch(url)
        EmailVerificationResponse(
            email = json.get("email").asString,
            status = json.get("status").asString,
            disposableEmail = json.get("disposable_email")?.asBoolean ?: false,
            greylisted = json.get("greylisted")?.asBoolean ?: false
        )
    }
    
    suspend fun verifyPhone(number: String): PhoneVerificationResponse = withContext(Dispatchers.IO) {
        val url = "$API_ENDPOINT/verify/v1/phone/?number=$number"
        val json = fetch(url)
        PhoneVerificationResponse(
            number = json.get("number").asString,
            valid = json.get("valid")?.asBoolean ?: false,
            countryCode = json.get("country_code")?.asString,
            carrier = json.get("carrier")?.asString
        )
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
            when (response.code) {
                401 -> throw AuthenticationException()
                429 -> throw RateLimitException(response.header("Retry-After")?.toInt())
                in 400..599 -> throw ApiCheckException("API error: ${response.code}", response.code)
            }
            
            val body = response.body?.string() ?: throw ApiCheckException("Empty response")
            return gson.fromJson(body, JsonObject::class.java)
        }
    }
    
    private fun parseLookup(json: JsonObject): LookupResponse {
        val countryObj = json.getAsJsonObject("country")
        val coordsObj = json.getAsJsonObject("coordinates")
        
        return LookupResponse(
            street = json.get("street").asString,
            number = json.get("number").asString,
            postalcode = json.get("postalcode").asString,
            city = json.get("city").asString,
            country = Country(
                name = countryObj.get("name").asString,
                code = countryObj.get("code").asString
            ),
            coordinates = Coordinates(
                latitude = coordsObj.get("latitude").asDouble,
                longitude = coordsObj.get("longitude").asDouble
            ),
            streetShort = json.get("streetShort")?.asString,
            numberAddition = json.get("numberAddition")?.asString,
            municipality = json.get("municipality")?.asString,
            province = json.get("province")?.asString
        )
    }
}

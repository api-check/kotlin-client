package nl.apicheck.client

data class Country(val name: String, val code: String)

data class Coordinates(val latitude: Double, val longitude: Double)

data class LookupResponse(
    val street: String,
    val number: String,
    val postalcode: String,
    val city: String,
    val country: Country,
    val coordinates: Coordinates,
    val streetShort: String? = null,
    val numberAddition: String? = null,
    val municipality: String? = null,
    val province: String? = null
)

data class NumberAdditionsResponse(
    val number: String,
    val numberAdditions: List<String>
)

data class SearchResult(
    val id: Int,
    val name: String,
    val type: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)

data class GlobalSearchResponse(
    val results: List<SearchResult>,
    val count: Int
)

data class EmailVerificationResponse(
    val email: String,
    val status: String,
    val disposableEmail: Boolean,
    val greylisted: Boolean
)

data class PhoneVerificationResponse(
    val number: String,
    val valid: Boolean,
    val countryCode: String? = null,
    val carrier: String? = null
)

open class ApiCheckException(message: String, val statusCode: Int? = null) : Exception(message)
class UnsupportedCountryException(message: String, val country: String) : ApiCheckException(message, 400)
class AuthenticationException : ApiCheckException("Invalid API key", 401)
class RateLimitException(val retryAfter: Int?) : ApiCheckException("Rate limit exceeded", 429)

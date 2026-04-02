package nl.apicheck.client

data class Country(val name: String, val code: String)

data class Address(
    val street: String,
    val number: String,
    val postalcode: String,
    val city: String,
    val province: String? = null,
    val country: Country
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
    val results: List<SearchResult>
)

data class EmailVerification(
    val status: String,
    val disposableEmail: Boolean? = null,
    val greylisted: Boolean? = null
)

data class PhoneVerification(
    val valid: Boolean,
    val countryCode: String? = null,
    val formattedNumber: String? = null
)

// Exception classes
open class ApiCheckException(message: String, val statusCode: Int? = null) : Exception(message)

class UnsupportedCountryException(message: String, val country: String) : ApiCheckException(message, 400)
class AuthenticationException(message: String) : ApiCheckException(message, 401)
class RateLimitException(val retryAfter: Int? = null) : ApiCheckException("Rate limit exceeded", 429)

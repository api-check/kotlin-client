# ApiCheck Kotlin Client

Address validation, search, and verification for 18 European countries.

## Installation

Add to your `build.gradle.kts`:

```kotlin
implementation("nl.apicheck:apicheck-client:2.0.0")
```

Or `build.gradle`:

```groovy
implementation 'nl.apicheck:apicheck-client:2.0.0'
```

## Quick Start

```kotlin
import nl.apicheck.client.ApiClient

val client = ApiClient("your-api-key")
```

## Global Search (Recommended)

The **global search** endpoint is the most powerful way to find addresses. It searches across streets, cities, and postal codes in one query with powerful filtering options.

```kotlin
// Basic search - finds streets, cities, and postal codes
val results = client.globalSearch("nl", query = "Amsterdam", limit = 10)

results.getAsJsonObject("Results").let { res ->
    res.getAsJsonArray("Streets")?.forEach { street ->
        println("${street.asJsonObject.get("name")?.asString} (street)")
    }
    res.getAsJsonArray("Cities")?.forEach { city ->
        println("${city.asJsonObject.get("name")?.asString} (city)")
    }
    res.getAsJsonArray("Postalcodes")?.forEach { pc ->
        println("${pc.asJsonObject.get("name")?.asString} (postalcode)")
    }
}

// Filter by city - only return results within a specific city
val cityResults = client.globalSearch(
    "nl", 
    query = "Dam", 
    cityId = 2465, 
    limit = 10
)

// Filter by street - only return results on a specific street  
val streetResults = client.globalSearch(
    "nl", 
    query = "1", 
    streetId = 12345, 
    limit = 10
)

// Filter by postal code area
val pcResults = client.globalSearch(
    "nl", 
    query = "A", 
    postalcodeId = 54321, 
    limit = 10
)

// Belgium: filter by locality (deelgemeente)
val locResults = client.globalSearch(
    "be", 
    query = "Hoofd", 
    localityId = 111, 
    limit = 10
)

// Belgium: filter by municipality (gemeente)
val munResults = client.globalSearch(
    "be", 
    query = "Station", 
    municipalityId = 222, 
    limit = 10
)
```

### Global Search Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `country` | String | Country code (nl, be, lu, de, fr, cz, fi, it, no, pl, pt, ro, es, ch, at, dk, gb, se) |
| `query` | String | Search term (street name, city name, or postal code) |
| `limit` | Int | Maximum results (default: 10) |
| `cityId` | Int? | Filter results to a specific city |
| `streetId` | Int? | Filter results to a specific street |
| `postalcodeId` | Int? | Filter results to a specific postal code area |
| `localityId` | Int? | Filter results to a specific locality (Belgium) |
| `municipalityId` | Int? | Filter results to a specific municipality (Belgium) |

### Result Types

Results are grouped in the `Results` object:

- `Streets` - Array of street matches
- `Cities` - Array of city matches
- `Postalcodes` - Array of postal code matches

## Address Lookup (Netherlands & Luxembourg)

For exact address lookup by postal code and house number:

```kotlin
// Basic lookup
val address = client.lookup("nl", postalcode = "1012LM", number = "1")
println(address.get("street").asString)  // Damrak
println(address.get("city").asString)    // Amsterdam

// With number addition (apartment/suite)
val addressWithAddition = client.lookup(
    "nl", 
    postalcode = "1012LM", 
    number = "1",
    numberAddition = "A"
)

// Get available number additions for an address
val additions = client.getNumberAdditions("nl", "1012LM", "1")
println(additions.get("numberAdditions").asJsonArray)  // ["A", "B", "1-3"]
```

## Individual Search Endpoints

```kotlin
// Search cities
val cities = client.searchCity("nl", name = "Amsterdam", limit = 10)

// Search streets
val streets = client.searchStreet("nl", name = "Damrak", limit = 10)
val streetsInCity = client.searchStreet("nl", name = "Dam", cityId = 2465, limit = 10)

// Search postal codes
val postalcodes = client.searchPostalcode("nl", name = "1012", limit = 10)

// Search localities (Belgium primarily)
val localities = client.searchLocality("be", name = "Antwerpen", limit = 10)

// Search municipalities (Belgium primarily)
val municipalities = client.searchMunicipality("be", name = "Antwerpen", limit = 10)

// Resolve full address using IDs from other searches
val addresses = client.searchAddress(
    "nl",
    cityId = 2465,
    number = "1",
    numberAddition = "A",
    limit = 10
)
```

## Verification

```kotlin
// Verify email
val emailResult = client.verifyEmail("test@example.com")
println(emailResult.get("status").asString)          // valid, invalid, or unknown
println(emailResult.get("disposable_email").asBoolean) // true if disposable
println(emailResult.get("greylisted").asBoolean)     // true if greylisted

// Verify phone number
val phoneResult = client.verifyPhone("+31612345678")
println(phoneResult.get("valid").asBoolean)          // true if valid
println(phoneResult.get("country_code").asString)    // NL
```

## Supported Countries

### All Search Endpoints (18 countries)
`nl`, `be`, `lu`, `de`, `fr`, `cz`, `fi`, `it`, `no`, `pl`, `pt`, `ro`, `es`, `ch`, `at`, `dk`, `gb`, `se`

### Address Lookup (Netherlands & Luxembourg only)
`nl`, `lu`

## API Key

Get your API key at [app.apicheck.nl](https://app.apicheck.nl)

## Options

```kotlin
val client = ApiClient(
    apiKey = "your-api-key",
    referer = "https://yoursite.com",  // Required if API key has "Allowed Hosts" enabled
    timeout = 15 // Request timeout in seconds (default: 10)
)
```

## Coroutines

All methods are `suspend` functions and should be called from a coroutine scope:

```kotlin
lifecycleScope.launch {
    val results = client.globalSearch("nl", query = "Amsterdam", limit = 10)
    // Process results
}
```

## Tips

1. **Use Global Search first** - It's the most flexible and covers all use cases
2. **Filter for precision** - Use cityId, streetId, etc. to narrow down results
3. **Chain searches** - Use Search City to get a cityId, then use it in Global Search or Search Address
4. **Belgium addresses** - Use localityId and municipalityId filters for precise results

## License

MIT

# ApiCheck Kotlin Client

Kotlin client for ApiCheck - address validation, search, and verification.

## Installation

Add to your `build.gradle.kts`:

```kotlin
implementation("nl.apicheck:apicheck-client:1.0.0")
```

Or `build.gradle`:

```groovy
implementation 'nl.apicheck:apicheck-client:1.0.0'
```

## Usage

```kotlin
import nl.apicheck.client.ApiClient
import kotlinx.coroutines.runBlocking

val client = ApiClient("your-api-key")

// Lookup address (NL, LU)
val address = client.lookup("nl", "1012LM", "1")
println("${address.street} ${address.number}, ${address.city}")

// Global search (18 countries)
val results = client.globalSearch("nl", "amsterdam")

// Verify email
val email = client.verifyEmail("test@example.com")
println(email.status) // valid, invalid, unknown

// Verify phone
val phone = client.verifyPhone("+31612345678")
println(phone.valid) // true/false
```

## Coroutines

All API methods are `suspend` functions. Use with coroutines:

```kotlin
lifecycleScope.launch {
    val address = client.lookup("nl", "1012LM", "1")
}

// Or runBlocking for simple scripts
runBlocking {
    val address = client.lookup("nl", "1012LM", "1")
}
```

## API

### `lookup(country, postalcode, number)`
Address lookup for NL and LU.

### `getNumberAdditions(country, postalcode, number)`
Get available number additions.

### `globalSearch(country, query, limit?)`
Global search across 18 countries.

### `verifyEmail(email)`
Verify email address.

### `verifyPhone(number)`
Verify phone number.

## Requirements

- Kotlin 1.9+
- Java 11+

## License

MIT

## Support

- Website: [apicheck.nl](https://apicheck.nl)
- Email: support@apicheck.nl

# ApiCheck Kotlin Client

Address validation, search, and verification for 18 European countries.

## Installation
Add to your `build.gradle.kts`:

implementation("nl.apicheck:apicheck-client:2.0.0")

## Quick Start
import nl.apicheck.client.ApiClient

val client = ApiClient("your-api-key")

## Global Search (Recommended)
The **global search** endpoint is the most powerful way to find addresses. It searches across streets, cities, and postal codes in one query with powerful filtering options.

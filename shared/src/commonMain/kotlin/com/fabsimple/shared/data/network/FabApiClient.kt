package com.fabsimple.shared.data.network

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import com.fabsimple.shared.data.local.LocalDatabase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.channels.BufferOverflow


/**
 * Central HTTP client wrapping the Supabase Edge Function API.
 * Equivalent to `lib/api.ts` in the Next.js codebase.
 *
 * Features:
 * - Automatic JSON envelope unwrapping ({ ok, data, error })
 * - Token injection from local storage
 * - Retry with exponential backoff (up to 3 attempts on 429/5xx)
 * - 401 → session clear + re-throw
 */
class FabApiClient(
    @PublishedApi internal val localDatabase: LocalDatabase,
    val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = false
        encodeDefaults = true
    }
) {
    // Configurable via BuildConfig or runtime settings
    var apiBaseUrl = "https://mteocbcpbdgfdysulmiv.supabase.co/functions/v1/api"
    var anonKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im10ZW9jYmNwYmRnZmR5c3VsbWl2Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3Nzc0MzgyMDEsImV4cCI6MjA5MzAxNDIwMX0.OAF16tBGpkct-eemtMeWsra1CJQ6al3zS8CnQFVgU_o"

    val sessionExpiredEvent = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(json)
        }
        install(Logging) {
            logger = Logger.SIMPLE
            level = LogLevel.ALL
        }
    }

    /**
     * Execute a typed API request with envelope unwrapping and retry logic.
     */
    suspend inline fun <reified T> request(
        path: String,
        method: HttpMethod,
        body: Any? = null,
        queryParams: Map<String, String>? = null,
        maxRetries: Int = 3
    ): T {
        var lastException: Exception? = null

        for (attempt in 0 until maxRetries) {
            try {
                val token = localDatabase.getAuthToken()
                val response: HttpResponse = httpClient.request {
                    url(apiBaseUrl + path)
                    this.method = method
                    headers {
                        append("apikey", anonKey)
                        if (token != null) {
                            append("Authorization", "Bearer $token")
                        }
                        append(HttpHeaders.ContentType, "application/json")
                    }
                    if (body != null) {
                        setBody(body)
                    }
                    queryParams?.forEach { (key, value) ->
                        url.parameters.append(key, value)
                    }
                }

                // Handle 401 — session expired
                if (response.status == HttpStatusCode.Unauthorized) {
                    localDatabase.clearAll()
                    sessionExpiredEvent.tryEmit(Unit)
                    throw SessionExpiredException("Session expired (401 Unauthorized)")
                }

                // Handle 429 / 5xx — retry with backoff
                if (response.status.value == 429 || response.status.value >= 500) {
                    val backoffMs = (1000L * (1 shl attempt)).coerceAtMost(8000L)
                    delay(backoffMs)
                    lastException = Exception("HTTP ${response.status.value}: retrying...")
                    continue
                }

                // Handle other errors
                if (!response.status.isSuccess()) {
                    val errorText = response.bodyAsText()
                    throw ApiException("HTTP ${response.status.value}: $errorText", response.status.value)
                }

                val text = response.bodyAsText()

                // Try envelope unwrap first
                val envelope = try {
                    json.decodeFromString<ApiEnvelope<T>>(text)
                } catch (_: Exception) {
                    // Fallback to direct deserialization
                    return json.decodeFromString<T>(text)
                }

                if (envelope.ok) {
                    return envelope.data
                        ?: throw ApiException("Response OK but data was null", 200)
                } else {
                    throw ApiException(
                        envelope.error?.message ?: "API returned failure envelope",
                        422
                    )
                }
            } catch (e: SessionExpiredException) {
                throw e // Don't retry auth failures
            } catch (e: ApiException) {
                throw e // Don't retry client errors
            } catch (e: Exception) {
                lastException = e
                if (attempt < maxRetries - 1) {
                    val backoffMs = (1000L * (1 shl attempt)).coerceAtMost(8000L)
                    delay(backoffMs)
                } else {
                    throw e
                }
            }
        }

        throw lastException ?: Exception("Request failed after $maxRetries attempts")
    }

    // ─── Convenience Methods ───

    suspend inline fun <reified T> get(
        path: String,
        queryParams: Map<String, String>? = null
    ): T = request(path, HttpMethod.Get, queryParams = queryParams)

    suspend inline fun <reified T> post(
        path: String,
        body: Any? = null
    ): T = request(path, HttpMethod.Post, body = body)

    suspend inline fun <reified T> patch(
        path: String,
        body: Any? = null
    ): T = request(path, HttpMethod.Patch, body = body)

    suspend inline fun <reified T> put(
        path: String,
        body: Any? = null
    ): T = request(path, HttpMethod.Put, body = body)

    suspend inline fun <reified T> delete(
        path: String
    ): T = request(path, HttpMethod.Delete)

    // ─── Raw Supabase Auth (not through Edge Functions) ───

    /**
     * Authenticate against Supabase Auth directly.
     * Returns the raw token response.
     */
    suspend fun supabaseAuthSignIn(email: String, password: String): SbTokenResponse {
        val authUrl = apiBaseUrl.replace("/functions/v1/api", "/auth/v1")
        val response = httpClient.post("$authUrl/token?grant_type=password") {
            header("apikey", anonKey)
            contentType(ContentType.Application.Json)
            setBody(kotlinx.serialization.json.buildJsonObject {
                put("email", kotlinx.serialization.json.JsonPrimitive(email))
                put("password", kotlinx.serialization.json.JsonPrimitive(password))
            })
        }

        if (!response.status.isSuccess()) {
            val errorBody = response.bodyAsText()
            throw ApiException("Sign-in failed: $errorBody", response.status.value)
        }

        return json.decodeFromString<SbTokenResponse>(response.bodyAsText())
    }

    suspend fun supabaseAuthSignUp(email: String, password: String): SbTokenResponse {
        val authUrl = apiBaseUrl.replace("/functions/v1/api", "/auth/v1")
        val response = httpClient.post("$authUrl/signup") {
            header("apikey", anonKey)
            contentType(ContentType.Application.Json)
            setBody(kotlinx.serialization.json.buildJsonObject {
                put("email", kotlinx.serialization.json.JsonPrimitive(email))
                put("password", kotlinx.serialization.json.JsonPrimitive(password))
            })
        }

        if (!response.status.isSuccess()) {
            val errorBody = response.bodyAsText()
            throw ApiException("Sign-up failed: $errorBody", response.status.value)
        }

        return json.decodeFromString<SbTokenResponse>(response.bodyAsText())
    }

    /**
     * Upload raw bytes to a pre-signed URL (for file attachments).
     */
    suspend fun uploadBytes(
        uploadUrl: String,
        bytes: ByteArray,
        contentType: String
    ) {
        val response = httpClient.put(uploadUrl) {
            contentType(ContentType.parse(contentType))
            setBody(bytes)
        }

        if (!response.status.isSuccess()) {
            throw ApiException(
                "File upload failed: ${response.bodyAsText()}",
                response.status.value
            )
        }
    }
}

// ─── Custom Exceptions ───

class SessionExpiredException(message: String) : Exception(message)

class ApiException(
    message: String,
    val statusCode: Int
) : Exception(message)

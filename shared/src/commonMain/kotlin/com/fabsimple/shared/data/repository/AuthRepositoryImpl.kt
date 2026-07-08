package com.fabsimple.shared.data.repository

import com.fabsimple.shared.data.local.LocalDatabase
import com.fabsimple.shared.data.network.*
import com.fabsimple.shared.domain.model.*
import com.fabsimple.shared.domain.repository.AuthRepository
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


class AuthRepositoryImpl(
    private val apiClient: FabApiClient,
    private val localDatabase: LocalDatabase,
    private val json: Json = Json { ignoreUnknownKeys = true }
) : AuthRepository {
 
    override val sessionExpired: SharedFlow<Unit> = apiClient.sessionExpiredEvent.asSharedFlow()

    private val _currentSession = MutableStateFlow<UserSession?>(null)
    override val currentSession: StateFlow<UserSession?> = _currentSession.asStateFlow()

    init {
        _currentSession.value = getSession()
    }


    override suspend fun signIn(email: String, password: String): UserSession {
        // 1. Authenticate against Supabase Auth
        val tokenResp = apiClient.supabaseAuthSignIn(email, password)
        val accessToken = tokenResp.access_token
        val authId = tokenResp.user.id
        val userRole = tokenResp.user.app_metadata?.role

        // 2. Cache token so subsequent API calls are authorized
        localDatabase.saveAuthToken(accessToken)

        // 3. Fetch user profile/role
        val users: List<UserProfile> = apiClient.get("/users")
        val profile = users.firstOrNull { it.id == authId }
            ?: UserProfile(id = authId, full_name = email.substringBefore("@"), role = userRole)

        // 4. Persist session locally
        localDatabase.saveUserId(profile.id)
        localDatabase.saveUserName(profile.full_name)
        localDatabase.saveUserRole(profile.role)
        localDatabase.saveUserEmail(email)

        val session = UserSession(
            userId = profile.id,
            email = email,
            name = profile.full_name,
            role = profile.role,
            token = accessToken
        )
        _currentSession.value = session
        return session
    }

    override suspend fun signUp(
        email: String,
        password: String,
        fullName: String,
        companyName: String
    ) {
        val tokenResp = apiClient.supabaseAuthSignUp(email, password)
        localDatabase.saveAuthToken(tokenResp.access_token)

        apiClient.post<Unit>(
            "/signup-bootstrap",
            SignupBootstrapRequest(
                auth_id = tokenResp.user.id,
                email = email,
                full_name = fullName,
                company_name = companyName
            )
        )
    }

    override suspend fun acceptInvite(token: String, password: String, fullName: String) {
        apiClient.post<Unit>(
            "/accept-invite",
            AcceptInviteRequest(token = token, password = password, full_name = fullName)
        )
    }

    override suspend fun forgotPassword(email: String) {
        val authUrl = apiClient.apiBaseUrl.replace("/functions/v1/api", "/auth/v1")
        apiClient.httpClient.post("$authUrl/recover") {
            header("apikey", apiClient.anonKey)
            contentType(ContentType.Application.Json)
            setBody(
                kotlinx.serialization.json.buildJsonObject {
                    put("email", kotlinx.serialization.json.JsonPrimitive(email))
                }
            )
        }
    }

    override fun getSession(): UserSession? {
        val token = localDatabase.getAuthToken() ?: return null
        val id = localDatabase.getUserId() ?: return null
        val name = localDatabase.getUserName() ?: ""
        val role = localDatabase.getUserRole() ?: "owner"
        val email = localDatabase.getUserEmail() ?: ""
        return UserSession(userId = id, email = email, name = name, role = role, token = token)
    }

    override fun signOut() {
        localDatabase.clearAll()
        _currentSession.value = null
    }
}

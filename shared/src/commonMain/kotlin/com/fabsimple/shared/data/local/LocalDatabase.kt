package com.fabsimple.shared.data.local

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import com.fabsimple.shared.domain.model.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

/**
 * Local key-value storage for auth tokens, cached data, and offline queue.
 * Uses Multiplatform Settings (SharedPreferences on Android, NSUserDefaults on iOS).
 *
 * Phase 2 can upgrade high-volume caches (parts, projects) to Room KMP tables.
 * For MVP the Settings-based approach keeps complexity minimal.
 */
class LocalDatabase(private val settings: Settings = Settings()) {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    // ─── Auth ───

    fun saveAuthToken(token: String?) {
        if (token == null) settings.remove(KEY_AUTH_TOKEN) else settings[KEY_AUTH_TOKEN] = token
    }
    fun getAuthToken(): String? = settings.getStringOrNull(KEY_AUTH_TOKEN)

    fun saveUserId(id: String?) {
        if (id == null) settings.remove(KEY_USER_ID) else settings[KEY_USER_ID] = id
    }
    fun getUserId(): String? = settings.getStringOrNull(KEY_USER_ID)

    fun saveUserName(name: String?) {
        if (name == null) settings.remove(KEY_USER_NAME) else settings[KEY_USER_NAME] = name
    }
    fun getUserName(): String? = settings.getStringOrNull(KEY_USER_NAME)

    fun saveUserRole(role: String?) {
        if (role == null) settings.remove(KEY_USER_ROLE) else settings[KEY_USER_ROLE] = role
    }
    fun getUserRole(): String? = settings.getStringOrNull(KEY_USER_ROLE)

    fun saveUserEmail(email: String?) {
        if (email == null) settings.remove(KEY_USER_EMAIL) else settings[KEY_USER_EMAIL] = email
    }
    fun getUserEmail(): String? = settings.getStringOrNull(KEY_USER_EMAIL)

    // ─── Parts Cache ───

    fun cacheParts(parts: List<Part>) {
        try {
            settings[KEY_CACHED_PARTS] = json.encodeToString(parts)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getCachedParts(): List<Part> {
        val data = settings.getStringOrNull(KEY_CACHED_PARTS) ?: return emptyList()
        return try {
            json.decodeFromString(data)
        } catch (_: Exception) { emptyList() }
    }

    // ─── Projects Cache ───

    fun cacheProjects(projects: List<Project>) {
        try {
            settings[KEY_CACHED_PROJECTS] = json.encodeToString(projects)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getCachedProjects(): List<Project> {
        val data = settings.getStringOrNull(KEY_CACHED_PROJECTS) ?: return emptyList()
        return try {
            json.decodeFromString(data)
        } catch (_: Exception) { emptyList() }
    }

    // ─── Generic Cache ───

    fun <T> cacheEntity(key: String, serializer: kotlinx.serialization.KSerializer<List<T>>, items: List<T>) {
        try {
            settings[key] = json.encodeToString(serializer, items)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun <T> getCachedEntity(key: String, serializer: kotlinx.serialization.KSerializer<List<T>>): List<T> {
        val data = settings.getStringOrNull(key) ?: return emptyList()
        return try {
            json.decodeFromString(serializer, data)
        } catch (_: Exception) { emptyList() }
    }

    // ─── Offline Action Queue ───

    fun queueOfflineAction(action: OfflineAction) {
        val current = getOfflineQueue().toMutableList()
        current.add(action)
        saveOfflineQueue(current)
    }

    fun getOfflineQueue(): List<OfflineAction> {
        val data = settings.getStringOrNull(KEY_OFFLINE_QUEUE) ?: return emptyList()
        return try {
            json.decodeFromString(data)
        } catch (_: Exception) { emptyList() }
    }

    fun saveOfflineQueue(queue: List<OfflineAction>) {
        try {
            settings[KEY_OFFLINE_QUEUE] = json.encodeToString(queue)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun clearOfflineQueue() = settings.remove(KEY_OFFLINE_QUEUE)

    // ─── Active Project ───

    fun saveActiveProjectId(id: String?) {
        if (id == null) settings.remove(KEY_ACTIVE_PROJECT) else settings[KEY_ACTIVE_PROJECT] = id
    }
    fun getActiveProjectId(): String? = settings.getStringOrNull(KEY_ACTIVE_PROJECT)

    // ─── API Config ───

    fun saveApiBaseUrl(url: String) { settings[KEY_API_BASE_URL] = url }
    fun getApiBaseUrl(): String? = settings.getStringOrNull(KEY_API_BASE_URL)

    fun saveAnonKey(key: String) { settings[KEY_ANON_KEY] = key }
    fun getAnonKey(): String? = settings.getStringOrNull(KEY_ANON_KEY)

    // ─── Clear All ───

    fun clearAll() = settings.clear()

    companion object {
        private const val KEY_AUTH_TOKEN = "fab_auth_token"
        private const val KEY_USER_ID = "fab_user_id"
        private const val KEY_USER_NAME = "fab_user_name"
        private const val KEY_USER_ROLE = "fab_user_role"
        private const val KEY_USER_EMAIL = "fab_user_email"
        private const val KEY_CACHED_PARTS = "fab_cached_parts"
        private const val KEY_CACHED_PROJECTS = "fab_cached_projects"
        private const val KEY_OFFLINE_QUEUE = "fab_offline_queue"
        private const val KEY_ACTIVE_PROJECT = "fab_active_project_id"
        private const val KEY_API_BASE_URL = "fab_api_base_url"
        private const val KEY_ANON_KEY = "fab_anon_key"
    }
}

package com.fabsimple.shared.data.repository

import com.fabsimple.shared.data.local.LocalDatabase
import com.fabsimple.shared.data.network.FabApiClient
import com.fabsimple.shared.domain.model.*
import com.fabsimple.shared.domain.repository.PartRepository
import kotlinx.datetime.Clock
import kotlinx.serialization.json.*
import kotlin.random.Random

class PartRepositoryImpl(
    private val apiClient: FabApiClient,
    private val localDatabase: LocalDatabase,
    private val json: Json = Json { ignoreUnknownKeys = true }
) : PartRepository {

    private var onlineStatus = true

    override fun isOnline(): Boolean = onlineStatus

    override fun setOnline(online: Boolean) {
        onlineStatus = online
    }

    override fun getOfflineQueue(): List<OfflineAction> = localDatabase.getOfflineQueue()

    override suspend fun getParts(projectId: String?): List<Part> {
        if (!isOnline()) return localDatabase.getCachedParts()

        return try {
            val queryParams = mutableMapOf<String, String>()
            if (projectId != null) queryParams["project_id"] = projectId

            val parts: List<Part> = apiClient.get(
                "/parts",
                queryParams.ifEmpty { null }
            )
            localDatabase.cacheParts(parts)
            parts
        } catch (e: Exception) {
            // Offline fallback
            localDatabase.getCachedParts()
        }
    }

    override suspend fun getPartById(id: String): Part {
        if (!isOnline()) {
            return localDatabase.getCachedParts().firstOrNull { it.id == id }
                ?: throw Exception("Part not cached locally")
        }
        return apiClient.get("/parts/$id")
    }

    override suspend fun updatePartStatus(id: String, payload: Map<String, String>): Part {
        val payloadObj = buildJsonObject {
            payload.forEach { (k, v) ->
                when {
                    v == "true" -> put(k, true)
                    v == "false" -> put(k, false)
                    v.toDoubleOrNull() != null -> put(k, v.toDouble())
                    else -> put(k, v)
                }
            }
        }

        // Optimistic update to local cache
        val cached = localDatabase.getCachedParts().toMutableList()
        val index = cached.indexOfFirst { it.id == id }
        var optimisticPart: Part? = null
        if (index != -1) {
            val oldPart = cached[index]
            optimisticPart = json.decodeFromString<Part>(
                json.encodeToString(
                    buildJsonObject {
                        json.encodeToJsonElement(oldPart).jsonObject.forEach { (k, v) -> put(k, v) }
                        payloadObj.forEach { (k, v) -> put(k, v) }
                    }
                )
            )
            cached[index] = optimisticPart
            localDatabase.cacheParts(cached)
        }

        if (!isOnline()) {
            val action = OfflineAction(
                id = Random.nextInt().toString(),
                partId = id,
                payloadJson = json.encodeToString(payloadObj),
                timestamp = Clock.System.now().toString()
            )
            localDatabase.queueOfflineAction(action)
            return optimisticPart ?: throw Exception("Part not cached locally")
        }

        return apiClient.patch("/parts/$id", payloadObj)
    }

    override suspend fun createPart(part: Part): Part {
        return apiClient.post("/parts", part)
    }

    override suspend fun deletePart(id: String) {
        apiClient.delete<Unit>("/parts/$id")
    }

    override suspend fun flushOfflineQueue() {
        if (!isOnline()) return
        val queue = localDatabase.getOfflineQueue()
        if (queue.isEmpty()) return

        val failed = mutableListOf<OfflineAction>()
        for (action in queue) {
            try {
                val payloadObj = json.parseToJsonElement(action.payloadJson).jsonObject
                apiClient.patch<Part>("/parts/${action.partId}", payloadObj)
            } catch (_: Exception) {
                failed.add(action)
            }
        }
        localDatabase.saveOfflineQueue(failed)
    }
}

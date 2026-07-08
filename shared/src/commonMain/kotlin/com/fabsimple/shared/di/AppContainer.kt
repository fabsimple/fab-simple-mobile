package com.fabsimple.shared.di

import com.fabsimple.shared.data.local.LocalDatabase
import com.fabsimple.shared.data.network.FabApiClient
import com.fabsimple.shared.data.repository.*
import com.fabsimple.shared.domain.repository.*

/**
 * Manual dependency injection container.
 * In a production app, consider replacing with Koin or kotlin-inject.
 *
 * Usage: `val repo = AppContainer.partRepository`
 */
object AppContainer {

    val localDatabase: LocalDatabase by lazy { LocalDatabase() }

    val apiClient: FabApiClient by lazy {
        FabApiClient(localDatabase).also { client ->
            // Apply persisted config if available
            localDatabase.getApiBaseUrl()?.let { client.apiBaseUrl = it }
            localDatabase.getAnonKey()?.let { client.anonKey = it }
        }
    }

    // ─── Repositories ───

    val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(apiClient, localDatabase)
    }

    val partRepository: PartRepository by lazy {
        PartRepositoryImpl(apiClient, localDatabase)
    }

    val projectRepository: ProjectRepository by lazy {
        ProjectRepositoryImpl(apiClient, localDatabase)
    }

    val drawingRepository: DrawingRepository by lazy {
        DrawingRepositoryImpl(apiClient)
    }

    val qcRepository: QcRepository by lazy {
        QcRepositoryImpl(apiClient)
    }

    val procurementRepository: ProcurementRepository by lazy {
        ProcurementRepositoryImpl(apiClient)
    }

    val financeRepository: FinanceRepository by lazy {
        FinanceRepositoryImpl(apiClient)
    }

    val logisticsRepository: LogisticsRepository by lazy {
        LogisticsRepositoryImpl(apiClient)
    }

    val productionRepository: ProductionRepository by lazy {
        ProductionRepositoryImpl(apiClient)
    }

    val adminRepository: AdminRepository by lazy {
        AdminRepositoryImpl(apiClient)
    }

    val fileRepository: FileRepository by lazy {
        FileRepositoryImpl(apiClient)
    }

    val dashboardRepository: DashboardRepository by lazy {
        DashboardRepositoryImpl(apiClient)
    }

    val searchRepository: SearchRepository by lazy {
        SearchRepositoryImpl(apiClient)
    }

    val copilotRepository: CopilotRepository by lazy {
        CopilotRepositoryImpl(apiClient)
    }

    val cutOptimizerRepository: CutOptimizerRepository by lazy {
        CutOptimizerRepositoryImpl(apiClient)
    }

    /**
     * Configure the API endpoint and anon key at runtime.
     * Call this once during app initialization.
     */
    fun configure(apiBaseUrl: String, anonKey: String) {
        apiClient.apiBaseUrl = apiBaseUrl
        apiClient.anonKey = anonKey
        localDatabase.saveApiBaseUrl(apiBaseUrl)
        localDatabase.saveAnonKey(anonKey)
    }
}

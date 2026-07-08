package com.fabsimple.shared.data.network

import kotlinx.serialization.Serializable
import com.fabsimple.shared.domain.model.ChatMessage

// ─── API Envelope ───

@Serializable
data class ApiEnvelope<T>(
    val ok: Boolean,
    val data: T? = null,
    val error: ApiErrorDetail? = null
)

@Serializable
data class ApiErrorDetail(
    val message: String,
    val code: String? = null
)

// ─── Auth Requests / Responses ───

@Serializable
data class SignupBootstrapRequest(
    val auth_id: String,
    val email: String,
    val full_name: String,
    val company_name: String
)

@Serializable
data class InviteUserRequest(
    val email: String,
    val role: String,
    val full_name: String
)

@Serializable
data class AcceptInviteRequest(
    val token: String,
    val password: String,
    val full_name: String
)

@Serializable
data class SbTokenResponse(
    val access_token: String,
    val refresh_token: String? = null,
    val token_type: String? = null,
    val expires_in: Int? = null,
    val user: SbUser
)

@Serializable
data class SbUser(
    val id: String,
    val email: String? = null,
    val app_metadata: app_metadata? = null
)

@Serializable
data class app_metadata(
    val role: String? = null,
)

// ─── Cut Optimizer ───

@Serializable
data class CutInputItem(
    val length: Double,
    val qty: Int = 1,
    val mark: String? = null
)

@Serializable
data class CutOptimizeRequest(
    val project_id: String? = null,
    val profile: String,
    val stock_length: Double,
    val kerf: Double = 0.125,
    val min_remnant: Double = 6.0,
    val cuts: List<CutInputItem>
)

// ─── Copilot ───

@Serializable
data class CopilotRequest(
    val messages: List<ChatMessage>,
    val project_id: String? = null
)

@Serializable
data class CopilotResponse(
    val reply: String,
    val tokens_used: Int? = null
)

// ─── File Upload ───

@Serializable
data class SignUploadRequest(
    val bucket: String,
    val filename: String,
    val entity_type: String,
    val entity_id: String,
    val content_type: String? = null
)

@Serializable
data class SignUploadResponse(
    val upload_url: String,
    val token: String,
    val storage_path: String,
    val attachment_id: String,
    val bucket: String
)

@Serializable
data class SignReadResponse(
    val url: String,
    val mime_type: String? = null,
    val size_bytes: Long? = null,
    val entity_type: String? = null,
    val entity_id: String? = null,
    val expires_in: Int? = null
)

// ─── Search ───

@Serializable
data class SearchResponse(
    val hits: List<com.fabsimple.shared.domain.model.SearchHit>
)

// ─── Dashboard Aggregates ───

@Serializable
data class DashboardProject(
    val id: String,
    val name: String,
    val number: String,
    val gc_name: String? = null,
    val contract_value: Double? = null,
    val deadline: String? = null,
    val status: String,
    val color: String? = null,
    val pm_id: String? = null,
    val total_parts: Int = 0,
    val completed: Int = 0,
    val progress: Double = 0.0
)

@Serializable
data class DashboardFinancial(
    val backlog: Double = 0.0,
    val billed: Double = 0.0,
    val retainage_held: Double = 0.0,
    val collected: Double = 0.0,
    val po_total: Double = 0.0
)

@Serializable
data class DashboardCertAlert(
    val id: String,
    val cert_type: String,
    val holder_name: String,
    val expiry_date: String,
    val alert_days: Int = 30
)

@Serializable
data class DashboardInventoryAlert(
    val id: String,
    val profile: String,
    val grade: String? = null,
    val quantity: Double = 0.0,
    val reorder_point: Double = 0.0,
    val status: String = "low"
)

@Serializable
data class DashboardActivity(
    val id: String,
    val user_name: String? = null,
    val action: String,
    val entity_type: String,
    val entity_label: String? = null,
    val created_at: String
)

@Serializable
data class DashboardNcr(
    val id: String,
    val ncr_number: String,
    val description: String,
    val status: String
)

@Serializable
data class DashboardCo(
    val id: String,
    val co_number: String,
    val amount: Double = 0.0,
    val status: String
)

@Serializable
data class DashboardRfi(
    val id: String,
    val rfi_number: String,
    val question: String,
    val status: String
)

@Serializable
data class DashboardRecentPart(
    val id: String,
    val part_mark: String,
    val profile: String,
    val status: String,
    val project_id: String? = null,
    val project_name: String? = null
)

@Serializable
data class DashboardProductionDay(
    val date: String,
    val parts_completed: Int
)

@Serializable
data class DashboardData(
    val parts_by_status: Map<String, Int> = emptyMap(),
    val total_parts: Int = 0,
    val total_weight: Double = 0.0,
    val projects: List<DashboardProject> = emptyList(),
    val financial: DashboardFinancial? = null,
    val cert_alerts: List<DashboardCertAlert> = emptyList(),
    val inventory_alerts: List<DashboardInventoryAlert> = emptyList(),
    val activity: List<DashboardActivity> = emptyList(),
    val open_ncrs: List<DashboardNcr> = emptyList(),
    val open_change_orders: List<DashboardCo> = emptyList(),
    val open_rfis: List<DashboardRfi> = emptyList(),
    val recent_parts: List<DashboardRecentPart> = emptyList(),
    val production_by_day: List<DashboardProductionDay> = emptyList()
)

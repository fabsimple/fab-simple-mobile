package com.fabsimple.shared.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ─── Auth & Users ───

@Serializable
data class UserSession(
    val userId: String,
    val email: String,
    val name: String,
    val role: String?,
    val token: String
)

@Serializable
data class UserProfile(
    val id: String,
    val full_name: String,
    val email: String? = null,
    val role: String?,
    val company_id: String? = null,
    val avatar_url: String? = null,
    val phone: String? = null,
    val active: Boolean = true
)

@Serializable
data class Organization(
    val id: String,
    val name: String,
    val legal_name: String? = null,
    val address_line1: String? = null,
    val address_line2: String? = null,
    val city: String? = null,
    val state: String? = null,
    val zip: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val website: String? = null,
    val license_number: String? = null,
    val tax_id: String? = null,
    val logo_url: String? = null,
    val plan: String = "free",
    val aisc_cert: Boolean = false,
    val max_parts: Int = 500,
    val max_projects: Int = 5,
    val max_users: Int = 10,
    val active: Boolean = true,
    val default_exclusions_qualifications: String? = null
)

// ─── Projects ───

@Serializable
data class Project(
    val id: String,
    val name: String,
    val number: String,
    val gc_name: String? = null,
    val contract_value: Double? = null,
    val deadline: String? = null,
    val status: String = "planning",
    val color: String? = null,
    val pm_id: String? = null,
    val address: String? = null,
    val scope: String? = null,
    val total_parts: Int = 0,
    val completed: Int = 0,
    val progress: Double = 0.0,
    val total_weight: Double = 0.0,
    val created_at: String? = null,
    val updated_at: String? = null
)

@Serializable
data class Estimate(
    val id: String,
    val project_name: String,
    val gc_name: String? = null,
    val bid_amount: Double? = null,
    val status: String = "draft",
    val due_date: String? = null,
    val tonnage: Double? = null,
    val notes: String? = null,
    val created_at: String? = null
)

// ─── Parts & Production ───

@Serializable
data class Part(
    val id: String,
    val part_mark: String,
    val profile: String,
    val status: String = "not_started",
    val grade: String? = null,
    val length: Double? = null,
    val quantity: Int = 1,
    val assembly_mark: String? = null,
    val heat_number: String? = null,
    val finish: String? = null,
    val weight: Double? = null,
    val project_id: String? = null,
    val project_name: String? = null,
    val project_number: String? = null,
    val assigned_user_id: String? = null,
    val sequence: Int? = null,
    val notes: String? = null,
    // Production timestamps
    val cut_completed_by: String? = null,
    val cut_completed_at: String? = null,
    val cut_hours: Double? = null,
    val cut_drop_length: String? = null,
    val fit_completed_by: String? = null,
    val fit_completed_at: String? = null,
    val fit_hours: Double? = null,
    val fit_skipped: Boolean? = null,
    val weld_completed_by: String? = null,
    val weld_completed_at: String? = null,
    val weld_qc_by: String? = null,
    val weld_qc_at: String? = null,
    val weld_hours: Double? = null,
    val weld_skipped: Boolean? = null,
    val finish_completed_by: String? = null,
    val finish_completed_at: String? = null,
    val finish_hours: Double? = null,
    val insp_completed_by: String? = null,
    val insp_completed_at: String? = null,
    val created_at: String? = null,
    val updated_at: String? = null
)

@Serializable
data class Assembly(
    val id: String,
    val assembly_mark: String,
    val project_id: String,
    val description: String? = null,
    val part_count: Int = 0,
    val total_weight: Double = 0.0,
    val status: String = "not_started",
    val created_at: String? = null
)

@Serializable
data class DailyProductionLog(
    val id: String,
    val log_date: String,
    val station: String,
    val shift: String? = null,
    val crew_size: Int? = null,
    val parts_completed: Int = 0,
    val hours_worked: Double? = null,
    val notes: String? = null,
    val logged_by: String? = null,
    val created_at: String? = null
)

// ─── Drawings ───

@Serializable
data class Drawing(
    val id: String,
    val drawing_number: String? = null,
    val filename: String? = null,
    val revision: String? = null,
    val status: String = "current",
    val storage_path: String? = null,
    val mime_type: String? = null,
    val size_bytes: Long? = null,
    val superseded: Boolean = false,
    val project_id: String? = null,
    val created_at: String? = null,
    val url: String? = null
)

// ─── Quality & Compliance ───

@Serializable
data class WeldInspection(
    val id: String,
    val part_id: String? = null,
    val project_id: String? = null,
    val weld_mark: String? = null,
    val inspector_id: String? = null,
    val inspected_at: String? = null,
    val result: String = "PASS",
    val weld_size_pass: Boolean = true,
    val visual_pass: Boolean = true,
    val ndt_type: String? = null,
    val ndt_pass: Boolean? = null,
    val notes: String? = null,
    val created_at: String? = null
)

@Serializable
data class PaintInspection(
    val id: String,
    val part_id: String? = null,
    val project_id: String? = null,
    val inspector_id: String? = null,
    val inspected_at: String? = null,
    val coat: String? = null,
    val dft_reading_1: Double = 0.0,
    val dft_reading_2: Double = 0.0,
    val dft_reading_3: Double = 0.0,
    val average_dft: Double = 0.0,
    val min_spec: Double? = null,
    val max_spec: Double? = null,
    val result: String = "PASS",
    val notes: String? = null,
    val created_at: String? = null
)

@Serializable
data class NcrReport(
    val id: String,
    val ncr_number: String,
    val project_id: String? = null,
    val part_id: String? = null,
    val raised_by_id: String? = null,
    val raised_at: String? = null,
    val description: String,
    val disposition: String? = null,
    val status: String = "open",
    val closed_at: String? = null,
    val closed_by_id: String? = null,
    val created_at: String? = null
)

@Serializable
data class AiscChecklistItem(
    val id: String,
    val project_id: String,
    val category: String,
    val item_text: String,
    val status: String = "pending",
    val checked_by: String? = null,
    val checked_at: String? = null,
    val notes: String? = null,
    val sort_order: Int = 0,
    val created_at: String? = null
)

@Serializable
data class OshaChecklist(
    val id: String,
    val project_id: String? = null,
    val category: String,
    val item_text: String,
    val status: String = "pending",
    val checked_by: String? = null,
    val checked_at: String? = null,
    val notes: String? = null,
    val created_at: String? = null
)

@Serializable
data class Certification(
    val id: String,
    val cert_type: String,
    val holder_name: String,
    val holder_id: String? = null,
    val cert_number: String? = null,
    val issued_date: String? = null,
    val expiry_date: String,
    val status: String = "active",
    val alert_days: Int = 30,
    val notes: String? = null,
    val created_at: String? = null
)

// ─── RFIs & Change Orders ───

@Serializable
data class Rfi(
    val id: String,
    val rfi_number: String,
    val project_id: String? = null,
    val raised_by_id: String? = null,
    val question: String,
    val status: String = "open",
    val answer: String? = null,
    val answered_by: String? = null,
    val answered_at: String? = null,
    val created_at: String? = null
)

@Serializable
data class ChangeOrder(
    val id: String,
    val co_number: String,
    val project_id: String? = null,
    val description: String? = null,
    val amount: Double = 0.0,
    val status: String = "pending",
    val approved_by: String? = null,
    val approved_at: String? = null,
    val created_at: String? = null
)

// ─── Procurement ───

@Serializable
data class PurchaseOrder(
    val id: String,
    val po_number: String,
    val vendor_name: String,
    val project_id: String? = null,
    val status: String = "draft",
    val total_amount: Double = 0.0,
    val ordered_date: String? = null,
    val expected_date: String? = null,
    val received_date: String? = null,
    val notes: String? = null,
    val created_at: String? = null
)

@Serializable
data class ReceivingRecord(
    val id: String,
    val po_id: String? = null,
    val received_date: String,
    val bol_number: String? = null,
    val received_by: String? = null,
    val notes: String? = null,
    val created_at: String? = null
)

@Serializable
data class Inventory(
    val id: String,
    val profile: String,
    val grade: String? = null,
    val length: Double? = null,
    val quantity: Int = 0,
    val location: String? = null,
    val heat_number: String? = null,
    val reorder_point: Int = 0,
    val status: String = "in_stock",
    val created_at: String? = null
)

@Serializable
data class HeatNumber(
    val id: String,
    val heat_number: String,
    val mill: String? = null,
    val grade: String? = null,
    val profile: String? = null,
    val mtr_attached: Boolean = false,
    val project_id: String? = null,
    val created_at: String? = null
)

// ─── Logistics ───

@Serializable
data class ShippingTicket(
    val id: String,
    val ticket_number: String,
    val project_id: String? = null,
    val carrier: String? = null,
    val ship_date: String? = null,
    val status: String = "pending",
    val pieces: Int = 0,
    val weight: Double = 0.0,
    val notes: String? = null,
    val created_at: String? = null
)

@Serializable
data class ErectionSequence(
    val id: String,
    val project_id: String? = null,
    val sequence_number: Int,
    val description: String? = null,
    val part_marks: String? = null,
    val status: String = "pending",
    val created_at: String? = null
)

@Serializable
data class GcContact(
    val id: String,
    val name: String,
    val company: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val role: String? = null,
    val notes: String? = null,
    val created_at: String? = null
)

// ─── Finance ───

@Serializable
data class BillingApplication(
    val id: String,
    val project_id: String? = null,
    val application_number: Int = 1,
    val period_start: String? = null,
    val period_end: String? = null,
    val scheduled_value: Double = 0.0,
    val work_completed: Double = 0.0,
    val materials_stored: Double = 0.0,
    val retainage_pct: Double = 10.0,
    val retainage_amount: Double = 0.0,
    val amount_due: Double = 0.0,
    val status: String = "draft",
    val created_at: String? = null
)

@Serializable
data class JobCostEntry(
    val id: String,
    val project_id: String? = null,
    val category: String,
    val description: String? = null,
    val budgeted: Double = 0.0,
    val actual: Double = 0.0,
    val variance: Double = 0.0,
    val created_at: String? = null
)

// ─── Notifications & Activity ───

@Serializable
data class Notification(
    val id: String,
    val user_id: String? = null,
    val title: String,
    val body: String? = null,
    val entity_type: String? = null,
    val entity_id: String? = null,
    val read: Boolean = false,
    val created_at: String? = null
)

@Serializable
data class ActivityFeedItem(
    val id: String,
    val user_name: String? = null,
    val action: String,
    val entity_type: String,
    val entity_label: String? = null,
    val created_at: String
)

// ─── Search ───

@Serializable
data class SearchHit(
    val kind: String,
    val id: String,
    val label: String,
    val subtitle: String? = null,
    val href: String
)

// ─── Chat / Copilot ───

@Serializable
data class ChatMessage(
    val role: String,
    val content: String
)

// ─── Cut Optimizer ───

@Serializable
data class CutPlan(
    val total_bars: Int = 0,
    val total_used: Double = 0.0,
    val total_stock: Double = 0.0,
    val yield_percentage: Double = 0.0,
    val waste_percentage: Double = 0.0,
    val bars: List<PackedBar> = emptyList()
)

@Serializable
data class PackedBar(
    val bar_index: Int,
    val cuts: List<CutItem>,
    val used_length: Double,
    val remnant: Double,
    val waste: Double
)

@Serializable
data class CutItem(
    val length: Double,
    val mark: String? = null
)

// ─── Offline Sync ───

@Serializable
data class OfflineAction(
    val id: String,
    val partId: String,
    val payloadJson: String,
    val timestamp: String
)

// ─── File Attachments ───

@Serializable
data class FileAttachment(
    val id: String,
    val storage_bucket: String? = null,
    val storage_path: String,
    val mime_type: String? = null,
    val size_bytes: Long? = null,
    val created_at: String? = null,
    val uploaded_by: String? = null,
    val entity_type: String? = null,
    val entity_id: String? = null
)

// ─── Audit Log ───

@Serializable
data class AuditLogEntry(
    val id: String,
    val user_id: String? = null,
    val user_name: String? = null,
    val action: String,
    val entity_type: String? = null,
    val entity_id: String? = null,
    val details: String? = null,
    val created_at: String? = null
)

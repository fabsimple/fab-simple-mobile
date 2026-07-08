package com.fabsimple.shared.domain.repository

import com.fabsimple.shared.domain.model.*
import com.fabsimple.shared.data.network.CutOptimizeRequest
import com.fabsimple.shared.data.network.DashboardData
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow


/**
 * Auth repository — sign-in, sign-up, session management.
 */
interface AuthRepository {
    suspend fun signIn(email: String, password: String): UserSession
    suspend fun signUp(email: String, password: String, fullName: String, companyName: String)
    suspend fun acceptInvite(token: String, password: String, fullName: String)
    suspend fun forgotPassword(email: String)
    fun getSession(): UserSession?
    fun signOut()
    val sessionExpired: SharedFlow<Unit>
    val currentSession: StateFlow<UserSession?>
}

/**
 * Parts repository — CRUD, status updates, offline-first.
 */
interface PartRepository {
    suspend fun getParts(projectId: String? = null): List<Part>
    suspend fun getPartById(id: String): Part
    suspend fun updatePartStatus(id: String, payload: Map<String, String>): Part
    suspend fun createPart(part: Part): Part
    suspend fun deletePart(id: String)
    fun isOnline(): Boolean
    fun setOnline(online: Boolean)
    fun getOfflineQueue(): List<OfflineAction>
    suspend fun flushOfflineQueue()
}

/**
 * Project repository — CRUD, list, detail.
 */
interface ProjectRepository {
    suspend fun getProjects(): List<Project>
    suspend fun getProjectById(id: String): Project
    suspend fun createProject(project: Project): Project
    suspend fun updateProject(id: String, project: Project): Project
    suspend fun deleteProject(id: String)
}

/**
 * Drawing repository.
 */
interface DrawingRepository {
    suspend fun getDrawings(projectId: String? = null): List<Drawing>
    suspend fun getDrawingsForPart(partId: String): List<Drawing>
}

/**
 * Quality & Compliance repository — AISC, Weld, Paint, NCR, OSHA, Certs.
 */
interface QcRepository {
    suspend fun getWeldInspections(projectId: String? = null): List<WeldInspection>
    suspend fun createWeldInspection(inspection: WeldInspection): WeldInspection
    suspend fun getPaintInspections(projectId: String? = null): List<PaintInspection>
    suspend fun createPaintInspection(inspection: PaintInspection): PaintInspection
    suspend fun getNcrReports(projectId: String? = null): List<NcrReport>
    suspend fun createNcrReport(ncr: NcrReport): NcrReport
    suspend fun getAiscChecklist(projectId: String): List<AiscChecklistItem>
    suspend fun updateAiscItem(item: AiscChecklistItem): AiscChecklistItem
    suspend fun getOshaChecklist(projectId: String? = null): List<OshaChecklist>
    suspend fun updateOshaItem(item: OshaChecklist): OshaChecklist
    suspend fun getCertifications(): List<Certification>
    suspend fun createCertification(cert: Certification): Certification
}

/**
 * Procurement repository — POs, Receiving, Inventory, Heat Numbers.
 */
interface ProcurementRepository {
    suspend fun getPurchaseOrders(): List<PurchaseOrder>
    suspend fun createPurchaseOrder(po: PurchaseOrder): PurchaseOrder
    suspend fun getReceivingRecords(): List<ReceivingRecord>
    suspend fun createReceivingRecord(record: ReceivingRecord): ReceivingRecord
    suspend fun getInventory(): List<Inventory>
    suspend fun createInventory(inv: Inventory): Inventory
    suspend fun updateInventory(id: String, inv: Inventory): Inventory
    suspend fun getHeatNumbers(): List<HeatNumber>
    suspend fun createHeatNumber(hn: HeatNumber): HeatNumber
}

/**
 * Finance repository — Billing (G702), Job Cost.
 */
interface FinanceRepository {
    suspend fun getBillingApplications(projectId: String? = null): List<BillingApplication>
    suspend fun createBillingApplication(app: BillingApplication): BillingApplication
    suspend fun getJobCostEntries(projectId: String? = null): List<JobCostEntry>
    suspend fun createJobCostEntry(entry: JobCostEntry): JobCostEntry
}

/**
 * Logistics repository — Shipping, Erection, QR Codes, GC Contacts.
 */
interface LogisticsRepository {
    suspend fun getShippingTickets(projectId: String? = null): List<ShippingTicket>
    suspend fun createShippingTicket(ticket: ShippingTicket): ShippingTicket
    suspend fun getErectionSequence(projectId: String? = null): List<ErectionSequence>
    suspend fun createErectionSequence(seq: ErectionSequence): ErectionSequence
    suspend fun getGcContacts(): List<GcContact>
    suspend fun createGcContact(contact: GcContact): GcContact
}

/**
 * Production repository — Assemblies, Daily Log, Estimates, Import.
 */
interface ProductionRepository {
    suspend fun getAssemblies(projectId: String? = null): List<Assembly>
    suspend fun createAssembly(assembly: Assembly): Assembly
    suspend fun getDailyLogs(): List<DailyProductionLog>
    suspend fun createDailyLog(log: DailyProductionLog): DailyProductionLog
    suspend fun getEstimates(): List<Estimate>
    suspend fun createEstimate(estimate: Estimate): Estimate
}

/**
 * Admin repository — Users, Audit Log, RFIs, Change Orders.
 */
interface AdminRepository {
    suspend fun getUsers(): List<UserProfile>
    suspend fun inviteUser(email: String, role: String, fullName: String)
    suspend fun getAuditLog(): List<AuditLogEntry>
    suspend fun getRfis(projectId: String? = null): List<Rfi>
    suspend fun createRfi(rfi: Rfi): Rfi
    suspend fun getChangeOrders(projectId: String? = null): List<ChangeOrder>
    suspend fun createChangeOrder(co: ChangeOrder): ChangeOrder
}

/**
 * File / Storage repository — upload, sign, list.
 */
interface FileRepository {
    suspend fun uploadFile(
        entityType: String,
        entityId: String,
        filename: String,
        bytes: ByteArray,
        contentType: String,
        bucket: String = "photos"
    ): String

    suspend fun getSignedReadUrl(attachmentId: String): String
    suspend fun getFilesForEntity(entityType: String, entityId: String): List<FileAttachment>
}

/**
 * Dashboard aggregate repository.
 */
interface DashboardRepository {
    suspend fun getDashboardData(): DashboardData
}

/**
 * Search repository.
 */
interface SearchRepository {
    suspend fun search(query: String): List<SearchHit>
}

/**
 * AI Copilot repository.
 */
interface CopilotRepository {
    suspend fun query(messages: List<ChatMessage>, projectId: String? = null): String
}

/**
 * Cut Optimizer repository.
 */
interface CutOptimizerRepository {
    suspend fun optimize(request: CutOptimizeRequest): CutPlan
}

package com.fabsimple.shared.data.repository

import com.fabsimple.shared.data.local.LocalDatabase
import com.fabsimple.shared.data.network.*
import com.fabsimple.shared.domain.model.*
import com.fabsimple.shared.domain.repository.*

// ─── Project Repository ───

class ProjectRepositoryImpl(
    private val apiClient: FabApiClient,
    private val localDatabase: LocalDatabase
) : ProjectRepository {
    override suspend fun getProjects(): List<Project> {
        return try {
            val list: List<Project> = apiClient.get("/projects")
            localDatabase.cacheProjects(list)
            list
        } catch (e: Exception) {
            e.printStackTrace()
            localDatabase.getCachedProjects()
        }
    }

    override suspend fun getProjectById(id: String): Project = apiClient.get("/projects/$id")
    override suspend fun createProject(project: Project): Project = apiClient.post("/projects", project)
    override suspend fun updateProject(id: String, project: Project): Project = apiClient.patch("/projects/$id", project)
    override suspend fun deleteProject(id: String) { apiClient.delete<Unit>("/projects/$id") }
}

// ─── Drawing Repository ───

class DrawingRepositoryImpl(
    private val apiClient: FabApiClient
) : DrawingRepository {
    override suspend fun getDrawings(projectId: String?): List<Drawing> {
        val params = projectId?.let { mapOf("project_id" to it) }
        return apiClient.get("/drawings", params)
    }
    override suspend fun getDrawingsForPart(partId: String): List<Drawing> =
        apiClient.get("/files", mapOf("entity_type" to "parts", "entity_id" to partId))
}

// ─── QC Repository ───

class QcRepositoryImpl(
    private val apiClient: FabApiClient
) : QcRepository {
    override suspend fun getWeldInspections(projectId: String?): List<WeldInspection> {
        val params = projectId?.let { mapOf("project_id" to it) }
        return apiClient.get("/weld_inspections", params)
    }
    override suspend fun createWeldInspection(inspection: WeldInspection): WeldInspection =
        apiClient.post("/weld_inspections", inspection)

    override suspend fun getPaintInspections(projectId: String?): List<PaintInspection> {
        val params = projectId?.let { mapOf("project_id" to it) }
        return apiClient.get("/paint_inspections", params)
    }
    override suspend fun createPaintInspection(inspection: PaintInspection): PaintInspection =
        apiClient.post("/paint_inspections", inspection)

    override suspend fun getNcrReports(projectId: String?): List<NcrReport> {
        val params = projectId?.let { mapOf("project_id" to it) }
        return apiClient.get("/ncr_reports", params)
    }
    override suspend fun createNcrReport(ncr: NcrReport): NcrReport =
        apiClient.post("/ncr_reports", ncr)

    override suspend fun getAiscChecklist(projectId: String): List<AiscChecklistItem> =
        apiClient.get("/aisc_checklist", mapOf("project_id" to projectId))
    override suspend fun updateAiscItem(item: AiscChecklistItem): AiscChecklistItem =
        apiClient.patch("/aisc_checklist/${item.id}", item)

    override suspend fun getOshaChecklist(projectId: String?): List<OshaChecklist> {
        val params = projectId?.let { mapOf("project_id" to it) }
        return apiClient.get("/osha_checklist", params)
    }
    override suspend fun updateOshaItem(item: OshaChecklist): OshaChecklist =
        apiClient.patch("/osha_checklist/${item.id}", item)

    override suspend fun getCertifications(): List<Certification> = apiClient.get("/certifications")
    override suspend fun createCertification(cert: Certification): Certification =
        apiClient.post("/certifications", cert)
}

// ─── Procurement Repository ───

class ProcurementRepositoryImpl(
    private val apiClient: FabApiClient
) : ProcurementRepository {
    override suspend fun getPurchaseOrders(): List<PurchaseOrder> = apiClient.get("/purchase_orders")
    override suspend fun createPurchaseOrder(po: PurchaseOrder): PurchaseOrder =
        apiClient.post("/purchase_orders", po)

    override suspend fun getReceivingRecords(): List<ReceivingRecord> = apiClient.get("/receiving")
    override suspend fun createReceivingRecord(record: ReceivingRecord): ReceivingRecord =
        apiClient.post("/receiving", record)

    override suspend fun getInventory(): List<Inventory> = apiClient.get("/inventory")
    override suspend fun createInventory(inv: Inventory): Inventory = apiClient.post("/inventory", inv)
    override suspend fun updateInventory(id: String, inv: Inventory): Inventory =
        apiClient.patch("/inventory/$id", inv)

    override suspend fun getHeatNumbers(): List<HeatNumber> = apiClient.get("/heat_numbers")
    override suspend fun createHeatNumber(hn: HeatNumber): HeatNumber =
        apiClient.post("/heat_numbers", hn)
}

// ─── Finance Repository ───

class FinanceRepositoryImpl(
    private val apiClient: FabApiClient
) : FinanceRepository {
    override suspend fun getBillingApplications(projectId: String?): List<BillingApplication> {
        val params = projectId?.let { mapOf("project_id" to it) }
        return apiClient.get("/billing", params)
    }
    override suspend fun createBillingApplication(app: BillingApplication): BillingApplication =
        apiClient.post("/billing", app)

    override suspend fun getJobCostEntries(projectId: String?): List<JobCostEntry> {
        val params = projectId?.let { mapOf("project_id" to it) }
        return apiClient.get("/job_cost", params)
    }
    override suspend fun createJobCostEntry(entry: JobCostEntry): JobCostEntry =
        apiClient.post("/job_cost", entry)
}

// ─── Logistics Repository ───

class LogisticsRepositoryImpl(
    private val apiClient: FabApiClient
) : LogisticsRepository {
    override suspend fun getShippingTickets(projectId: String?): List<ShippingTicket> {
        val params = projectId?.let { mapOf("project_id" to it) }
        return apiClient.get("/shipping", params)
    }
    override suspend fun createShippingTicket(ticket: ShippingTicket): ShippingTicket =
        apiClient.post("/shipping", ticket)

    override suspend fun getErectionSequence(projectId: String?): List<ErectionSequence> {
        val params = projectId?.let { mapOf("project_id" to it) }
        return apiClient.get("/erection_sequence", params)
    }
    override suspend fun createErectionSequence(seq: ErectionSequence): ErectionSequence =
        apiClient.post("/erection_sequence", seq)

    override suspend fun getGcContacts(): List<GcContact> = apiClient.get("/gc_contacts")
    override suspend fun createGcContact(contact: GcContact): GcContact =
        apiClient.post("/gc_contacts", contact)
}

// ─── Production Repository ───

class ProductionRepositoryImpl(
    private val apiClient: FabApiClient
) : ProductionRepository {
    override suspend fun getAssemblies(projectId: String?): List<Assembly> {
        val params = projectId?.let { mapOf("project_id" to it) }
        return apiClient.get("/assemblies", params)
    }
    override suspend fun createAssembly(assembly: Assembly): Assembly =
        apiClient.post("/assemblies", assembly)

    override suspend fun getDailyLogs(): List<DailyProductionLog> = apiClient.get("/daily_log")
    override suspend fun createDailyLog(log: DailyProductionLog): DailyProductionLog =
        apiClient.post("/daily_log", log)

    override suspend fun getEstimates(): List<Estimate> = apiClient.get("/estimates")
    override suspend fun createEstimate(estimate: Estimate): Estimate =
        apiClient.post("/estimates", estimate)
}

// ─── Admin Repository ───

class AdminRepositoryImpl(
    private val apiClient: FabApiClient
) : AdminRepository {
    override suspend fun getUsers(): List<UserProfile> = apiClient.get("/users")
    override suspend fun inviteUser(email: String, role: String, fullName: String) {
        apiClient.post<Unit>("/invite-user", InviteUserRequest(email, role, fullName))
    }
    override suspend fun getAuditLog(): List<AuditLogEntry> = apiClient.get("/audit_log")

    override suspend fun getRfis(projectId: String?): List<Rfi> {
        val params = projectId?.let { mapOf("project_id" to it) }
        return apiClient.get("/rfis", params)
    }
    override suspend fun createRfi(rfi: Rfi): Rfi = apiClient.post("/rfis", rfi)

    override suspend fun getChangeOrders(projectId: String?): List<ChangeOrder> {
        val params = projectId?.let { mapOf("project_id" to it) }
        return apiClient.get("/change_orders", params)
    }
    override suspend fun createChangeOrder(co: ChangeOrder): ChangeOrder =
        apiClient.post("/change_orders", co)
}

// ─── File Repository ───

class FileRepositoryImpl(
    private val apiClient: FabApiClient
) : FileRepository {
    override suspend fun uploadFile(
        entityType: String,
        entityId: String,
        filename: String,
        bytes: ByteArray,
        contentType: String,
        bucket: String
    ): String {
        val sign: SignUploadResponse = apiClient.post(
            "/files/sign-upload",
            SignUploadRequest(
                bucket = bucket,
                filename = filename,
                entity_type = entityType,
                entity_id = entityId,
                content_type = contentType
            )
        )
        apiClient.uploadBytes(sign.upload_url, bytes, contentType)
        return sign.attachment_id
    }

    override suspend fun getSignedReadUrl(attachmentId: String): String {
        val resp: SignReadResponse = apiClient.get("/files/sign-read/$attachmentId")
        return resp.url
    }

    override suspend fun getFilesForEntity(entityType: String, entityId: String): List<FileAttachment> =
        apiClient.get("/files", mapOf("entity_type" to entityType, "entity_id" to entityId))
}

// ─── Dashboard Repository ───

class DashboardRepositoryImpl(
    private val apiClient: FabApiClient
) : DashboardRepository {
    override suspend fun getDashboardData(): DashboardData = apiClient.get("/dashboard")
}

// ─── Search Repository ───

class SearchRepositoryImpl(
    private val apiClient: FabApiClient
) : SearchRepository {
    override suspend fun search(query: String): List<SearchHit> {
        val resp: SearchResponse = apiClient.get("/search", mapOf("q" to query))
        return resp.hits
    }
}

// ─── Copilot Repository ───

class CopilotRepositoryImpl(
    private val apiClient: FabApiClient
) : CopilotRepository {
    override suspend fun query(messages: List<ChatMessage>, projectId: String?): String {
        val resp: CopilotResponse = apiClient.post(
            "/copilot",
            CopilotRequest(messages, projectId)
        )
        return resp.reply
    }
}

// ─── Cut Optimizer Repository ───

class CutOptimizerRepositoryImpl(
    private val apiClient: FabApiClient
) : CutOptimizerRepository {
    override suspend fun optimize(request: CutOptimizeRequest): CutPlan =
        apiClient.post("/cut-optimize", request)
}

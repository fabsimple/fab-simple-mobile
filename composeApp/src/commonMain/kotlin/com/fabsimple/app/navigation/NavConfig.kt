package com.fabsimple.app.navigation

/**
 * Port of `lib/nav-config.ts` — navigation sections, items, and role-based access.
 */

data class NavItem(
    val key: String,
    val label: String,
    val icon: String,   // Icon name string (resolve in Compose via when-mapping)
    val roles: List<String>? = null, // null = visible to all roles
    val badge: String? = null
)

data class NavSection(
    val label: String,
    val items: List<NavItem>
)

/** All valid user roles in FabSimple */
val ALL_ROLES = listOf("owner", "admin", "pm", "foreman", "qc_inspector", "worker")
val OFFICE_ROLES = listOf("owner", "admin", "pm")
val SHOP_ROLES = listOf("owner", "admin", "pm", "foreman", "qc_inspector")

/**
 * Complete navigation tree — mirrors NAV_SECTIONS from nav-config.ts.
 * Role filtering is done at render time.
 */
val NAV_SECTIONS = listOf(
    NavSection(
        label = "Overview",
        items = listOf(
            NavItem("dashboard", "Dashboard", "LayoutDashboard"),
            NavItem("live-activity", "Live Activity", "Activity", roles = OFFICE_ROLES),
        )
    ),
    NavSection(
        label = "Pre-Construction",
        items = listOf(
            NavItem("estimating", "Estimating", "Calculator", roles = OFFICE_ROLES),
            NavItem("projects", "Projects", "FolderKanban"),
            NavItem("drawings", "Drawings", "FileText"),
            NavItem("change-orders", "Change Orders", "FileDiff", roles = OFFICE_ROLES),
            NavItem("rfis", "RFIs", "MessageSquare", roles = OFFICE_ROLES),
        )
    ),
    NavSection(
        label = "Production",
        items = listOf(
            NavItem("parts", "Parts", "Wrench"),
            NavItem("assemblies", "Assemblies", "Boxes", roles = SHOP_ROLES),
            NavItem("daily-log", "Daily Log", "ClipboardList", roles = SHOP_ROLES),
            NavItem("cut-list", "Cut List Optimizer", "Scissors"),
            NavItem("import", "Import Parts", "Upload", roles = OFFICE_ROLES),
        )
    ),
    NavSection(
        label = "Quality & Compliance",
        items = listOf(
            NavItem("aisc", "AISC 303", "Shield", roles = SHOP_ROLES),
            NavItem("weld-log", "Weld Log", "Flame", roles = SHOP_ROLES),
            NavItem("paint-inspection", "Paint/Coat", "Paintbrush", roles = SHOP_ROLES),
            NavItem("osha", "OSHA Safety", "HardHat", roles = SHOP_ROLES),
            NavItem("certifications", "Certifications", "Award", roles = SHOP_ROLES),
            NavItem("ncr", "NCR Reports", "AlertTriangle"),
        )
    ),
    NavSection(
        label = "Procurement",
        items = listOf(
            NavItem("purchase-orders", "Purchase Orders", "ShoppingCart", roles = OFFICE_ROLES),
            NavItem("receiving", "Receiving", "PackageCheck", roles = SHOP_ROLES),
            NavItem("inventory", "Inventory", "Warehouse"),
            NavItem("heat-numbers", "Heat Numbers", "Thermometer", roles = SHOP_ROLES),
        )
    ),
    NavSection(
        label = "Logistics",
        items = listOf(
            NavItem("erection", "Erection Seq.", "Building", roles = OFFICE_ROLES),
            NavItem("shipping", "Shipping", "Truck"),
            NavItem("qr-codes", "QR Codes", "QrCode"),
            NavItem("gc-contacts", "GC Contacts", "Users", roles = OFFICE_ROLES),
        )
    ),
    NavSection(
        label = "Finance",
        items = listOf(
            NavItem("job-cost", "Job Costing", "DollarSign", roles = OFFICE_ROLES),
            NavItem("billing", "AIA Billing", "Receipt", roles = OFFICE_ROLES),
        )
    ),
    NavSection(
        label = "Admin",
        items = listOf(
            NavItem("users", "Team", "Users", roles = listOf("owner", "admin")),
            NavItem("audit-log", "Audit Log", "ScrollText", roles = listOf("owner", "admin")),
            NavItem("integrations", "Integrations", "Plug", roles = listOf("owner", "admin")),
            NavItem("pricing", "Plans & Pricing", "CreditCard", roles = listOf("owner")),
        )
    ),
)

/**
 * Routes that don't require the sidebar layout (auth + worker).
 */
val NO_SIDEBAR_ROUTES = setOf(
    "login", "signup", "forgot", "accept-invite",
    "worker-queue", "worker-detail", "worker-scan"
)

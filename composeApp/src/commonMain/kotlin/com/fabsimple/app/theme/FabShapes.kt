package com.fabsimple.app.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Shape tokens — matching the border-radius values from globals.css.
 */
object FabShapes {
    val Card = RoundedCornerShape(14.dp)       // .card { border-radius: 14px }
    val StatCard = RoundedCornerShape(12.dp)   // .stat-card { border-radius: 12px }
    val Button = RoundedCornerShape(8.dp)      // .btn { border-radius: 8px }
    val ButtonLarge = RoundedCornerShape(10.dp)// .btn-lg { border-radius: 10px }
    val Input = RoundedCornerShape(8.dp)       // .input { border-radius: 8px }
    val Pill = RoundedCornerShape(20.dp)       // .pill { border-radius: 20px }
    val Modal = RoundedCornerShape(16.dp)      // .modal { border-radius: 16px }
    val Alert = RoundedCornerShape(9.dp)       // .alert { border-radius: 9px }
    val InfoCell = RoundedCornerShape(8.dp)    // .info-cell { border-radius: 8px }
    val Checklist = RoundedCornerShape(9.dp)   // .cc { border-radius: 9px }
    val Dropzone = RoundedCornerShape(12.dp)   // .dropzone { border-radius: 12px }
    val QrCard = RoundedCornerShape(10.dp)     // .qr-card { border-radius: 10px }
    val PhoneFrame = RoundedCornerShape(24.dp) // .phone-frame { border-radius: 24px }

    // Worker-specific
    val WorkerButton = RoundedCornerShape(16.dp)
    val WorkerCard = RoundedCornerShape(12.dp)
    val WorkerTab = RoundedCornerShape(12.dp)
}

/** Material3 Shapes configured with FabSimple radii */
val FabMaterialShapes = Shapes(
    small = FabShapes.Button,
    medium = FabShapes.Card,
    large = FabShapes.Modal,
    extraLarge = FabShapes.PhoneFrame,
)

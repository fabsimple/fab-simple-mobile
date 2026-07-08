package com.fabsimple.app.components

import platform.UIKit.UIPrintInteractionController
import platform.UIKit.UIMarkupTextPrintFormatter
import platform.UIKit.UIPrintInfo
import platform.UIKit.UIPrintInfoOutputType

/**
 * iOS implementation using UIPrintInteractionController and UIMarkupTextPrintFormatter.
 */
actual fun printHtml(html: String, jobName: String) {
    val printController = UIPrintInteractionController.sharedPrintController()
    val printInfo = UIPrintInfo.printInfo()
    printInfo.outputType = UIPrintInfoOutputType.values()[0] // UIPrintInfoOutputGeneral
    printInfo.jobName = jobName
    printController.printInfo = printInfo
    
    val formatter = UIMarkupTextPrintFormatter(html)
    printController.printFormatter = formatter
    
    printController.presentAnimated(animated = true, completionHandler = null)
}

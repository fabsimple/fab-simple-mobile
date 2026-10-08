package com.fabsimple.app.components

import platform.Network.nw_path_evaluator_create
import platform.Network.nw_path_evaluator_copy_path
import platform.Network.nw_path_get_status
import platform.Network.nw_path_status_satisfied

/**
 * iOS implementation to check network availability using Network framework.
 */
actual fun isNetworkAvailable(): Boolean {
    val evaluator = nw_path_evaluator_create() ?: return true
    val path = nw_path_evaluator_copy_path(evaluator) ?: return true
    val status = nw_path_get_status(path)
    return status == nw_path_status_satisfied
}

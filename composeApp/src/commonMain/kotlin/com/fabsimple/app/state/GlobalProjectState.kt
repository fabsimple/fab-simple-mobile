package com.fabsimple.app.state

import com.fabsimple.shared.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ActiveProject(
    val id: String?,
    val name: String,
    val number: String?
)

/**
 * Global reactive project selection state.
 * Syncs with LocalDatabase cache automatically.
 */
object GlobalProjectState {
    private val _currentProject = MutableStateFlow<ActiveProject>(getInitialProject())
    val currentProject: StateFlow<ActiveProject> = _currentProject.asStateFlow()

    fun selectProject(id: String, name: String, number: String?) {
        AppContainer.localDatabase.saveActiveProjectId(id)
        _currentProject.value = ActiveProject(id, name, number)
    }

    fun clearProject() {
        AppContainer.localDatabase.saveActiveProjectId(null)
        _currentProject.value = ActiveProject(null, "All Projects", null)
    }

    private fun getInitialProject(): ActiveProject {
        val storedId = AppContainer.localDatabase.getActiveProjectId()
        if (storedId == null) {
            return ActiveProject(null, "All Projects", null)
        }
        // Let Phase 3/4 repository load projects later, or default to placeholder
        // which will resolve on load
        return ActiveProject(storedId, "Loading Project…", null)
    }
}

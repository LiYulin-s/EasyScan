package io.github.liyulin.easyscan.ui.viewmodel

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.liyulin.easyscan.getDataStorePath
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import okio.Path.Companion.toPath

class HistoryViewModel : ViewModel() {
    @Serializable
    data class HistoryItem(
        val key: String,
        val name: String,
    )

    private fun createDataStore(producePath: () -> String): DataStore<Preferences> {
        return PreferenceDataStoreFactory.createWithPath(
            migrations = emptyList(),
            scope = viewModelScope,
            produceFile = { producePath().toPath() }
        )
    }

    // Initialize the settings DataStore with a predefined path.
    private val settings = createDataStore {
        getDataStorePath()
    }

    private val historyKey = stringSetPreferencesKey("history")

    val history: StateFlow<Collection<HistoryItem>> = settings.data.map { preferences ->
        (preferences[historyKey] ?: setOf<String>()).map {
            Json.decodeFromString<HistoryItem>(it)
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, setOf<HistoryItem>())

    suspend fun clearHistory() {
        settings.edit { preferences ->
            preferences.remove(historyKey)
        }
    }

    suspend fun saveHistory(history: HistoryItem) {
        settings.edit { preferences ->
            preferences[historyKey] = preferences[historyKey].orEmpty().plusElement(Json.encodeToJsonElement(history).toString())
        }
    }

}
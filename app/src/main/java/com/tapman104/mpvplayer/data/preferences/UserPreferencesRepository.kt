package com.tapman104.mpvplayer.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.userPrefsDataStore by preferencesDataStore(name = "user_prefs")

class UserPreferencesRepository(private val context: Context) {

    companion object {
        val SUBTITLE_LANGUAGE = stringPreferencesKey("subtitle_language")
        const val DEFAULT_SUBTITLE_LANGUAGE = "en"
    }

    /** Emits the saved subtitle language preference, defaulting to "en". */
    val subtitleLanguage: Flow<String> = context.userPrefsDataStore.data.map { prefs ->
        prefs[SUBTITLE_LANGUAGE] ?: DEFAULT_SUBTITLE_LANGUAGE
    }

    suspend fun setSubtitleLanguage(lang: String) {
        context.userPrefsDataStore.edit { prefs ->
            prefs[SUBTITLE_LANGUAGE] = lang
        }
    }
}

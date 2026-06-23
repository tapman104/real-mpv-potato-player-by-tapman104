package com.tapman104.mpvplayer.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.tapman104.mpvplayer.data.db.AppDatabase
import com.tapman104.mpvplayer.data.preferences.UserPreferencesRepository

class PlayerViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlayerViewModel::class.java)) {
            val appContext = context.applicationContext
            val database = Room.databaseBuilder(
                appContext,
                AppDatabase::class.java,
                "mpvplayer.db"
            ).build()
            val userPreferencesRepository = UserPreferencesRepository(appContext)
            @Suppress("UNCHECKED_CAST")
            return PlayerViewModel(
                context = appContext,
                resumePositionDao = database.resumePositionDao(),
                userPreferencesRepository = userPreferencesRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

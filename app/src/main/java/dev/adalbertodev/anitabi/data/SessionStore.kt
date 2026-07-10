package dev.adalbertodev.anitabi.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "session")

class SessionStore(private val context: Context) {
    private val keyToken = stringPreferencesKey("access_token")

    val token: Flow<String?> = context.dataStore.data.map { it[keyToken] }

    suspend fun saveToken(token: String) {
        context.dataStore.edit { it[keyToken] = token }
    }

    suspend fun clear() {
        context.dataStore.edit { it.remove(keyToken) }
    }
}
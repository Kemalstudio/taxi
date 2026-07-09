package com.taxiplatform.driver.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.taxiplatform.driver.domain.model.AuthSession
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.authDataStore by preferencesDataStore(name = "auth")

@Singleton
class TokenStore @Inject constructor(
	@ApplicationContext private val context: Context,
) {
	private val tokenKey = stringPreferencesKey("token")
	private val userIdKey = stringPreferencesKey("user_id")

	suspend fun save(session: AuthSession) {
		context.authDataStore.edit { prefs ->
			prefs[tokenKey] = session.token
			prefs[userIdKey] = session.userId
		}
	}

	suspend fun current(): AuthSession? {
		val prefs = context.authDataStore.data
			.catch { if (it is IOException) emit(emptyPreferences()) else throw it }
			.first()
		val token = prefs[tokenKey] ?: return null
		val userId = prefs[userIdKey] ?: return null
		return AuthSession(userId = userId, token = token)
	}

	suspend fun clear() {
		context.authDataStore.edit { it.clear() }
	}

	fun tokenFlow() = context.authDataStore.data.map { it[tokenKey] }
}

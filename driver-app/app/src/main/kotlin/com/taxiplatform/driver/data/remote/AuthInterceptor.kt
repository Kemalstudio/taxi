package com.taxiplatform.driver.data.remote

import com.taxiplatform.driver.data.local.TokenStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
	private val tokenStore: TokenStore,
) : Interceptor {

	override fun intercept(chain: Interceptor.Chain): Response {
		val original = chain.request()
		if (original.url.encodedPath.contains("/auth/")) {
			return chain.proceed(original)
		}

		val token = runBlocking { tokenStore.tokenFlow().first() }
		val request = if (token != null) {
			original.newBuilder().addHeader("Authorization", "Bearer $token").build()
		} else {
			original
		}
		return chain.proceed(request)
	}
}

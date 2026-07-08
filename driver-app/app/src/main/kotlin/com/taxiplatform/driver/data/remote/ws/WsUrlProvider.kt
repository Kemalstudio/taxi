package com.taxiplatform.driver.data.remote.ws

import com.taxiplatform.driver.BuildConfig
import java.net.URI
import javax.inject.Inject
import javax.inject.Singleton

/** Backend exposes SockJS at /ws; appending /websocket gives the raw (non-SockJS-framed) transport. */
@Singleton
class WsUrlProvider @Inject constructor() {
	private val httpUri: URI = URI(BuildConfig.BASE_URL)

	fun rawWebSocketUrl(): String {
		val scheme = if (httpUri.scheme == "https") "wss" else "ws"
		return "$scheme://${httpUri.authority}/ws/websocket"
	}

	fun host(): String = httpUri.authority
}

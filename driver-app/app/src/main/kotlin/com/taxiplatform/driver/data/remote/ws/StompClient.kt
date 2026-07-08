package com.taxiplatform.driver.data.remote.ws

import com.squareup.moshi.Moshi
import com.taxiplatform.driver.data.remote.dto.RideOfferMessageDto
import com.taxiplatform.driver.data.remote.dto.RideStatusMessageDto
import com.taxiplatform.driver.data.remote.dto.toDomain
import com.taxiplatform.driver.domain.model.RideOffer
import com.taxiplatform.driver.domain.model.RideStatusUpdate
import com.taxiplatform.driver.domain.repository.RideEventsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filter
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Minimal STOMP-over-WebSocket client (CONNECT/SUBSCRIBE/MESSAGE only — no
 * ack modes, no heart-beats). Talks to the backend's raw SockJS transport at
 * `/ws/websocket`, matching `WebSocketConfig` on the server.
 */
@Singleton
class StompClient @Inject constructor(
	private val okHttpClient: OkHttpClient,
	private val moshi: Moshi,
	private val wsUrlProvider: WsUrlProvider,
) : RideEventsRepository {

	/** STOMP frames are terminated by the NUL octet, per the STOMP 1.2 spec. */
	private val frameTerminator: Char = 0.toChar()

	private var webSocket: WebSocket? = null
	@Volatile private var connected = false
	private val pendingDestinations = mutableListOf<String>()
	private val activeDestinations = mutableSetOf<String>()
	private val subscriptionIds = AtomicInteger(0)

	private val offers = MutableSharedFlow<RideOffer>(extraBufferCapacity = 8)
	private val statuses = MutableSharedFlow<RideStatusUpdate>(extraBufferCapacity = 8)

	override fun connect(driverId: String) {
		if (webSocket != null) return
		connected = false
		val request = Request.Builder().url(wsUrlProvider.rawWebSocketUrl()).build()
		webSocket = okHttpClient.newWebSocket(request, FrameListener())
		subscribe("/topic/driver/$driverId")
	}

	override fun disconnect() {
		webSocket?.close(1000, "driver offline")
		webSocket = null
		connected = false
		pendingDestinations.clear()
		activeDestinations.clear()
	}

	override fun observeOffers(): Flow<RideOffer> = offers.asSharedFlow()

	override fun observeRideStatus(rideId: String): Flow<RideStatusUpdate> {
		subscribe("/topic/ride/$rideId")
		return statuses.asSharedFlow().filter { it.rideId == rideId }
	}

	private fun subscribe(destination: String) {
		if (destination in activeDestinations || destination in pendingDestinations) return
		if (connected) sendSubscribe(destination) else pendingDestinations += destination
	}

	private fun sendSubscribe(destination: String) {
		val id = "sub-${subscriptionIds.getAndIncrement()}"
		webSocket?.send(frame("SUBSCRIBE", mapOf("id" to id, "destination" to destination)))
		activeDestinations += destination
	}

	private fun frame(command: String, headers: Map<String, String>, body: String = ""): String {
		val sb = StringBuilder(command).append('\n')
		headers.forEach { (key, value) -> sb.append(key).append(':').append(value).append('\n') }
		sb.append('\n').append(body).append(frameTerminator)
		return sb.toString()
	}

	private fun handleFrame(raw: String) {
		val separatorIndex = raw.indexOf("\n\n")
		val headerPart = if (separatorIndex == -1) raw else raw.substring(0, separatorIndex)
		val body = if (separatorIndex == -1) "" else raw.substring(separatorIndex + 2)
		val headerLines = headerPart.lines()
		if (headerLines.isEmpty()) return
		val command = headerLines.first().trim()
		val headers = headerLines.drop(1).mapNotNull { line ->
			val idx = line.indexOf(':')
			if (idx == -1) null else line.substring(0, idx) to line.substring(idx + 1)
		}.toMap()

		when (command) {
			"CONNECTED" -> {
				connected = true
				val toSubscribe = pendingDestinations.toList()
				pendingDestinations.clear()
				toSubscribe.forEach { sendSubscribe(it) }
			}
			"MESSAGE" -> handleMessage(headers["destination"] ?: return, body)
			else -> Unit // ERROR / RECEIPT frames are not needed for phase 2
		}
	}

	private fun handleMessage(destination: String, body: String) {
		when {
			destination.startsWith("/topic/driver/") ->
				moshi.adapter(RideOfferMessageDto::class.java).fromJson(body)?.let { offers.tryEmit(it.toDomain()) }

			destination.startsWith("/topic/ride/") ->
				moshi.adapter(RideStatusMessageDto::class.java).fromJson(body)?.let { statuses.tryEmit(it.toDomain()) }
		}
	}

	private inner class FrameListener : WebSocketListener() {
		override fun onOpen(webSocket: WebSocket, response: Response) {
			webSocket.send(frame("CONNECT", mapOf("accept-version" to "1.2", "host" to wsUrlProvider.host())))
		}

		override fun onMessage(webSocket: WebSocket, text: String) {
			text.split(frameTerminator).filter { it.isNotBlank() }.forEach(::handleFrame)
		}

		override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
			connected = false
		}

		override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
			connected = false
		}
	}
}

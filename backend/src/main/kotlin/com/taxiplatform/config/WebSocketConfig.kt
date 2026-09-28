package com.taxiplatform.config

import com.taxiplatform.application.ports.JwtService
import com.taxiplatform.application.ports.RideRepository
import com.taxiplatform.application.ports.UserRepository
import com.taxiplatform.infrastructure.security.StompAuthChannelInterceptor
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.ChannelRegistration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig(
	private val jwtService: JwtService,
	private val userRepository: UserRepository,
	private val rideRepository: RideRepository,
	@Value("\${taxi.cors.allowed-origins}") private val allowedOrigins: String,
) : WebSocketMessageBrokerConfigurer {

	override fun configureMessageBroker(registry: MessageBrokerRegistry) {
		registry.enableSimpleBroker("/topic")
		registry.setApplicationDestinationPrefixes("/app")
	}

	override fun registerStompEndpoints(registry: StompEndpointRegistry) {
		val origins = allowedOrigins.split(",").map { it.trim() }.filter { it.isNotBlank() }.toTypedArray()
		registry.addEndpoint("/ws").setAllowedOrigins(*origins).withSockJS()
	}

	override fun configureClientInboundChannel(registration: ChannelRegistration) {
		registration.interceptors(
			StompAuthChannelInterceptor(jwtService, userRepository, rideRepository),
		)
	}
}

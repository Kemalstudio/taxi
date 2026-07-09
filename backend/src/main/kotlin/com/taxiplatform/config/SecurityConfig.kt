package com.taxiplatform.config

import com.taxiplatform.infrastructure.security.JwtAuthFilter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableMethodSecurity
class SecurityConfig(
	private val jwtAuthFilter: JwtAuthFilter,
	@Value("\${taxi.cors.allowed-origins}") private val allowedOrigins: String,
) {

	@Bean
	fun filterChain(http: HttpSecurity): SecurityFilterChain {
		http
			.cors { it.configurationSource(corsConfigurationSource()) }
			.csrf { it.disable() }
			.sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
			.authorizeHttpRequests { auth ->
				auth
					.requestMatchers("/auth/**", "/ws/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
					.anyRequest().authenticated()
			}
			.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)
			.httpBasic { it.disable() }
			.formLogin { it.disable() }

		return http.build()
	}

	@Bean
	fun corsConfigurationSource(): CorsConfigurationSource {
		val configuration = CorsConfiguration().apply {
			allowedOrigins = this@SecurityConfig.allowedOrigins.split(",").map { it.trim() }.filter { it.isNotBlank() }
			allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
			allowedHeaders = listOf("*")
			allowCredentials = true
		}
		return UrlBasedCorsConfigurationSource().apply {
			registerCorsConfiguration("/**", configuration)
		}
	}
}

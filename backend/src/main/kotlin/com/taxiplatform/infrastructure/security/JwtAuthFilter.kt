package com.taxiplatform.infrastructure.security

import com.taxiplatform.application.ports.JwtService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

class AuthenticatedPrincipal(val userId: java.util.UUID)

@Component
class JwtAuthFilter(
	private val jwtService: JwtService,
) : OncePerRequestFilter() {

	override fun doFilterInternal(
		request: HttpServletRequest,
		response: HttpServletResponse,
		filterChain: FilterChain,
	) {
		val header = request.getHeader("Authorization")
		if (header != null && header.startsWith("Bearer ")) {
			val token = header.removePrefix("Bearer ").trim()
			val principal = jwtService.parse(token)
			if (principal != null && SecurityContextHolder.getContext().authentication == null) {
				val authorities = listOf(SimpleGrantedAuthority("ROLE_${principal.role.name}"))
				val authentication = UsernamePasswordAuthenticationToken(
					AuthenticatedPrincipal(principal.userId),
					null,
					authorities,
				)
				SecurityContextHolder.getContext().authentication = authentication
			}
		}
		filterChain.doFilter(request, response)
	}
}

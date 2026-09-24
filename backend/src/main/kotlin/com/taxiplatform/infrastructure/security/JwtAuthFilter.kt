package com.taxiplatform.infrastructure.security

import com.taxiplatform.application.ports.JwtService
import com.taxiplatform.application.ports.UserRepository
import com.taxiplatform.domain.user.isBackOffice
import com.taxiplatform.domain.user.permissions
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
	private val userRepository: UserRepository,
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
				// Read the current role and ban/2FA state so role changes and account blocks revoke
				// existing tokens immediately instead of waiting for JWT expiration.
				val user = userRepository.findById(principal.userId)
				if (user == null || user.banned || (user.role.isBackOffice() && !user.twoFactorEnabled)) {
					filterChain.doFilter(request, response)
					return
				}
				val authorities = buildList {
					add(SimpleGrantedAuthority("ROLE_${user.role.name}"))
					user.role.permissions().forEach { add(SimpleGrantedAuthority(it.authority)) }
				}
				val authentication = UsernamePasswordAuthenticationToken(
					AuthenticatedPrincipal(user.id),
					null,
					authorities,
				)
				SecurityContextHolder.getContext().authentication = authentication
			}
		}
		filterChain.doFilter(request, response)
	}
}

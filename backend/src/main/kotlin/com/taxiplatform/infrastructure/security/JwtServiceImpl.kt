package com.taxiplatform.infrastructure.security

import com.taxiplatform.application.ports.JwtPrincipal
import com.taxiplatform.application.ports.JwtService
import com.taxiplatform.domain.user.Role
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.util.Date
import java.util.UUID

@Service
class JwtServiceImpl(
	@Value("\${taxi.jwt.secret}") secret: String,
	@Value("\${taxi.jwt.expiration-minutes}") private val expirationMinutes: Long,
) : JwtService {

	private val log = LoggerFactory.getLogger(JwtServiceImpl::class.java)
	private val key = Keys.hmacShaKeyFor(secret.toByteArray(Charsets.UTF_8))
	private val roleClaim = "role"

	override fun generateToken(userId: UUID, role: Role): String {
		val now = Instant.now()
		return Jwts.builder()
			.subject(userId.toString())
			.claim(roleClaim, role.name)
			.issuedAt(Date.from(now))
			.expiration(Date.from(now.plus(Duration.ofMinutes(expirationMinutes))))
			.signWith(key)
			.compact()
	}

	override fun parse(token: String): JwtPrincipal? {
		return try {
			val claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).payload
			JwtPrincipal(
				userId = UUID.fromString(claims.subject),
				role = Role.valueOf(claims.get(roleClaim, String::class.java)),
			)
		} catch (e: ExpiredJwtException) {
			log.debug("JWT expired: {}", e.message)
			null
		} catch (e: JwtException) {
			log.debug("JWT invalid: {}", e.message)
			null
		} catch (e: IllegalArgumentException) {
			log.debug("JWT malformed subject/claim: {}", e.message)
			null
		}
	}
}

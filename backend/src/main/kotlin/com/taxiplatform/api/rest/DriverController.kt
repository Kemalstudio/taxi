package com.taxiplatform.api.rest

import com.taxiplatform.api.dto.DriverLocationRequest
import com.taxiplatform.api.dto.DriverStatusRequest
import com.taxiplatform.application.driver.UpdateDriverLocationUseCase
import com.taxiplatform.application.driver.UpdateDriverStatusUseCase
import com.taxiplatform.domain.geo.GeoPoint
import com.taxiplatform.infrastructure.security.AuthenticatedPrincipal
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/driver")
@PreAuthorize("hasRole('DRIVER')")
class DriverController(
	private val updateDriverStatusUseCase: UpdateDriverStatusUseCase,
	private val updateDriverLocationUseCase: UpdateDriverLocationUseCase,
) {

	@PostMapping("/status")
	fun updateStatus(
		@AuthenticationPrincipal principal: AuthenticatedPrincipal,
		@Valid @RequestBody request: DriverStatusRequest,
	): ResponseEntity<Void> {
		updateDriverStatusUseCase.execute(principal.userId, request.status)
		return ResponseEntity.noContent().build()
	}

	@PostMapping("/location")
	fun updateLocation(
		@AuthenticationPrincipal principal: AuthenticatedPrincipal,
		@Valid @RequestBody request: DriverLocationRequest,
	): ResponseEntity<Void> {
		updateDriverLocationUseCase.execute(principal.userId, GeoPoint(request.lat, request.lng))
		return ResponseEntity.noContent().build()
	}
}

package com.taxiplatform.api.rest

import com.taxiplatform.api.dto.AdminDriverResponse
import com.taxiplatform.api.dto.OnlineDriverResponse
import com.taxiplatform.api.dto.PlatformStatsResponse
import com.taxiplatform.api.dto.RideResponse
import com.taxiplatform.api.dto.SosIncidentResponse
import com.taxiplatform.application.admin.GetPlatformStatsUseCase
import com.taxiplatform.application.admin.ListDriversUseCase
import com.taxiplatform.application.admin.ListOnlineDriversUseCase
import com.taxiplatform.application.admin.ListRidesUseCase
import com.taxiplatform.application.safety.ListRecentSosUseCase
import com.taxiplatform.domain.ride.RideStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
class AdminController(
	private val getPlatformStatsUseCase: GetPlatformStatsUseCase,
	private val listRidesUseCase: ListRidesUseCase,
	private val listDriversUseCase: ListDriversUseCase,
	private val listOnlineDriversUseCase: ListOnlineDriversUseCase,
	private val listRecentSosUseCase: ListRecentSosUseCase,
) {

	@GetMapping("/stats")
	fun stats(): PlatformStatsResponse = PlatformStatsResponse.from(getPlatformStatsUseCase.execute())

	@GetMapping("/rides")
	fun rides(
		@RequestParam(required = false) status: RideStatus?,
		@RequestParam(defaultValue = "50") limit: Int,
	): List<RideResponse> = listRidesUseCase.execute(status, limit).map(RideResponse::from)

	@GetMapping("/drivers")
	fun drivers(): List<AdminDriverResponse> = listDriversUseCase.execute().map(AdminDriverResponse::from)

	@GetMapping("/drivers/online")
	fun onlineDrivers(): List<OnlineDriverResponse> =
		listOnlineDriversUseCase.execute().map(OnlineDriverResponse::from)

	@GetMapping("/sos")
	fun sos(@RequestParam(defaultValue = "50") limit: Int): List<SosIncidentResponse> =
		listRecentSosUseCase.execute(limit).map(SosIncidentResponse::from)
}

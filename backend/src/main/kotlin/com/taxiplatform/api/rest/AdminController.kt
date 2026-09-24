package com.taxiplatform.api.rest

import com.taxiplatform.api.dto.AdminDriverResponse
import com.taxiplatform.api.dto.AdminRideDetailResponse
import com.taxiplatform.api.dto.MessageResponse
import com.taxiplatform.api.dto.AdminUserResponse
import com.taxiplatform.api.dto.AdminPromoCodeResponse
import com.taxiplatform.api.dto.AuditLogEntryResponse
import com.taxiplatform.api.dto.BroadcastLogResponse
import com.taxiplatform.api.dto.GeneratePayoutRequest
import com.taxiplatform.api.dto.PayoutResponse
import com.taxiplatform.api.dto.SendBroadcastRequest
import com.taxiplatform.api.dto.TariffRevenuePointResponse
import com.taxiplatform.api.dto.BanUserRequest
import com.taxiplatform.api.dto.ChangeRoleRequest
import com.taxiplatform.api.dto.CreatePromoCodeRequest
import com.taxiplatform.api.dto.DriverActivityStatsResponse
import com.taxiplatform.api.dto.DriverEarningsResponse
import com.taxiplatform.api.dto.OnlineDriverResponse
import com.taxiplatform.api.dto.OtpChallengeResponse
import com.taxiplatform.api.dto.PlatformRevenueResponse
import com.taxiplatform.api.dto.PlatformStatsResponse
import com.taxiplatform.api.dto.ReassignRideRequest
import com.taxiplatform.api.dto.CancelRideRequest
import com.taxiplatform.api.dto.RideResponse
import com.taxiplatform.api.dto.SettingsResponse
import com.taxiplatform.api.dto.SosIncidentResponse
import com.taxiplatform.api.dto.SystemStatusResponse
import com.taxiplatform.api.dto.UpdatePromoCodeRequest
import com.taxiplatform.api.dto.UpdateSettingsRequest
import com.taxiplatform.api.dto.VerifyDriverRequest
import com.taxiplatform.application.admin.AdminGetRideDetailUseCase
import com.taxiplatform.application.admin.AdminListRideMessagesUseCase
import com.taxiplatform.application.admin.ApproveDriverUseCase
import com.taxiplatform.application.admin.BanUserUseCase
import com.taxiplatform.application.admin.GeneratePayoutUseCase
import com.taxiplatform.application.admin.GetRevenueByTariffUseCase
import com.taxiplatform.application.admin.ListAuditLogUseCase
import com.taxiplatform.application.admin.ListBroadcastsUseCase
import com.taxiplatform.application.admin.ListPayoutsUseCase
import com.taxiplatform.application.admin.MarkPayoutPaidUseCase
import com.taxiplatform.application.admin.RecordAuditLogUseCase
import com.taxiplatform.application.admin.SendBroadcastUseCase
import com.taxiplatform.application.admin.ChangeUserRoleUseCase
import com.taxiplatform.application.admin.GetDriverActivityStatsUseCase
import com.taxiplatform.application.admin.GetDriverDocumentUseCase
import com.taxiplatform.application.admin.GetDriverEarningsUseCase
import com.taxiplatform.application.admin.GetPlatformRevenueUseCase
import com.taxiplatform.application.admin.GetPlatformStatsUseCase
import com.taxiplatform.application.admin.GetSystemStatusUseCase
import com.taxiplatform.application.admin.ListDriversUseCase
import com.taxiplatform.application.admin.ListOnlineDriversUseCase
import com.taxiplatform.application.admin.ListRecentOtpUseCase
import com.taxiplatform.application.admin.ListRidesUseCase
import com.taxiplatform.application.admin.ListUsersUseCase
import com.taxiplatform.application.admin.RejectDriverUseCase
import com.taxiplatform.application.admin.UnbanUserUseCase
import com.taxiplatform.application.payment.ConfirmCardPaymentUseCase
import com.taxiplatform.application.ports.DocumentKind
import com.taxiplatform.application.promo.CreatePromoCodeUseCase
import com.taxiplatform.application.promo.ListPromoCodesUseCase
import com.taxiplatform.application.promo.PromoCodePatch
import com.taxiplatform.application.promo.UpdatePromoCodeUseCase
import com.taxiplatform.application.ride.AdminForceCancelRideUseCase
import com.taxiplatform.application.ride.AdminReassignRideUseCase
import com.taxiplatform.application.safety.ListRecentSosUseCase
import com.taxiplatform.application.settings.GetSettingsUseCase
import com.taxiplatform.application.settings.UpdateSettingsUseCase
import com.taxiplatform.domain.ride.RideStatus
import com.taxiplatform.domain.user.Role
import com.taxiplatform.infrastructure.security.AuthenticatedPrincipal
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Duration
import java.time.Instant
import java.util.UUID

/**
 * Every endpoint here requires ADMIN or OPERATOR (both are "in the back office"); a few
 * sensitive ones (user/role management, payment confirmation) are ADMIN-only — see the
 * per-method `@PreAuthorize`.
 */
@RestController
@RequestMapping("/admin")
@PreAuthorize("hasAuthority('admin.dashboard.view')")
class AdminController(
	private val getPlatformStatsUseCase: GetPlatformStatsUseCase,
	private val listRidesUseCase: ListRidesUseCase,
	private val listDriversUseCase: ListDriversUseCase,
	private val listOnlineDriversUseCase: ListOnlineDriversUseCase,
	private val listRecentSosUseCase: ListRecentSosUseCase,
	private val confirmCardPaymentUseCase: ConfirmCardPaymentUseCase,
	private val listUsersUseCase: ListUsersUseCase,
	private val changeUserRoleUseCase: ChangeUserRoleUseCase,
	private val listRecentOtpUseCase: ListRecentOtpUseCase,
	private val getDriverEarningsUseCase: GetDriverEarningsUseCase,
	private val getDriverActivityStatsUseCase: GetDriverActivityStatsUseCase,
	private val getSettingsUseCase: GetSettingsUseCase,
	private val updateSettingsUseCase: UpdateSettingsUseCase,
	private val getPlatformRevenueUseCase: GetPlatformRevenueUseCase,
	private val getSystemStatusUseCase: GetSystemStatusUseCase,
	private val approveDriverUseCase: ApproveDriverUseCase,
	private val rejectDriverUseCase: RejectDriverUseCase,
	private val getDriverDocumentUseCase: GetDriverDocumentUseCase,
	private val banUserUseCase: BanUserUseCase,
	private val unbanUserUseCase: UnbanUserUseCase,
	private val adminForceCancelRideUseCase: AdminForceCancelRideUseCase,
	private val adminReassignRideUseCase: AdminReassignRideUseCase,
	private val listPromoCodesUseCase: ListPromoCodesUseCase,
	private val createPromoCodeUseCase: CreatePromoCodeUseCase,
	private val updatePromoCodeUseCase: UpdatePromoCodeUseCase,
	private val adminGetRideDetailUseCase: AdminGetRideDetailUseCase,
	private val adminListRideMessagesUseCase: AdminListRideMessagesUseCase,
	private val recordAuditLogUseCase: RecordAuditLogUseCase,
	private val listAuditLogUseCase: ListAuditLogUseCase,
	private val getRevenueByTariffUseCase: GetRevenueByTariffUseCase,
	private val generatePayoutUseCase: GeneratePayoutUseCase,
	private val markPayoutPaidUseCase: MarkPayoutPaidUseCase,
	private val listPayoutsUseCase: ListPayoutsUseCase,
	private val sendBroadcastUseCase: SendBroadcastUseCase,
	private val listBroadcastsUseCase: ListBroadcastsUseCase,
) {

	@GetMapping("/stats")
	fun stats(): PlatformStatsResponse = PlatformStatsResponse.from(getPlatformStatsUseCase.execute())

	@PostMapping("/rides/{rideId}/payments/confirm")
	@PreAuthorize("hasAuthority('admin.finance.manage')")
	fun confirmPayment(@AuthenticationPrincipal principal: AuthenticatedPrincipal, @PathVariable rideId: UUID): RideResponse {
		val ride = confirmCardPaymentUseCase.execute(rideId)
		recordAuditLogUseCase.execute(principal.userId, "PAYMENT_CONFIRMED", "RIDE", rideId.toString())
		return RideResponse.from(ride)
	}

	@GetMapping("/rides")
	@PreAuthorize("hasAuthority('admin.rides.view')")
	fun rides(
		@RequestParam(required = false) status: RideStatus?,
		@RequestParam(required = false) driverId: UUID?,
		@RequestParam(defaultValue = "50") limit: Int,
	): List<RideResponse> = listRidesUseCase.execute(status, driverId, limit).map(RideResponse::from)

	/** The full ops view — passenger identity and dispatch offer history included, neither of
	 * which the passenger-facing `GET /rides/{id}` exposes. */
	@GetMapping("/rides/{rideId}")
	@PreAuthorize("hasAuthority('admin.rides.view')")
	fun rideDetail(@PathVariable rideId: UUID): AdminRideDetailResponse =
		AdminRideDetailResponse.from(adminGetRideDetailUseCase.execute(rideId))

	@GetMapping("/rides/{rideId}/messages")
	@PreAuthorize("hasAuthority('admin.rides.view')")
	fun rideMessages(@PathVariable rideId: UUID): List<MessageResponse> =
		adminListRideMessagesUseCase.execute(rideId).map(MessageResponse::from)

	@GetMapping("/drivers")
	@PreAuthorize("hasAuthority('admin.drivers.view')")
	fun drivers(): List<AdminDriverResponse> = listDriversUseCase.execute().map(AdminDriverResponse::from)

	@PostMapping("/rides/{rideId}/cancel")
	@PreAuthorize("hasAuthority('admin.rides.manage')")
	fun forceCancelRide(
		@AuthenticationPrincipal principal: AuthenticatedPrincipal,
		@PathVariable rideId: UUID,
		@RequestBody(required = false) request: CancelRideRequest?,
	): RideResponse {
		val ride = adminForceCancelRideUseCase.execute(rideId, request?.reason)
		recordAuditLogUseCase.execute(principal.userId, "RIDE_CANCELLED", "RIDE", rideId.toString(), request?.reason)
		return RideResponse.from(ride)
	}

	@PostMapping("/rides/{rideId}/reassign")
	@PreAuthorize("hasAuthority('admin.rides.manage')")
	fun reassignRide(
		@AuthenticationPrincipal principal: AuthenticatedPrincipal,
		@PathVariable rideId: UUID,
		@RequestBody(required = false) request: ReassignRideRequest?,
	): RideResponse {
		val ride = adminReassignRideUseCase.execute(rideId, request?.driverId)
		val details = request?.driverId?.let { "driver=$it" } ?: "auto-redispatch"
		recordAuditLogUseCase.execute(principal.userId, "RIDE_REASSIGNED", "RIDE", rideId.toString(), details)
		return RideResponse.from(ride)
	}

	@GetMapping("/drivers/online")
	@PreAuthorize("hasAuthority('admin.drivers.view')")
	fun onlineDrivers(): List<OnlineDriverResponse> =
		listOnlineDriversUseCase.execute().map(OnlineDriverResponse::from)

	@GetMapping("/sos")
	@PreAuthorize("hasAuthority('admin.rides.view')")
	fun sos(@RequestParam(defaultValue = "50") limit: Int): List<SosIncidentResponse> =
		listRecentSosUseCase.execute(limit).map(SosIncidentResponse::from)

	@GetMapping("/users")
	@PreAuthorize("hasAuthority('admin.users.view')")
	fun users(
		@RequestParam(required = false) role: Role?,
		@RequestParam(defaultValue = "100") limit: Int,
	): List<AdminUserResponse> = listUsersUseCase.execute(role, limit).map(AdminUserResponse::from)

	@PostMapping("/users/{userId}/role")
	@PreAuthorize("hasAuthority('admin.roles.manage')")
	fun changeRole(
		@AuthenticationPrincipal principal: AuthenticatedPrincipal,
		@PathVariable userId: UUID,
		@Valid @RequestBody request: ChangeRoleRequest,
	): AdminUserResponse {
		val user = changeUserRoleUseCase.execute(principal.userId, userId, request.role)
		recordAuditLogUseCase.execute(principal.userId, "USER_ROLE_CHANGED", "USER", userId.toString(), "role=${request.role}")
		return AdminUserResponse.from(user)
	}

	@GetMapping("/otp/recent")
	@PreAuthorize("hasAuthority('admin.otp-support.view')")
	fun recentOtp(@RequestParam(defaultValue = "50") limit: Int): List<OtpChallengeResponse> =
		listRecentOtpUseCase.execute(limit).map(OtpChallengeResponse::from)

	@GetMapping("/drivers/{driverId}/earnings")
	@PreAuthorize("hasAuthority('admin.finance.view')")
	fun driverEarnings(
		@PathVariable driverId: UUID,
		@RequestParam(required = false) from: Instant?,
		@RequestParam(required = false) to: Instant?,
	): DriverEarningsResponse {
		val rangeTo = to ?: Instant.now()
		val rangeFrom = from ?: rangeTo.minus(Duration.ofDays(30))
		return DriverEarningsResponse.from(getDriverEarningsUseCase.execute(driverId, rangeFrom, rangeTo))
	}

	@GetMapping("/drivers/{driverId}/stats")
	@PreAuthorize("hasAuthority('admin.drivers.view')")
	fun driverStats(@PathVariable driverId: UUID): DriverActivityStatsResponse =
		DriverActivityStatsResponse.from(getDriverActivityStatsUseCase.execute(driverId))

	@PostMapping("/drivers/{driverId}/verify")
	@PreAuthorize("hasAuthority('admin.drivers.moderate')")
	fun verifyDriver(
		@AuthenticationPrincipal principal: AuthenticatedPrincipal,
		@PathVariable driverId: UUID,
		@RequestBody request: VerifyDriverRequest,
	): ResponseEntity<Void> {
		if (request.approve) {
			approveDriverUseCase.execute(driverId)
			recordAuditLogUseCase.execute(principal.userId, "DRIVER_APPROVED", "DRIVER", driverId.toString())
		} else {
			rejectDriverUseCase.execute(driverId, request.reason)
			recordAuditLogUseCase.execute(principal.userId, "DRIVER_REJECTED", "DRIVER", driverId.toString(), request.reason)
		}
		return ResponseEntity.noContent().build()
	}

	@GetMapping("/drivers/{driverId}/documents/{kind}")
	@PreAuthorize("hasAuthority('admin.drivers.moderate')")
	fun driverDocument(@PathVariable driverId: UUID, @PathVariable kind: DocumentKind): ResponseEntity<ByteArray> {
		val document = getDriverDocumentUseCase.execute(driverId, kind)
		return ResponseEntity.ok().contentType(MediaType.parseMediaType(document.contentType)).body(document.bytes)
	}

	@PostMapping("/users/{userId}/ban")
	@PreAuthorize("hasAuthority('admin.users.manage')")
	fun banUser(
		@AuthenticationPrincipal principal: AuthenticatedPrincipal,
		@PathVariable userId: UUID,
		@RequestBody(required = false) request: BanUserRequest?,
	): AdminUserResponse {
		val user = banUserUseCase.execute(principal.userId, userId, request?.reason)
		recordAuditLogUseCase.execute(principal.userId, "USER_BANNED", "USER", userId.toString(), request?.reason)
		return AdminUserResponse.from(user)
	}

	@PostMapping("/users/{userId}/unban")
	@PreAuthorize("hasAuthority('admin.users.manage')")
	fun unbanUser(@AuthenticationPrincipal principal: AuthenticatedPrincipal, @PathVariable userId: UUID): AdminUserResponse {
		val user = unbanUserUseCase.execute(principal.userId, userId)
		recordAuditLogUseCase.execute(principal.userId, "USER_UNBANNED", "USER", userId.toString())
		return AdminUserResponse.from(user)
	}

	@PostMapping("/settings")
	@PreAuthorize("hasAuthority('admin.settings.manage')")
	fun updateSettings(
		@AuthenticationPrincipal principal: AuthenticatedPrincipal,
		@RequestBody request: UpdateSettingsRequest,
	): SettingsResponse {
		updateSettingsUseCase.execute(request.settings)
		recordAuditLogUseCase.execute(principal.userId, "SETTINGS_UPDATED", "SETTINGS", null, request.settings.keys.joinToString())
		return SettingsResponse(getSettingsUseCase.execute())
	}

	@GetMapping("/audit-log")
	@PreAuthorize("hasAuthority('admin.audit.view')")
	fun auditLog(@RequestParam(defaultValue = "100") limit: Int): List<AuditLogEntryResponse> =
		listAuditLogUseCase.execute(limit).map(AuditLogEntryResponse::from)

	@GetMapping("/revenue")
	@PreAuthorize("hasAuthority('admin.finance.view')")
	fun revenue(
		@RequestParam(required = false) from: Instant?,
		@RequestParam(required = false) to: Instant?,
	): PlatformRevenueResponse {
		val rangeTo = to ?: Instant.now()
		val rangeFrom = from ?: rangeTo.minus(Duration.ofDays(7))
		return PlatformRevenueResponse.from(getPlatformRevenueUseCase.execute(rangeFrom, rangeTo))
	}

	@GetMapping("/revenue/by-tariff")
	@PreAuthorize("hasAuthority('admin.finance.view')")
	fun revenueByTariff(
		@RequestParam(required = false) from: Instant?,
		@RequestParam(required = false) to: Instant?,
	): List<TariffRevenuePointResponse> {
		val rangeTo = to ?: Instant.now()
		val rangeFrom = from ?: rangeTo.minus(Duration.ofDays(7))
		return getRevenueByTariffUseCase.execute(rangeFrom, rangeTo).map(TariffRevenuePointResponse::from)
	}

	@GetMapping("/payouts")
	@PreAuthorize("hasAuthority('admin.finance.view')")
	fun payouts(
		@RequestParam(required = false) driverId: UUID?,
		@RequestParam(defaultValue = "100") limit: Int,
	): List<PayoutResponse> = listPayoutsUseCase.execute(driverId, limit).map(PayoutResponse::from)

	@PostMapping("/payouts")
	@PreAuthorize("hasAuthority('admin.finance.manage')")
	fun generatePayout(
		@AuthenticationPrincipal principal: AuthenticatedPrincipal,
		@Valid @RequestBody request: GeneratePayoutRequest,
	): PayoutResponse {
		val payout = generatePayoutUseCase.execute(request.driverId, request.periodFrom, request.periodTo)
		recordAuditLogUseCase.execute(
			principal.userId,
			"PAYOUT_GENERATED",
			"DRIVER",
			request.driverId.toString(),
			"${payout.amount} TMT for ${payout.rideCount} rides",
		)
		return PayoutResponse.from(payout)
	}

	@PostMapping("/payouts/{payoutId}/pay")
	@PreAuthorize("hasAuthority('admin.finance.manage')")
	fun markPayoutPaid(
		@AuthenticationPrincipal principal: AuthenticatedPrincipal,
		@PathVariable payoutId: UUID,
	): PayoutResponse {
		val payout = markPayoutPaidUseCase.execute(payoutId)
		recordAuditLogUseCase.execute(principal.userId, "PAYOUT_PAID", "DRIVER", payout.driverId.toString(), "${payout.amount} TMT")
		return PayoutResponse.from(payout)
	}

	@GetMapping("/broadcast")
	@PreAuthorize("hasAuthority('admin.notifications.send')")
	fun broadcastHistory(@RequestParam(defaultValue = "50") limit: Int): List<BroadcastLogResponse> =
		listBroadcastsUseCase.execute(limit).map(BroadcastLogResponse::from)

	@PostMapping("/broadcast")
	@PreAuthorize("hasAuthority('admin.notifications.send')")
	fun sendBroadcast(
		@AuthenticationPrincipal principal: AuthenticatedPrincipal,
		@Valid @RequestBody request: SendBroadcastRequest,
	): BroadcastLogResponse {
		val log = sendBroadcastUseCase.execute(principal.userId, request.segment, request.title, request.body)
		recordAuditLogUseCase.execute(
			principal.userId,
			"BROADCAST_SENT",
			"BROADCAST",
			log.id.toString(),
			"${request.segment} · ${log.recipientCount} recipients · ${request.title}",
		)
		return BroadcastLogResponse.from(log)
	}

	@GetMapping("/system-status")
	fun systemStatus(): SystemStatusResponse = SystemStatusResponse.from(getSystemStatusUseCase.execute())

	@GetMapping("/promo-codes")
	@PreAuthorize("hasAuthority('admin.promo.manage')")
	fun promoCodes(): List<AdminPromoCodeResponse> = listPromoCodesUseCase.execute().map(AdminPromoCodeResponse::from)

	@PostMapping("/promo-codes")
	@PreAuthorize("hasAuthority('admin.promo.manage')")
	fun createPromoCode(
		@AuthenticationPrincipal principal: AuthenticatedPrincipal,
		@Valid @RequestBody request: CreatePromoCodeRequest,
	): AdminPromoCodeResponse {
		val promo = createPromoCodeUseCase.execute(
			code = request.code,
			discountType = request.discountType,
			discountValue = request.discountValue,
			maxUses = request.maxUses,
			expiresAt = request.expiresAt,
		)
		recordAuditLogUseCase.execute(principal.userId, "PROMO_CREATED", "PROMO", promo.id.toString(), request.code)
		return AdminPromoCodeResponse.from(promo)
	}

	@PatchMapping("/promo-codes/{id}")
	@PreAuthorize("hasAuthority('admin.promo.manage')")
	fun updatePromoCode(
		@AuthenticationPrincipal principal: AuthenticatedPrincipal,
		@PathVariable id: UUID,
		@RequestBody request: UpdatePromoCodeRequest,
	): AdminPromoCodeResponse {
		val promo = updatePromoCodeUseCase.execute(
			id,
			PromoCodePatch(
				discountType = request.discountType,
				discountValue = request.discountValue,
				maxUses = request.maxUses,
				expiresAt = request.expiresAt,
				active = request.active,
			),
		)
		recordAuditLogUseCase.execute(principal.userId, "PROMO_UPDATED", "PROMO", id.toString())
		return AdminPromoCodeResponse.from(promo)
	}
}

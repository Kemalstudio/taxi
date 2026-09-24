package com.taxiplatform.domain.user

import java.time.Instant
import java.util.UUID

enum class Role {
	PASSENGER,
	DRIVER,
	OPERATOR,
	DISPATCHER,
	MODERATOR,
	ACCOUNTANT,
	ADMIN,
	SUPER_ADMIN,
}

enum class Permission(val authority: String) {
	DASHBOARD_VIEW("admin.dashboard.view"),
	RIDE_VIEW("admin.rides.view"),
	RIDE_MANAGE("admin.rides.manage"),
	DRIVER_VIEW("admin.drivers.view"),
	DRIVER_MODERATE("admin.drivers.moderate"),
	USER_VIEW("admin.users.view"),
	USER_MANAGE("admin.users.manage"),
	ROLE_MANAGE("admin.roles.manage"),
	FINANCE_VIEW("admin.finance.view"),
	FINANCE_MANAGE("admin.finance.manage"),
	PROMO_MANAGE("admin.promo.manage"),
	NOTIFICATION_SEND("admin.notifications.send"),
	SETTINGS_MANAGE("admin.settings.manage"),
	AUDIT_VIEW("admin.audit.view"),
	OTP_SUPPORT_VIEW("admin.otp-support.view"),
}

fun Role.isBackOffice(): Boolean = this !in setOf(Role.PASSENGER, Role.DRIVER)

/** Central permission matrix used by both endpoint authorization and the JWT filter. */
fun Role.permissions(): Set<Permission> = when (this) {
	Role.SUPER_ADMIN -> Permission.entries.toSet()
	Role.ADMIN -> Permission.entries.toSet() - Permission.ROLE_MANAGE
	Role.DISPATCHER -> setOf(
		Permission.DASHBOARD_VIEW,
		Permission.RIDE_VIEW,
		Permission.RIDE_MANAGE,
		Permission.DRIVER_VIEW,
	)
	Role.MODERATOR -> setOf(
		Permission.DASHBOARD_VIEW,
		Permission.DRIVER_VIEW,
		Permission.DRIVER_MODERATE,
		Permission.USER_VIEW,
		Permission.USER_MANAGE,
	)
	Role.ACCOUNTANT -> setOf(
		Permission.DASHBOARD_VIEW,
		Permission.DRIVER_VIEW,
		Permission.FINANCE_VIEW,
		Permission.FINANCE_MANAGE,
	)
	// Legacy role retained for existing accounts; equivalent to an operations viewer.
	Role.OPERATOR -> setOf(
		Permission.DASHBOARD_VIEW,
		Permission.RIDE_VIEW,
		Permission.DRIVER_VIEW,
		Permission.FINANCE_VIEW,
	)
	Role.PASSENGER,
	Role.DRIVER,
	-> emptySet()
}

data class User(
	val id: UUID,
	val email: String,
	val passwordHash: String,
	val role: Role,
	val fullName: String,
	val phone: String?,
	val createdAt: Instant,
	val loyaltyPoints: Int = 0,
	val banned: Boolean = false,
	val bannedReason: String? = null,
	val bannedAt: Instant? = null,
	val twoFactorEnabled: Boolean = false,
	val twoFactorSecret: String? = null,
	val twoFactorVerifiedAt: Instant? = null,
	val twoFactorLastUsedStep: Long? = null,
)

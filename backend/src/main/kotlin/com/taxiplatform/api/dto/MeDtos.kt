package com.taxiplatform.api.dto

import com.taxiplatform.domain.user.User
import com.taxiplatform.domain.user.permissions
import java.util.UUID

data class MeResponse(
	val userId: UUID,
	val fullName: String,
	val phone: String?,
	val role: String,
	val loyaltyPoints: Int,
	val permissions: Set<String>,
	val twoFactorEnabled: Boolean,
) {
	companion object {
		fun from(user: User) = MeResponse(
			userId = user.id,
			fullName = user.fullName,
			phone = user.phone,
			role = user.role.name,
			loyaltyPoints = user.loyaltyPoints,
			permissions = user.role.permissions().mapTo(linkedSetOf()) { it.authority },
			twoFactorEnabled = user.twoFactorEnabled,
		)
	}
}

data class PushSubscriptionKeysDto(
	val p256dh: String,
	val auth: String,
)

data class PushSubscriptionRequest(
	val endpoint: String,
	val keys: PushSubscriptionKeysDto,
)

data class UnsubscribePushRequest(
	val endpoint: String,
)

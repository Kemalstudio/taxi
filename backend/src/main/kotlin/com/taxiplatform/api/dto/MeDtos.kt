package com.taxiplatform.api.dto

import com.taxiplatform.domain.user.User
import java.util.UUID

data class MeResponse(
	val userId: UUID,
	val fullName: String,
	val phone: String?,
	val role: String,
	val loyaltyPoints: Int,
) {
	companion object {
		fun from(user: User) = MeResponse(
			userId = user.id,
			fullName = user.fullName,
			phone = user.phone,
			role = user.role.name,
			loyaltyPoints = user.loyaltyPoints,
		)
	}
}

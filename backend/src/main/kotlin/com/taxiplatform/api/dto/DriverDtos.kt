package com.taxiplatform.api.dto

import com.taxiplatform.application.driver.DriverMe
import com.taxiplatform.domain.driver.DriverStatus
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.util.UUID

data class DriverStatusRequest(
	val status: DriverStatus,
)

/** Vehicle info the driver sets themselves — plate format is checked in UpdateDriverVehicleUseCase,
 * not here, because it needs normalizing (case/whitespace) before it can be matched. */
data class UpdateVehicleRequest(
	@field:NotBlank @field:Size(max = 60)
	val vehicleMake: String,

	@field:NotBlank @field:Size(max = 60)
	val vehicleModel: String,

	@field:NotBlank @field:Size(max = 20)
	val plateNumber: String,
)

data class DriverLocationRequest(
	@field:DecimalMin("-90.0") @field:DecimalMax("90.0")
	val lat: Double,

	@field:DecimalMin("-180.0") @field:DecimalMax("180.0")
	val lng: Double,
)

/** The signed-in driver's own account + vehicle profile — backs the driver web cabinet. */
data class DriverMeResponse(
	val userId: UUID,
	val fullName: String,
	val phone: String?,
	val vehicleMake: String?,
	val vehicleModel: String?,
	val plateNumber: String?,
	val rating: String,
	val status: String,
	val verificationStatus: String,
	val rejectionReason: String?,
	val hasLicenseDoc: Boolean,
	val hasVehicleDoc: Boolean,
) {
	companion object {
		fun from(me: DriverMe) = DriverMeResponse(
			userId = me.user.id,
			fullName = me.user.fullName,
			phone = me.user.phone,
			vehicleMake = me.profile.vehicleMake,
			vehicleModel = me.profile.vehicleModel,
			plateNumber = me.profile.plateNumber,
			rating = me.profile.rating.toPlainString(),
			status = me.profile.status.name,
			verificationStatus = me.profile.verificationStatus.name,
			rejectionReason = me.profile.rejectionReason,
			hasLicenseDoc = me.profile.licenseDocPath != null,
			hasVehicleDoc = me.profile.vehicleDocPath != null,
		)
	}
}

package com.taxiplatform.api.error

import com.taxiplatform.application.admin.DriverDocumentNotFoundException
import com.taxiplatform.application.admin.NoEarningsInPeriodException
import com.taxiplatform.application.admin.PayoutAlreadyPaidException
import com.taxiplatform.application.admin.PayoutNotFoundException
import com.taxiplatform.application.admin.UserNotFoundException
import com.taxiplatform.application.admin.UserManagementDeniedException
import com.taxiplatform.application.auth.AccountBannedException
import com.taxiplatform.application.auth.EmailAlreadyRegisteredException
import com.taxiplatform.application.auth.InvalidCredentialsException
import com.taxiplatform.application.auth.AdminTwoFactorRequiredException
import com.taxiplatform.application.auth.AdminLoginRateLimitedException
import com.taxiplatform.application.auth.InvalidTwoFactorChallengeException
import com.taxiplatform.application.auth.InvalidTwoFactorCodeException
import com.taxiplatform.application.auth.OtpInvalidException
import com.taxiplatform.application.auth.OtpRateLimitedException
import com.taxiplatform.application.driver.InvalidPlateNumberException
import com.taxiplatform.application.driver.PlateNumberTakenException
import com.taxiplatform.application.promo.PromoCodeAlreadyExistsException
import com.taxiplatform.application.promo.PromoCodeNotFoundException
import com.taxiplatform.application.ride.AlreadyRatedException
import com.taxiplatform.application.ride.DriverProfileNotFoundException
import com.taxiplatform.application.ride.InvalidRideStateException
import com.taxiplatform.application.ride.NoPendingOfferException
import com.taxiplatform.application.ride.RideAccessDeniedException
import com.taxiplatform.application.ride.RideNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.Instant

data class ApiError(
	val timestamp: Instant,
	val status: Int,
	val error: String,
	val message: String,
)

@RestControllerAdvice
class GlobalExceptionHandler {

	private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

	@ExceptionHandler(EmailAlreadyRegisteredException::class, PromoCodeAlreadyExistsException::class, PlateNumberTakenException::class)
	fun handleConflict(ex: RuntimeException) = respond(HttpStatus.CONFLICT, ex.message)

	@ExceptionHandler(InvalidPlateNumberException::class)
	fun handleInvalidPlate(ex: InvalidPlateNumberException) = respond(HttpStatus.BAD_REQUEST, ex.message)

	@ExceptionHandler(
		InvalidCredentialsException::class,
		OtpInvalidException::class,
		InvalidTwoFactorChallengeException::class,
		InvalidTwoFactorCodeException::class,
	)
	fun handleUnauthorized(ex: RuntimeException) = respond(HttpStatus.UNAUTHORIZED, ex.message)

	@ExceptionHandler(AdminTwoFactorRequiredException::class)
	fun handleTwoFactorRequired(ex: AdminTwoFactorRequiredException) = respond(HttpStatus.FORBIDDEN, ex.message)

	@ExceptionHandler(AccountBannedException::class)
	fun handleBanned(ex: AccountBannedException) = respond(HttpStatus.FORBIDDEN, ex.message)

	@ExceptionHandler(OtpRateLimitedException::class, AdminLoginRateLimitedException::class)
	fun handleRateLimited(ex: OtpRateLimitedException) = respond(HttpStatus.TOO_MANY_REQUESTS, ex.message)

	@ExceptionHandler(
		RideNotFoundException::class,
		DriverProfileNotFoundException::class,
		UserNotFoundException::class,
		DriverDocumentNotFoundException::class,
		PromoCodeNotFoundException::class,
		PayoutNotFoundException::class,
	)
	fun handleNotFound(ex: RuntimeException) = respond(HttpStatus.NOT_FOUND, ex.message)

	@ExceptionHandler(
		NoPendingOfferException::class,
		InvalidRideStateException::class,
		AlreadyRatedException::class,
		PayoutAlreadyPaidException::class,
	)
	fun handleConflictState(ex: RuntimeException) = respond(HttpStatus.CONFLICT, ex.message)

	@ExceptionHandler(NoEarningsInPeriodException::class)
	fun handleNoEarnings(ex: NoEarningsInPeriodException) = respond(HttpStatus.BAD_REQUEST, ex.message)

	@ExceptionHandler(RideAccessDeniedException::class, UserManagementDeniedException::class)
	fun handleForbidden(ex: RuntimeException) = respond(HttpStatus.FORBIDDEN, ex.message)

	@ExceptionHandler(MethodArgumentNotValidException::class)
	fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ApiError> {
		val message = ex.bindingResult.fieldErrors.joinToString("; ") { "${it.field}: ${it.defaultMessage}" }
		return respond(HttpStatus.BAD_REQUEST, message)
	}

	@ExceptionHandler(HttpMessageNotReadableException::class)
	fun handleMalformedRequest(ex: HttpMessageNotReadableException) = respond(HttpStatus.BAD_REQUEST, "Malformed request body")

	@ExceptionHandler(IllegalArgumentException::class)
	fun handleIllegalArgument(ex: IllegalArgumentException) = respond(HttpStatus.BAD_REQUEST, ex.message)

	@ExceptionHandler(Exception::class)
	fun handleUnexpected(ex: Exception): ResponseEntity<ApiError> {
		log.error("Unhandled exception", ex)
		return respond(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred")
	}

	private fun respond(status: HttpStatus, message: String?): ResponseEntity<ApiError> =
		ResponseEntity.status(status).body(
			ApiError(
				timestamp = Instant.now(),
				status = status.value(),
				error = status.reasonPhrase,
				message = message ?: status.reasonPhrase,
			),
		)
}

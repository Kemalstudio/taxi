package com.taxiplatform.api.error

import com.taxiplatform.application.auth.EmailAlreadyRegisteredException
import com.taxiplatform.application.auth.InvalidCredentialsException
import com.taxiplatform.application.ride.DriverProfileNotFoundException
import com.taxiplatform.application.ride.InvalidRideStateException
import com.taxiplatform.application.ride.NoPendingOfferException
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

	@ExceptionHandler(EmailAlreadyRegisteredException::class)
	fun handleConflict(ex: EmailAlreadyRegisteredException) = respond(HttpStatus.CONFLICT, ex.message)

	@ExceptionHandler(InvalidCredentialsException::class)
	fun handleUnauthorized(ex: InvalidCredentialsException) = respond(HttpStatus.UNAUTHORIZED, ex.message)

	@ExceptionHandler(RideNotFoundException::class, DriverProfileNotFoundException::class)
	fun handleNotFound(ex: RuntimeException) = respond(HttpStatus.NOT_FOUND, ex.message)

	@ExceptionHandler(NoPendingOfferException::class, InvalidRideStateException::class)
	fun handleConflictState(ex: RuntimeException) = respond(HttpStatus.CONFLICT, ex.message)

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

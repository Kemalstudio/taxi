package com.taxiplatform.application.auth

class EmailAlreadyRegisteredException(email: String) : RuntimeException("Email already registered: $email")

class InvalidCredentialsException : RuntimeException("Invalid email or password")

class OtpRateLimitedException : RuntimeException("Too many codes requested for this phone — try again later")

class OtpInvalidException : RuntimeException("Invalid or expired code")

class AccountBannedException(reason: String?) :
	RuntimeException(if (reason.isNullOrBlank()) "This account has been banned" else "This account has been banned: $reason")

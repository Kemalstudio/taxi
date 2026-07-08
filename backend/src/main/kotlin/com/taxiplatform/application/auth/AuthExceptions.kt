package com.taxiplatform.application.auth

class EmailAlreadyRegisteredException(email: String) : RuntimeException("Email already registered: $email")

class InvalidCredentialsException : RuntimeException("Invalid email or password")

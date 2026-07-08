package com.taxiplatform.application.ports

interface PasswordHasher {
	fun hash(rawPassword: String): String
	fun matches(rawPassword: String, hashedPassword: String): Boolean
}

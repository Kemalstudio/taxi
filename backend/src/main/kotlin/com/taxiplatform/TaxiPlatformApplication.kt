package com.taxiplatform

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class TaxiPlatformApplication

fun main(args: Array<String>) {
	runApplication<TaxiPlatformApplication>(*args)
}

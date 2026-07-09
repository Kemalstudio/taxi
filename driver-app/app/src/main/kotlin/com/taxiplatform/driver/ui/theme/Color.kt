package com.taxiplatform.driver.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

val TaxiAmber = Color(0xFFF5A623)
val TaxiInk = Color(0xFF1B1F2A)

val LightColorScheme = lightColorScheme(
	primary = TaxiAmber,
	onPrimary = TaxiInk,
	secondary = TaxiInk,
)

val DarkColorScheme = darkColorScheme(
	primary = TaxiAmber,
	onPrimary = TaxiInk,
	secondary = TaxiAmber,
)

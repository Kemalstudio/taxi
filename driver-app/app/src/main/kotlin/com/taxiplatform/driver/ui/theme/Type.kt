package com.taxiplatform.driver.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(
	headlineMedium = TextStyle(
		fontWeight = FontWeight.SemiBold,
		fontSize = 28.sp,
		lineHeight = 34.sp,
	),
	bodyLarge = TextStyle(
		fontWeight = FontWeight.Normal,
		fontSize = 16.sp,
		lineHeight = 24.sp,
	),
)

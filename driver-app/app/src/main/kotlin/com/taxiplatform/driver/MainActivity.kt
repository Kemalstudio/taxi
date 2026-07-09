package com.taxiplatform.driver

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.taxiplatform.driver.ui.navigation.DriverNavHost
import com.taxiplatform.driver.ui.theme.TaxiDriverTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		setContent {
			TaxiDriverTheme {
				DriverNavHost()
			}
		}
	}
}

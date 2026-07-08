package com.taxiplatform.application.dispatch

import com.taxiplatform.application.ports.RideOfferRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant

/**
 * Sweeps ride offers that were never explicitly accepted/rejected and expired,
 * advancing dispatch to the next-nearest driver for each one. This is what makes
 * an *ignored* (not just rejected) offer reassign automatically.
 */
@Component
class OfferTimeoutScheduler(
	private val rideOfferRepository: RideOfferRepository,
	private val dispatchService: DispatchService,
) {
	private val log = LoggerFactory.getLogger(OfferTimeoutScheduler::class.java)

	@Scheduled(fixedDelayString = "\${taxi.dispatch.offer-sweep-interval-ms}")
	fun sweepExpiredOffers() {
		val expired = rideOfferRepository.findExpiredPending(Instant.now())
		expired.forEach { offer ->
			log.info("Offer {} for ride {} expired without response, advancing dispatch", offer.id, offer.rideId)
			dispatchService.expireOffer(offer)
		}
	}
}

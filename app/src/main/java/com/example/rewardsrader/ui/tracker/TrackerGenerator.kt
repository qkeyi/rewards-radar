package com.example.rewardsrader.ui.tracker

import com.example.rewardsrader.data.local.entity.BenefitFrequency
import com.example.rewardsrader.data.local.entity.BenefitType
import com.example.rewardsrader.data.local.entity.CardSubDurationUnit
import com.example.rewardsrader.data.local.entity.ProfileCardWithRelations
import com.example.rewardsrader.data.local.entity.TrackerEntity
import com.example.rewardsrader.data.local.entity.TrackerSourceType
import java.time.LocalDate
import java.time.Period

class TrackerGenerator(
    private val newId: () -> String
) {
    fun generateMissingTrackers(
        cards: List<ProfileCardWithRelations>,
        existing: List<TrackerEntity>,
        today: LocalDate = LocalDate.now()
    ): TrackerGenerationResult {
        val existingKeys = existing.mapTo(mutableSetOf()) { trackerKey(it) }
        val newTrackers = mutableListOf<TrackerEntity>()
        val updatedTrackers = mutableListOf<TrackerEntity>()
        val offerTrackersById = existing
            .filter { it.type == TrackerSourceType.Offer && it.offerId != null }
            .groupBy { it.offerId!! }

        cards.forEach { card ->
            val profileCardId = card.profileCard.id
            val cardStart = parseTrackerDate(card.profileCard.openDateUtc) ?: today
            card.benefits.forEach { entry ->
                if (entry.benefit.type != BenefitType.Credit) return@forEach
                val frequency = entry.benefit.frequency
                if (today.isBefore(cardStart)) return@forEach
                when (frequency) {
                    BenefitFrequency.EveryTransaction -> {
                        val periodStart = today.withDayOfMonth(1)
                        val periodEnd = periodStart.plusMonths(1).minusDays(1)
                        addTracker(
                            existingKeys = existingKeys,
                            newTrackers = newTrackers,
                            profileCardId = profileCardId,
                            profileCardBenefitId = entry.link.id,
                            offerId = null,
                            sourceType = TrackerSourceType.Benefit,
                            startDate = periodStart,
                            endDate = periodEnd
                        )
                    }
                    BenefitFrequency.EveryAnniversary -> {
                        val period = Period.ofYears(1)
                        val periodStart = currentPeriodStart(cardStart, today, period)
                        val periodEnd = periodStart.plus(period).minusDays(1)
                        addTracker(
                            existingKeys = existingKeys,
                            newTrackers = newTrackers,
                            profileCardId = profileCardId,
                            profileCardBenefitId = entry.link.id,
                            offerId = null,
                            sourceType = TrackerSourceType.Benefit,
                            startDate = periodStart,
                            endDate = periodEnd
                        )
                    }
                    else -> {
                        if (isCalendarFrequency(frequency)) {
                            val periodStart = calendarPeriodStart(today, frequency)
                            val periodEnd = calendarPeriodEnd(periodStart, frequency)
                            addTracker(
                                existingKeys = existingKeys,
                                newTrackers = newTrackers,
                                profileCardId = profileCardId,
                                profileCardBenefitId = entry.link.id,
                                offerId = null,
                                sourceType = TrackerSourceType.Benefit,
                                startDate = periodStart,
                                endDate = periodEnd
                            )
                        }
                    }
                }
            }

            val subSpending = card.profileCard.subSpending
            val subDuration = card.profileCard.subDuration
            if (subSpending != null && subSpending > 0.0 && subDuration != null && subDuration > 0) {
                val startDate = parseTrackerDate(card.profileCard.openDateUtc) ?: today
                val unit = card.profileCard.subDurationUnit ?: CardSubDurationUnit.Month
                val endDate = when (unit) {
                    CardSubDurationUnit.Day -> startDate.plusDays(subDuration.toLong()).minusDays(1)
                    CardSubDurationUnit.Month -> startDate.plusMonths(subDuration.toLong()).minusDays(1)
                }
                val safeEnd = if (endDate.isBefore(startDate)) startDate else endDate
                addTracker(
                    existingKeys = existingKeys,
                    newTrackers = newTrackers,
                    profileCardId = profileCardId,
                    profileCardBenefitId = null,
                    offerId = null,
                    sourceType = TrackerSourceType.Sub,
                    startDate = startDate,
                    endDate = safeEnd
                )
            }

            card.offers.forEach { offer ->
                val startDate = parseTrackerDate(offer.startDateUtc) ?: today
                val endDate = parseTrackerDate(offer.endDateUtc) ?: startDate
                val safeEnd = if (endDate.isBefore(startDate)) startDate else endDate
                val existingOfferTracker = offerTrackersById[offer.id]
                    ?.maxByOrNull { tracker -> parseTrackerDate(tracker.endDateUtc) ?: LocalDate.MIN }
                if (existingOfferTracker != null) {
                    val newStart = formatTrackerDate(startDate)
                    val newEnd = formatTrackerDate(safeEnd)
                    if (existingOfferTracker.startDateUtc != newStart ||
                        existingOfferTracker.endDateUtc != newEnd
                    ) {
                        updatedTrackers.add(
                            existingOfferTracker.copy(
                                startDateUtc = newStart,
                                endDateUtc = newEnd
                            )
                        )
                    }
                    return@forEach
                }
                if (shouldGenerateTracker(startDate, safeEnd, today)) {
                    addTracker(
                        existingKeys = existingKeys,
                        newTrackers = newTrackers,
                        profileCardId = profileCardId,
                        profileCardBenefitId = null,
                        offerId = offer.id,
                        sourceType = TrackerSourceType.Offer,
                        startDate = startDate,
                        endDate = safeEnd
                    )
                }
            }
        }

        return TrackerGenerationResult(
            newTrackers = newTrackers,
            updatedTrackers = updatedTrackers
        )
    }

    private fun addTracker(
        existingKeys: MutableSet<TrackerKey>,
        newTrackers: MutableList<TrackerEntity>,
        profileCardId: String,
        profileCardBenefitId: String?,
        offerId: String?,
        sourceType: TrackerSourceType,
        startDate: LocalDate,
        endDate: LocalDate
    ) {
        val key = TrackerKey(
            profileCardId = profileCardId,
            sourceType = sourceType,
            profileCardBenefitId = profileCardBenefitId,
            offerId = offerId,
            startDate = formatTrackerDate(startDate),
            endDate = formatTrackerDate(endDate)
        )
        if (existingKeys.add(key)) {
            newTrackers.add(
                TrackerEntity(
                    id = newId(),
                    profileCardId = profileCardId,
                    profileCardBenefitId = profileCardBenefitId,
                    offerId = offerId,
                    type = sourceType,
                    startDateUtc = key.startDate,
                    endDateUtc = key.endDate
                )
            )
        }
    }

    private fun shouldGenerateTracker(
        startDate: LocalDate,
        endDate: LocalDate?,
        today: LocalDate
    ): Boolean {
        if (today.isBefore(startDate)) return false
        if (endDate != null && today.isAfter(endDate)) return false
        return true
    }

    private fun isCalendarFrequency(frequency: BenefitFrequency): Boolean =
        when (frequency) {
            BenefitFrequency.Monthly,
            BenefitFrequency.Quarterly,
            BenefitFrequency.SemiAnnually,
            BenefitFrequency.Annually -> true
            else -> false
        }

    private fun currentPeriodStart(
        startDate: LocalDate,
        today: LocalDate,
        period: Period
    ): LocalDate {
        var periodStart = startDate
        while (!periodStart.plus(period).isAfter(today)) {
            periodStart = periodStart.plus(period)
        }
        return periodStart
    }

    private fun calendarPeriodStart(date: LocalDate, frequency: BenefitFrequency): LocalDate =
        when (frequency) {
            BenefitFrequency.Monthly -> date.withDayOfMonth(1)
            BenefitFrequency.Quarterly -> {
                val startMonth = ((date.monthValue - 1) / 3) * 3 + 1
                LocalDate.of(date.year, startMonth, 1)
            }
            BenefitFrequency.SemiAnnually -> {
                val startMonth = if (date.monthValue <= 6) 1 else 7
                LocalDate.of(date.year, startMonth, 1)
            }
            BenefitFrequency.Annually -> LocalDate.of(date.year, 1, 1)
            else -> date
        }

    private fun calendarPeriodEnd(date: LocalDate, frequency: BenefitFrequency): LocalDate {
        val start = calendarPeriodStart(date, frequency)
        return when (frequency) {
            BenefitFrequency.Monthly -> start.plusMonths(1).minusDays(1)
            BenefitFrequency.Quarterly -> start.plusMonths(3).minusDays(1)
            BenefitFrequency.SemiAnnually -> start.plusMonths(6).minusDays(1)
            BenefitFrequency.Annually -> start.plusYears(1).minusDays(1)
            else -> start
        }
    }

    private fun trackerKey(tracker: TrackerEntity) = TrackerKey(
        profileCardId = tracker.profileCardId,
        sourceType = tracker.type,
        profileCardBenefitId = tracker.profileCardBenefitId,
        offerId = tracker.offerId,
        startDate = tracker.startDateUtc,
        endDate = tracker.endDateUtc
    )

    private data class TrackerKey(
        val profileCardId: String,
        val sourceType: TrackerSourceType,
        val profileCardBenefitId: String?,
        val offerId: String?,
        val startDate: String,
        val endDate: String
    )
}

data class TrackerGenerationResult(
    val newTrackers: List<TrackerEntity>,
    val updatedTrackers: List<TrackerEntity>
)

package com.sportsxtreme.app.presentation.match

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle
import java.util.Locale

internal object TournamentStartDatePolicy {
    const val NOT_STARTED_MESSAGE = "Start date of tournament is not reached change date or wait till start date"

    fun hasNotReached(startDate: String, today: LocalDate = LocalDate.now()): Boolean {
        val date = startDate.trim().takeIf(String::isNotEmpty)?.let(::parseDate) ?: return false
        return today.isBefore(date)
    }

    private fun parseDate(value: String): LocalDate? {
        val patterns = listOf("dd/MM/uuuu", "d/M/uuuu")
        for (pattern in patterns) {
            try {
                return LocalDate.parse(
                    value,
                    DateTimeFormatter.ofPattern(pattern, Locale.getDefault())
                        .withResolverStyle(ResolverStyle.STRICT)
                )
            } catch (_: DateTimeParseException) {
                // Try the next supported date shape.
            }
        }
        return null
    }
}

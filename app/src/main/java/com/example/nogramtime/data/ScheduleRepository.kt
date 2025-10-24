package com.example.nogramtime.data

import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

/**
 * Repository responsible for retrieving and evaluating blocking rules. It
 * delegates persistence to the DAO and exposes convenience methods to
 * determine whether Instagram should be blocked at a given time.
 */
class ScheduleRepository(private val dao: BlockRuleDao) {
    /**
     * A Flow of the current rules. Collected in the UI to update lists.
     */
    val rulesFlow: Flow<List<BlockRule>> = dao.getRulesFlow()

    /**
     * Returns true if the current time falls within any active blocking rule.
     * The optional [now] parameter allows tests to specify a fixed moment.
     */
    suspend fun isInstagramBlockedNow(now: LocalDateTime = LocalDateTime.now()): Boolean {
        val day = now.dayOfWeek.value
        val currentMinutes = now.hour * 60 + now.minute
        val rules = dao.getRules()
        for (rule in rules) {
            if (!rule.days.contains(day)) continue
            val start = rule.startHour * 60 + rule.startMinute
            val end = rule.endHour * 60 + rule.endMinute
            val inRange = if (start <= end) {
                currentMinutes in start until end
            } else {
                // Rule wraps around midnight. Consider times after start OR before end.
                currentMinutes >= start || currentMinutes < end
            }
            if (inRange) return true
        }
        return false
    }
}
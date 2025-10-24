package com.example.nogramtime.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDateTime

/**
 * Unit tests for ScheduleRepository to verify blocking logic works correctly.
 * These tests use a fake DAO to avoid database dependencies.
 */
class ScheduleRepositoryTest {

    private lateinit var repository: ScheduleRepository
    private lateinit var fakeDao: FakeBlockRuleDao

    @Before
    fun setup() {
        fakeDao = FakeBlockRuleDao()
        repository = ScheduleRepository(fakeDao)
    }

    @Test
    fun `isInstagramBlockedNow returns true when time is within rule`() = runBlocking {
        // Rule: Monday 09:00 - 17:00
        fakeDao.addRule(
            BlockRule(
                id = 1,
                startHour = 9,
                startMinute = 0,
                endHour = 17,
                endMinute = 0,
                days = listOf(DayOfWeek.MONDAY.value)
            )
        )

        // Test time: Monday 12:00
        val testTime = LocalDateTime.of(2025, 10, 27, 12, 0) // Monday
        val result = repository.isInstagramBlockedNow(testTime)

        assertTrue("Should be blocked at 12:00 on Monday", result)
    }

    @Test
    fun `isInstagramBlockedNow returns false when time is outside rule`() = runBlocking {
        // Rule: Monday 09:00 - 17:00
        fakeDao.addRule(
            BlockRule(
                id = 1,
                startHour = 9,
                startMinute = 0,
                endHour = 17,
                endMinute = 0,
                days = listOf(DayOfWeek.MONDAY.value)
            )
        )

        // Test time: Monday 18:00 (after end time)
        val testTime = LocalDateTime.of(2025, 10, 27, 18, 0)
        val result = repository.isInstagramBlockedNow(testTime)

        assertFalse("Should not be blocked at 18:00 on Monday", result)
    }

    @Test
    fun `isInstagramBlockedNow returns false when day does not match`() = runBlocking {
        // Rule: Monday 09:00 - 17:00
        fakeDao.addRule(
            BlockRule(
                id = 1,
                startHour = 9,
                startMinute = 0,
                endHour = 17,
                endMinute = 0,
                days = listOf(DayOfWeek.MONDAY.value)
            )
        )

        // Test time: Tuesday 12:00
        val testTime = LocalDateTime.of(2025, 10, 28, 12, 0) // Tuesday
        val result = repository.isInstagramBlockedNow(testTime)

        assertFalse("Should not be blocked on Tuesday", result)
    }

    @Test
    fun `isInstagramBlockedNow handles midnight wraparound correctly`() = runBlocking {
        // Rule: Monday 22:00 - 02:00 (wraps to Tuesday)
        fakeDao.addRule(
            BlockRule(
                id = 1,
                startHour = 22,
                startMinute = 0,
                endHour = 2,
                endMinute = 0,
                days = listOf(DayOfWeek.MONDAY.value)
            )
        )

        // Test time: Monday 23:00 (after start, before midnight)
        val testTime1 = LocalDateTime.of(2025, 10, 27, 23, 0)
        assertTrue("Should be blocked at 23:00 on Monday", repository.isInstagramBlockedNow(testTime1))

        // Test time: Monday 01:00 (after midnight, before end)
        val testTime2 = LocalDateTime.of(2025, 10, 27, 1, 0)
        assertTrue("Should be blocked at 01:00 on Monday", repository.isInstagramBlockedNow(testTime2))

        // Test time: Monday 03:00 (after end time)
        val testTime3 = LocalDateTime.of(2025, 10, 27, 3, 0)
        assertFalse("Should not be blocked at 03:00 on Monday", repository.isInstagramBlockedNow(testTime3))
    }

    @Test
    fun `isInstagramBlockedNow returns true when multiple rules match`() = runBlocking {
        // Rule 1: Monday 09:00 - 12:00
        fakeDao.addRule(
            BlockRule(
                id = 1,
                startHour = 9,
                startMinute = 0,
                endHour = 12,
                endMinute = 0,
                days = listOf(DayOfWeek.MONDAY.value)
            )
        )

        // Rule 2: Monday 14:00 - 17:00
        fakeDao.addRule(
            BlockRule(
                id = 2,
                startHour = 14,
                startMinute = 0,
                endHour = 17,
                endMinute = 0,
                days = listOf(DayOfWeek.MONDAY.value)
            )
        )

        // Test time: Monday 15:00 (matches second rule)
        val testTime = LocalDateTime.of(2025, 10, 27, 15, 0)
        val result = repository.isInstagramBlockedNow(testTime)

        assertTrue("Should be blocked at 15:00 on Monday", result)
    }

    @Test
    fun `isInstagramBlockedNow returns false when no rules exist`() = runBlocking {
        // No rules added
        val testTime = LocalDateTime.of(2025, 10, 27, 12, 0)
        val result = repository.isInstagramBlockedNow(testTime)

        assertFalse("Should not be blocked when no rules exist", result)
    }

    /**
     * Fake DAO implementation for testing without database dependencies.
     */
    private class FakeBlockRuleDao : BlockRuleDao {
        private val rules = mutableListOf<BlockRule>()

        fun addRule(rule: BlockRule) {
            rules.add(rule)
        }

        override fun getRulesFlow(): Flow<List<BlockRule>> = flowOf(rules)

        override suspend fun getRules(): List<BlockRule> = rules

        override suspend fun count(): Int = rules.size

        override suspend fun insert(rule: BlockRule): Long {
            rules.add(rule)
            return rule.id
        }

        override suspend fun update(rule: BlockRule) {
            val index = rules.indexOfFirst { it.id == rule.id }
            if (index != -1) {
                rules[index] = rule
            }
        }

        override suspend fun delete(rule: BlockRule) {
            rules.removeIf { it.id == rule.id }
        }
    }
}

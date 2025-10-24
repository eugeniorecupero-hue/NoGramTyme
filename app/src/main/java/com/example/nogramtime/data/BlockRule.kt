package com.example.nogramtime.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a blocking rule. A rule specifies a start and end time
 * (expressed as hour and minute) and the days of the week on which the rule is
 * active. Days are represented by their ISO‐8601 values (1 = Monday … 7 =
 * Sunday). If a rule crosses midnight the end time can be less than the start
 * time.
 */
@Entity
data class BlockRule(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val days: List<Int>
)
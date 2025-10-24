package com.example.nogramtime.data

import androidx.room.TypeConverter

/**
 * Room cannot persist lists directly, so this converter stores a list of
 * integers as a comma separated string. ISO 8601 day numbers (1–7) are used
 * to represent the days of the week.
 */
class Converters {
    @TypeConverter
    fun fromList(list: List<Int>?): String {
        return list?.joinToString(separator = ",") ?: ""
    }

    @TypeConverter
    fun toList(data: String?): List<Int> {
        val text = data ?: return emptyList()
        return if (text.isBlank()) emptyList() else text.split(",").mapNotNull { it.toIntOrNull() }
    }
}
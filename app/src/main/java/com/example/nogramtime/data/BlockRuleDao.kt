package com.example.nogramtime.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for [BlockRule]. Provides both reactive streams and
 * traditional suspend functions for reading and modifying rules. This DAO is
 * accessed from the repository and view models.
 */
@Dao
interface BlockRuleDao {
    @Query("SELECT * FROM BlockRule")
    fun getRulesFlow(): Flow<List<BlockRule>>

    @Query("SELECT * FROM BlockRule")
    suspend fun getRules(): List<BlockRule>

    @Query("SELECT COUNT(*) FROM BlockRule")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rule: BlockRule): Long

    @Update
    suspend fun update(rule: BlockRule)

    @Delete
    suspend fun delete(rule: BlockRule)
}
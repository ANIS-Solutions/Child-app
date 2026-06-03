package com.anis.child.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ScreenTimeConfigDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(config: ScreenTimeConfigEntity)

    @Query("SELECT * FROM screen_time_config WHERE id = 1")
    suspend fun getConfig(): ScreenTimeConfigEntity?

    @Query("UPDATE screen_time_config SET extraTimeEarnedMinutes = :minutes WHERE id = 1")
    suspend fun addExtraTime(minutes: Int)

    @Query("UPDATE screen_time_config SET temporaryRestrictionUntil = :until WHERE id = 1")
    suspend fun setTemporaryRestriction(until: Long?)
}

package com.example.rma_premiere.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.rma_premiere.data.local.entity.QuizResultEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizDao {

    @Insert
    suspend fun insertQuizResult(result: QuizResultEntity)

    @Insert
    suspend fun insertQuizResults(results: List<QuizResultEntity>)

    @Query("SELECT * FROM quiz_results ORDER BY playedAt DESC")
    fun getAllResults(): Flow<List<QuizResultEntity>>

    @Query("SELECT MAX(score) FROM quiz_results WHERE category = :category")
    fun getBestScore(category: Int = 1): Flow<Float?>

    @Query("SELECT COUNT(*) FROM quiz_results WHERE category = :category")
    fun getTotalPlays(category: Int = 1): Flow<Int>

    @Query("DELETE FROM quiz_results")
    suspend fun deleteAllResults()
}

package com.liferise.app.data.repository

import com.liferise.app.data.local.dao.DiaryDao
import com.liferise.app.data.local.entity.DiaryEntryEntity
import com.liferise.app.domain.model.DiaryEntry
import com.liferise.app.domain.model.Mood
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface DiaryRepository {
    fun getAllEntries(): Flow<List<DiaryEntry>>
    fun searchEntries(query: String): Flow<List<DiaryEntry>>
    suspend fun getEntryById(id: Long): DiaryEntry?
    suspend fun createEntry(entry: DiaryEntry): Long
    suspend fun updateEntry(entry: DiaryEntry)
    suspend fun deleteEntry(id: Long)
    suspend fun getEntriesCount(startMillis: Long, endMillis: Long): Int
    suspend fun getTotalEntriesCount(): Int
}

class DiaryRepositoryImpl(
    private val diaryDao: DiaryDao
) : DiaryRepository {

    override fun getAllEntries(): Flow<List<DiaryEntry>> {
        return diaryDao.getAllEntriesFlow().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun searchEntries(query: String): Flow<List<DiaryEntry>> {
        return diaryDao.searchEntriesFlow(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getEntryById(id: Long): DiaryEntry? {
        return diaryDao.getEntryById(id)?.toDomain()
    }

    override suspend fun createEntry(entry: DiaryEntry): Long {
        return diaryDao.insertEntry(entry.toEntity())
    }

    override suspend fun updateEntry(entry: DiaryEntry) {
        diaryDao.updateEntry(entry.toEntity())
    }

    override suspend fun deleteEntry(id: Long) {
        diaryDao.deleteEntryById(id)
    }

    override suspend fun getEntriesCount(startMillis: Long, endMillis: Long): Int {
        return diaryDao.getEntriesCount(startMillis, endMillis)
    }

    override suspend fun getTotalEntriesCount(): Int {
        return diaryDao.getTotalEntriesCount()
    }
}

fun DiaryEntryEntity.toDomain() = DiaryEntry(
    id = id,
    title = title,
    content = content,
    mood = mood,
    tags = tags,
    imageUris = imageUris,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun DiaryEntry.toEntity() = DiaryEntryEntity(
    id = id,
    title = title,
    content = content,
    mood = mood,
    tags = tags,
    imageUris = imageUris,
    createdAt = createdAt,
    updatedAt = updatedAt
)

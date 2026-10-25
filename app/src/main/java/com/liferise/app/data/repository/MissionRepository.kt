package com.liferise.app.data.repository

import com.liferise.app.data.local.dao.MissionDao
import com.liferise.app.data.local.entity.DailyMissionEntity
import com.liferise.app.domain.model.DailyMission
import com.liferise.app.domain.model.MissionCategory
import com.liferise.app.missions.generator.MissionGenerator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface MissionRepository {
    fun getMissionsForDateFlow(dateIso: String): Flow<List<DailyMission>>
    suspend fun getOrGenerateMissions(dateIso: String): List<DailyMission>
    suspend fun toggleMissionCompleted(missionId: Long, isCompleted: Boolean)
    suspend fun replaceMission(missionId: Long, dateIso: String): DailyMission?
}

class MissionRepositoryImpl(
    private val missionDao: MissionDao
) : MissionRepository {

    override fun getMissionsForDateFlow(dateIso: String): Flow<List<DailyMission>> {
        return missionDao.getMissionsForDateFlow(dateIso).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getOrGenerateMissions(dateIso: String): List<DailyMission> {
        val existing = missionDao.getMissionsForDate(dateIso)
        if (existing.isNotEmpty()) {
            return existing.map { it.toDomain() }
        }

        val generated = MissionGenerator.generateDefaultMissionsForDate(dateIso)
        missionDao.insertMissions(generated.map { it.toEntity() })
        return missionDao.getMissionsForDate(dateIso).map { it.toDomain() }
    }

    override suspend fun toggleMissionCompleted(missionId: Long, isCompleted: Boolean) {
        val completedAt = if (isCompleted) System.currentTimeMillis() else null
        missionDao.updateMissionCompletion(missionId, isCompleted, completedAt)
    }

    override suspend fun replaceMission(missionId: Long, dateIso: String): DailyMission? {
        val existing = missionDao.getMissionsForDate(dateIso)
        val target = existing.find { it.id == missionId } ?: return null

        val replacement = MissionGenerator.getReplacementMission(
            dateIso = dateIso,
            existingTitles = existing.map { it.title }.toSet(),
            category = target.category
        )

        val updatedEntity = target.copy(
            title = replacement.title,
            description = replacement.description,
            isCompleted = false,
            completedAt = null
        )
        missionDao.updateMission(updatedEntity)
        return updatedEntity.toDomain()
    }
}

fun DailyMissionEntity.toDomain() = DailyMission(
    id = id,
    dateIso = dateIso,
    title = title,
    description = description,
    category = category,
    isCompleted = isCompleted,
    targetPoints = targetPoints,
    completedAt = completedAt
)

fun DailyMission.toEntity() = DailyMissionEntity(
    id = id,
    dateIso = dateIso,
    title = title,
    description = description,
    category = category,
    isCompleted = isCompleted,
    targetPoints = targetPoints,
    completedAt = completedAt
)

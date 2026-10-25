package com.liferise.app.settings

import android.content.Context
import com.liferise.app.domain.model.DiaryEntry
import com.liferise.app.domain.model.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

object ExportImportManager {

    suspend fun exportDataToJson(
        context: Context,
        diaryEntries: List<DiaryEntry>,
        tasks: List<Task>
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject()
            root.put("app", "LifeRise")
            root.put("version", "1.0.0")
            root.put("exportedAt", System.currentTimeMillis())

            val diaryArray = JSONArray()
            for (entry in diaryEntries) {
                val obj = JSONObject().apply {
                    put("id", entry.id)
                    put("title", entry.title)
                    put("content", entry.content)
                    put("mood", entry.mood.name)
                    put("tags", JSONArray(entry.tags))
                    put("createdAt", entry.createdAt)
                    put("updatedAt", entry.updatedAt)
                }
                diaryArray.put(obj)
            }
            root.put("diaryEntries", diaryArray)

            val tasksArray = JSONArray()
            for (task in tasks) {
                val obj = JSONObject().apply {
                    put("id", task.id)
                    put("title", task.title)
                    put("description", task.description)
                    put("dueDate", task.dueDate)
                    put("isCompleted", task.isCompleted)
                    put("priority", task.priority.name)
                    put("recurrence", task.recurrence.name)
                }
                tasksArray.put(obj)
            }
            root.put("tasks", tasksArray)

            val exportFile = File(context.cacheDir, "LifeRise_Backup_${System.currentTimeMillis()}.json")
            FileOutputStream(exportFile).use { fos ->
                fos.write(root.toString(2).toByteArray(Charsets.UTF_8))
                fos.flush()
            }

            Result.success(exportFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

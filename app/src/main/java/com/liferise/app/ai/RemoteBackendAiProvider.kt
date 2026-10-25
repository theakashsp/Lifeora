package com.liferise.app.ai

import com.liferise.app.BuildConfig
import com.liferise.app.core.util.Resource
import com.liferise.app.domain.model.DiaryAnalysis
import com.liferise.app.domain.model.DiaryEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * Communicates with a secure external backend AI proxy.
 * Secret keys (e.g. OpenAI / Gemini API keys) reside strictly on the server, NEVER in the Android client.
 */
class RemoteBackendAiProvider(
    private val endpointUrl: String = BuildConfig.AI_ENDPOINT_URL
) : AiAnalysisProvider {

    override val providerName: String = "LifeRise Secure Cloud AI"
    override val isDemoMode: Boolean = endpointUrl.isBlank()

    override suspend fun analyzeEntry(entry: DiaryEntry): Resource<DiaryAnalysis> = withContext(Dispatchers.IO) {
        if (endpointUrl.isBlank()) {
            return@withContext Resource.Error("Remote AI endpoint is not configured in local.properties (AI_ENDPOINT_URL). Use local rule-based mode.")
        }

        var connection: HttpURLConnection? = null
        try {
            val url = URL(endpointUrl)
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                setRequestProperty("Accept", "application/json")
                connectTimeout = 12000
                readTimeout = 15000
                doOutput = true
            }

            val payload = JSONObject().apply {
                put("entryId", entry.id)
                put("title", entry.title)
                put("content", entry.content)
                put("mood", entry.mood.name)
                put("timestamp", entry.createdAt)
            }

            OutputStreamWriter(connection.outputStream, "UTF-8").use { writer ->
                writer.write(payload.toString())
                writer.flush()
            }

            val responseCode = connection.responseCode
            if (responseCode in 200..299) {
                val responseText = connection.inputStream.bufferedReader().use(BufferedReader::readText)
                val json = JSONObject(responseText)

                fun jsonArrayToStringList(arr: JSONArray?): List<String> {
                    if (arr == null) return emptyList()
                    return (0 until arr.length()).map { arr.getString(it) }
                }

                val analysis = DiaryAnalysis(
                    id = 0,
                    entryId = entry.id,
                    summary = json.optString("summary", "Analysis completed."),
                    achievements = jsonArrayToStringList(json.optJSONArray("achievements")),
                    difficulties = jsonArrayToStringList(json.optJSONArray("difficulties")),
                    lessonsLearned = jsonArrayToStringList(json.optJSONArray("lessonsLearned")),
                    suggestedImprovements = jsonArrayToStringList(json.optJSONArray("suggestedImprovements")),
                    actionForTomorrow = json.optString("actionForTomorrow", "Plan tomorrow's priority task."),
                    isDemoRuleBased = false,
                    analyzedAt = System.currentTimeMillis()
                )
                Resource.Success(analysis)
            } else {
                val errorStream = connection.errorStream?.bufferedReader()?.use(BufferedReader::readText) ?: "HTTP $responseCode"
                Resource.Error("Backend AI server error ($responseCode): $errorStream")
            }
        } catch (e: Exception) {
            Resource.Error("Network error connecting to AI backend: ${e.localizedMessage ?: "Unknown error"}", e)
        } finally {
            connection?.disconnect()
        }
    }
}

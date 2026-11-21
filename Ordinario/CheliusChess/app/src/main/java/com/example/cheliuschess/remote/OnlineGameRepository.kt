package com.example.cheliuschess.remote

import com.google.gson.Gson
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

data class UserProfileData(
    val id: String = "",
    val username: String = "",
    val rating: Int = 1200
)

data class OnlineGameData(
    val id: String? = null,
    val white_player_id: String? = null,
    val black_player_id: String? = null,
    val status: String = "WAITING",
    val turn: String = "WHITE",
    val current_fen: String = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
    val last_move_san: String? = null,
    val time_config_minutes: Int = 10,
    val time_config_increment: Int = 0,
    val white_time_left_ms: Long = 600000,
    val black_time_left_ms: Long = 600000,
    val winner_id: String? = null,
    val end_reason: String? = null,
    val score: String = "*"
)

class OnlineGameRepository {

    private val gson = Gson()

    suspend fun signUp(emailText: String, passwordText: String, usernameText: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            SupabaseClientManager.auth.signUpWith(Email) {
                email = emailText
                password = passwordText
            }
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun signIn(emailText: String, passwordText: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            SupabaseClientManager.auth.signInWith(Email) {
                email = emailText
                password = passwordText
            }
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun signOut() = withContext(Dispatchers.IO) {
        try {
            SupabaseClientManager.auth.signOut()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getCurrentUserId(): String? {
        return SupabaseClientManager.auth.currentSessionOrNull()?.user?.id
    }

    suspend fun ensureProfileExists() = withContext(Dispatchers.IO) {
        try {
            val userId = getCurrentUserId() ?: return@withContext
            val username = SupabaseClientManager.auth.currentSessionOrNull()?.user?.email?.substringBefore("@") ?: "Jugador"
            val payload = buildJsonObject {
                put("id", userId)
                put("username", username)
            }
            SupabaseClientManager.postgrest["profiles"].upsert(payload)
        } catch (e: Exception) {
            // Ignorado si ya existe por el trigger en Supabase
        }
    }

    suspend fun getWaitingGames(): Result<List<OnlineGameData>> = withContext(Dispatchers.IO) {
        try {
            val response = SupabaseClientManager.postgrest["games"]
                .select {
                    filter {
                        eq("status", "WAITING")
                    }
                }
            val json = response.data.trim()
            if (json.isBlank() || json == "[]") {
                return@withContext Result.success(emptyList())
            }
            val list = gson.fromJson(json, Array<OnlineGameData>::class.java).toList()
            Result.success(list)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun createGame(timeMinutes: Int, timeIncrement: Int): Result<OnlineGameData> = withContext(Dispatchers.IO) {
        try {
            val currentUserId = getCurrentUserId() ?: throw IllegalStateException("Usuario no autenticado en Supabase")
            ensureProfileExists()

            val payload: JsonObject = buildJsonObject {
                put("white_player_id", currentUserId)
                put("status", "WAITING")
                put("turn", "WHITE")
                put("current_fen", "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")
                put("time_config_minutes", timeMinutes)
                put("time_config_increment", timeIncrement)
                put("white_time_left_ms", timeMinutes * 60000L)
                put("black_time_left_ms", timeMinutes * 60000L)
            }

            val response = SupabaseClientManager.postgrest["games"]
                .insert(payload) {
                    select()
                }
            val createdGame = parseSingleGameResponse(response.data)
            Result.success(createdGame)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun joinGame(gameId: String): Result<OnlineGameData> = withContext(Dispatchers.IO) {
        try {
            val currentUserId = getCurrentUserId() ?: throw IllegalStateException("Usuario no autenticado en Supabase")
            ensureProfileExists()

            val payload: JsonObject = buildJsonObject {
                put("black_player_id", currentUserId)
                put("status", "IN_PROGRESS")
            }

            val response = SupabaseClientManager.postgrest["games"]
                .update(payload) {
                    filter { eq("id", gameId) }
                    select()
                }
            val updatedGame = parseSingleGameResponse(response.data)
            Result.success(updatedGame)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun updateMove(
        gameId: String,
        newFen: String,
        lastMoveSan: String,
        nextTurn: String,
        whiteTimeLeftMs: Long,
        blackTimeLeftMs: Long
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val payload: JsonObject = buildJsonObject {
                put("current_fen", newFen)
                put("last_move_san", lastMoveSan)
                put("turn", nextTurn)
                put("white_time_left_ms", whiteTimeLeftMs)
                put("black_time_left_ms", blackTimeLeftMs)
            }
            SupabaseClientManager.postgrest["games"].update(payload) {
                filter { eq("id", gameId) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun finishGame(
        gameId: String,
        winnerId: String?,
        endReason: String,
        score: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val payload: JsonObject = buildJsonObject {
                put("status", "FINISHED")
                put("end_reason", endReason)
                put("score", score)
                if (winnerId != null) {
                    put("winner_id", winnerId)
                }
            }
            SupabaseClientManager.postgrest["games"].update(payload) {
                filter { eq("id", gameId) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun fetchGameById(gameId: String): Result<OnlineGameData> = withContext(Dispatchers.IO) {
        try {
            val response = SupabaseClientManager.postgrest["games"].select {
                filter { eq("id", gameId) }
            }
            val game = parseSingleGameResponse(response.data)
            Result.success(game)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    private fun parseSingleGameResponse(json: String): OnlineGameData {
        val trimmed = json.trim()
        if (trimmed.isBlank() || trimmed == "[]" || trimmed == "{}") {
            throw IllegalStateException("Supabase no devolvió datos de la partida. Comprueba las políticas RLS de tu proyecto.")
        }
        return if (trimmed.startsWith("[")) {
            val list = gson.fromJson(trimmed, Array<OnlineGameData>::class.java)
            if (list.isNullOrEmpty()) throw IllegalStateException("Supabase devolvió una lista vacía de partidas.")
            list.first()
        } else {
            gson.fromJson(trimmed, OnlineGameData::class.java) ?: throw IllegalStateException("Error al parsear los datos de Supabase.")
        }
    }
}

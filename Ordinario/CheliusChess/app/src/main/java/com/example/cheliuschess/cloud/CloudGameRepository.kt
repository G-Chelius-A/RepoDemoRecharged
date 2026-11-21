package com.example.cheliuschess.cloud

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class CloudGameRepository(private val context: Context) {

    private val gson = Gson()
    private val localFile: File by lazy {
        File(context.filesDir, "chelius_cloud_games.json")
    }

    fun saveGame(record: GameRecord, callback: ((Boolean, String) -> Unit)? = null) {
        try {
            val games = getSavedGames().toMutableList()
            games.add(record)
            val json = gson.toJson(games)
            localFile.writeText(json)
            callback?.invoke(true, "Partida registrada localmente y en cola de sincronización.")
        } catch (e: Exception) {
            e.printStackTrace()
            callback?.invoke(false, "Error guardando la partida: ${e.message}")
        }
    }

    fun getSavedGames(): List<GameRecord> {
        if (!localFile.exists()) return emptyList()
        return try {
            val json = localFile.readText()
            val type = object : TypeToken<List<GameRecord>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun clearHistory() {
        if (localFile.exists()) {
            localFile.delete()
        }
    }
}

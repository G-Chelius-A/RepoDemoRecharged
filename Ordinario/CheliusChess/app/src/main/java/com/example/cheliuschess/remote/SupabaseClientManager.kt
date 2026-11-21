package com.example.cheliuschess.remote

import com.example.cheliuschess.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.realtime

object SupabaseClientManager {

    // =========================================================================================
    // CREDENCIALES DE SUPABASE
    // URL del API Endpoint de Supabase y Anon Key
    // =========================================================================================
    private const val DIRECT_SUPABASE_URL: String = "https://gbxeqlliiagjicgfekpp.supabase.co"
    private const val DIRECT_SUPABASE_ANON_KEY: String = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImdieGVxbGxpaWFnamljZ2Zla3BwIiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTUxMzQ1NDgsImV4cCI6MjExMDcxMDU0OH0.eNzARnxeIGNKL-0YCUdrDWVAYcaL3BGGO9LOZ6HZ5cM"

    var supabaseUrl: String = if (DIRECT_SUPABASE_URL.isNotBlank() && !DIRECT_SUPABASE_URL.contains("your-project")) {
        DIRECT_SUPABASE_URL
    } else {
        BuildConfig.SUPABASE_URL
    }

    var supabaseAnonKey: String = if (DIRECT_SUPABASE_ANON_KEY.isNotBlank() && !DIRECT_SUPABASE_ANON_KEY.contains("your-anon")) {
        DIRECT_SUPABASE_ANON_KEY
    } else {
        BuildConfig.SUPABASE_ANON_KEY
    }

    private var clientInstance: SupabaseClient? = null

    val client: SupabaseClient
        get() {
            if (clientInstance == null) {
                clientInstance = createSupabaseClient(
                    supabaseUrl = supabaseUrl,
                    supabaseKey = supabaseAnonKey
                ) {
                    install(Auth)
                    install(Postgrest)
                    install(Realtime)
                }
            }
            return clientInstance!!
        }

    val auth get() = client.auth
    val postgrest get() = client.postgrest
    val realtime get() = client.realtime

    fun isConfigured(): Boolean {
        return supabaseUrl.isNotBlank() &&
               supabaseAnonKey.isNotBlank() &&
               !supabaseUrl.contains("your-project") &&
               !supabaseAnonKey.contains("your-anon")
    }
}

package com.example.persistenciaandroid;

public final class SupabaseConfig {
    public static final String URL = BuildConfig.SUPABASE_URL;
    public static final String ANON_KEY = BuildConfig.SUPABASE_ANON_KEY;
    public static final String TABLE = "productos";
    public static final String ID_COLUMN = "id";

    private SupabaseConfig() {
    }
}

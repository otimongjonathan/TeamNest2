package com.example.teamnest

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

object SupabaseConfig {
    private const val SUPABASE_URL = "https://gneoqekamvqplyibrano.supabase.co"
    private const val SUPABASE_KEY = "sb_publishable_pTv-dU3h8uP4z4jnbgxIYw_049SUzHl"

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        install(Storage)
        install(Postgrest)
    }
}

package com.pokedex.app

import android.content.Context
import androidx.core.content.edit

/**
 * Guarda o progresso de treino de cada Pokémon (nível, XP e registro) no aparelho,
 * para que ele não se perca ao sair da tela ou fechar o app.
 */
object TrainingStore {
    private const val PREFS_NAME = "training"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** Devolve o Pokémon com o progresso salvo; sem progresso salvo, mantém os valores do mock */
    fun load(context: Context, pokemon: Pokemon): Pokemon {
        val prefs = prefs(context)
        return pokemon.copy(
            level = prefs.getInt("${pokemon.id}_level", pokemon.level),
            experience = prefs.getInt("${pokemon.id}_experience", pokemon.experience),
            isRegistered = prefs.getBoolean("${pokemon.id}_registered", pokemon.isRegistered),
        )
    }

    fun save(context: Context, pokemon: Pokemon) {
        prefs(context).edit {
            putInt("${pokemon.id}_level", pokemon.level)
            putInt("${pokemon.id}_experience", pokemon.experience)
            putBoolean("${pokemon.id}_registered", pokemon.isRegistered)
        }
    }
}

package com.pokedex.app

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.google.android.material.snackbar.Snackbar
import com.pokedex.app.databinding.ActivityPokemonDetailBinding
import com.pokedex.app.databinding.BadgeLayoutBinding
import com.pokedex.app.databinding.StatRowBinding
import kotlin.random.Random

class PokemonDetailActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_POKEMON_ID = "pokemon_id"

        /** Faixa de XP ganha a cada treino */
        private const val MIN_TRAINING_EXPERIENCE = 10
        private const val MAX_TRAINING_EXPERIENCE = 30
    }

    private lateinit var binding: ActivityPokemonDetailBinding
    private lateinit var pokemon: Pokemon

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityPokemonDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.pokemonDetailToolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val pokemonId = intent.getStringExtra(EXTRA_POKEMON_ID)
        Log.d("PokemonDetailActivity", "Pokémon ID: $pokemonId")

        // O extra pode chegar nulo ou com um id inexistente: nesse caso a tela é encerrada
        val selectedPokemon = findPokemonById(pokemonId)
        if (selectedPokemon == null) {
            Toast.makeText(this, R.string.pokemon_not_found, Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        pokemon = TrainingStore.load(this, selectedPokemon)

        bindPokemonInfo()
        updateTrainingViews(animate = false)

        binding.trainButton.setOnClickListener { train() }
        binding.rareCandyButton.setOnClickListener { useRareCandy() }
        binding.evolveButton.setOnClickListener { evolve() }

        binding.registerButton.setOnClickListener {
            pokemon = pokemon.toggleRegistered()
            TrainingStore.save(this, pokemon)
            updateTrainingViews(animate = false)
        }

        // Afasta o conteúdo da barra de status e da barra de navegação,
        // para que ele não passe por baixo delas ao rolar a tela
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun bindPokemonInfo() {
        binding.sprite.setImageResource(pokemon.imageRes)
        binding.pokemonName.text = pokemon.name
        binding.pokemonNumber.text = getString(R.string.pokemon_number, pokemon.number)
        binding.descriptionTextView.text = pokemon.description
        binding.categoryTextView.text = pokemon.category
        binding.heightTextView.text = getString(R.string.height_format, pokemon.heightInMeters)
        binding.weightTextView.text = getString(R.string.weight_format, pokemon.weightInKilograms)
        binding.abilityTextView.text = pokemon.ability
        binding.weaknessesTextView.text = pokemon.weaknesses.joinToString(", ")

        // Campos opcionais: quando não existem, a tela mostra um traço no lugar
        binding.hiddenAbilityTextView.text = pokemon.hiddenAbility ?: getString(R.string.not_available)
        binding.evolvesToTextView.text = pokemon.evolvesTo ?: getString(R.string.not_available)

        // Componente XML reutilizável (badge_layout) inflado para cada tipo e fraqueza
        binding.badgesRow1.removeAllViews()
        binding.badgesRow2.removeAllViews()
        pokemon.types.forEach { type ->
            addBadgeView(binding.badgesRow1, type)
        }

        pokemon.weaknesses.take(3).forEach { weakness ->
            addBadgeView(binding.badgesRow2, weakness)
        }
    }

    private fun addBadgeView(atRow: LinearLayout, text: String) {
        val badgeViewBinding = BadgeLayoutBinding.inflate(layoutInflater, atRow, false)
        badgeViewBinding.badgeText.text = text
        badgeViewBinding.root.tintByType(text)
        atRow.addView(badgeViewBinding.root)
    }

    private fun train() {
        if (pokemon.isMaxLevel) {
            showMessage(getString(R.string.max_level_message, pokemon.name))
            return
        }
        val gainedExperience = Random.nextInt(MIN_TRAINING_EXPERIENCE, MAX_TRAINING_EXPERIENCE + 1)
        onProgressChanged(pokemon.train(gainedExperience), getString(R.string.experience_gained, gainedExperience))
    }

    private fun useRareCandy() {
        val nextLevel = pokemon.getNextLevel()
        if (nextLevel == null) {
            showMessage(getString(R.string.max_level_message, pokemon.name))
            return
        }
        onProgressChanged(nextLevel, null)
    }

    /**
     * Aplica o novo progresso, salva e avisa quando o Pokémon sobe de nível.
     * Sem subir de nível, mostra [messageWithoutLevelUp] (ex.: o XP ganho), se houver.
     */
    private fun onProgressChanged(updated: Pokemon, messageWithoutLevelUp: String?) {
        val leveledUp = updated.level > pokemon.level
        pokemon = updated
        TrainingStore.save(this, pokemon)
        updateTrainingViews(animate = true)

        if (leveledUp) {
            showMessage(getString(R.string.level_up_message, pokemon.name, pokemon.level))
            bounce(binding.levelTextView)
        } else if (messageWithoutLevelUp != null) {
            showMessage(messageWithoutLevelUp)
        }
    }

    /**
     * Transforma o Pokémon na sua evolução, que herda o nível e o XP e já fica registrada.
     */
    private fun evolve() {
        val evolution = findPokemonByName(pokemon.evolvesTo) ?: return
        val previousName = pokemon.name
        val evolved = evolution.copy(
            level = pokemon.level,
            experience = pokemon.experience,
            isRegistered = true,
        )
        TrainingStore.save(this, evolved)
        intent.putExtra(EXTRA_POKEMON_ID, evolved.id)

        // Pisca a imagem: some, troca para a evolução e volta
        binding.sprite.animate().alpha(0f).scaleX(0.6f).scaleY(0.6f).setDuration(250).withEndAction {
            pokemon = evolved
            bindPokemonInfo()
            updateTrainingViews(animate = false)
            binding.sprite.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(350).start()
            showMessage(getString(R.string.evolved_message, previousName, evolved.name))
        }.start()
    }

    private fun updateTrainingViews(animate: Boolean) {
        binding.levelTextView.text = getString(R.string.level, pokemon.level)
        binding.experienceTextView.text = if (pokemon.isMaxLevel) {
            getString(R.string.experience_max)
        } else {
            getString(R.string.experience, pokemon.experience, pokemon.experienceToNextLevel)
        }
        binding.experienceIndicator.setProgressCompat(pokemon.experiencePercentage, animate)

        val stats = pokemon.stats
        bindStat(binding.statHp, R.string.stat_hp, stats.hp, animate)
        bindStat(binding.statAttack, R.string.stat_attack, stats.attack, animate)
        bindStat(binding.statDefense, R.string.stat_defense, stats.defense, animate)
        bindStat(binding.statSpeed, R.string.stat_speed, stats.speed, animate)

        updateEvolutionViews()

        binding.registerStatusTextView.setText(
            if (pokemon.isRegistered) R.string.registered_status else R.string.unregistered_status
        )
        binding.registerButton.setText(
            if (pokemon.isRegistered) R.string.unregister_action else R.string.register_action
        )
    }

    private fun updateEvolutionViews() {
        val evolvesTo = pokemon.evolvesTo
        val evolutionLevel = pokemon.evolutionLevel
        val evolution = findPokemonByName(evolvesTo)

        val readyToEvolve = pokemon.canEvolve && evolution != null
        binding.evolveButton.isVisible = readyToEvolve
        binding.evolutionTextView.isVisible = !readyToEvolve

        if (readyToEvolve) {
            binding.evolveButton.text = getString(R.string.evolve_action, evolvesTo)
            return
        }
        binding.evolutionTextView.text = when {
            evolvesTo == null -> getString(R.string.evolution_final)
            evolutionLevel == null -> getString(R.string.evolution_other_way, evolvesTo)
            pokemon.canEvolve -> getString(R.string.evolution_missing, evolvesTo)
            else -> getString(R.string.evolution_hint, evolvesTo, evolutionLevel)
        }
    }

    private fun bindStat(row: StatRowBinding, @StringRes label: Int, value: Int, animate: Boolean) {
        row.statLabel.setText(label)
        row.statValue.text = value.toString()
        row.statBar.setProgressCompat(value, animate)
    }

    private fun bounce(view: View) {
        // Cresce a partir do canto esquerdo, onde o texto começa
        view.pivotX = 0f
        view.pivotY = view.height / 2f
        view.animate().scaleX(1.3f).scaleY(1.3f).setDuration(150).withEndAction {
            view.animate().scaleX(1f).scaleY(1f).setDuration(150).start()
        }.start()
    }

    private fun showMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }
}

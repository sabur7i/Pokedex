package com.pokedex.app

import android.content.res.ColorStateList
import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat

/**
 * Modelo imutável que representa um Pokémon da Pokédex.
 *
 * Os campos [hiddenAbility] e [evolvesTo] são opcionais (nullable) porque nem todo
 * Pokémon possui habilidade oculta ou uma próxima evolução.
 */
data class Pokemon(
    val id: String,
    val number: Int,
    val name: String,
    @DrawableRes val imageRes: Int,
    val category: String,
    val types: List<String>,
    val description: String,
    val heightInMeters: Double,
    val weightInKilograms: Double,
    val ability: String,
    val hiddenAbility: String?,
    val evolvesTo: String?,
    val weaknesses: List<String>,
    val baseStats: Stats,
    /** Nível em que evolui para [evolvesTo]; `null` quando evolui de outro jeito (pedra, troca...) */
    val evolutionLevel: Int? = null,
    val level: Int = 5,
    val maxLevel: Int = 100,
    /** XP acumulado dentro do nível atual (volta a zero a cada nível) */
    val experience: Int = 0,
    val isRegistered: Boolean = false,
) {
    init {
        require(number > 0) { "number must be greater than 0" }
        require(types.isNotEmpty()) { "types must not be empty" }
        require(maxLevel > 0) { "maxLevel must be greater than 0" }
        require(level in 1..maxLevel) { "level must be between 1 and maxLevel" }
        require(experience >= 0) { "experience must not be negative" }
    }

    val mainType: String
        get() = types.first()

    val isMaxLevel: Boolean
        get() = level == maxLevel

    /** XP necessário para sair do nível atual: cada nível pede um pouco mais que o anterior */
    val experienceToNextLevel: Int
        get() = experienceNeeded(level)

    val experiencePercentage: Int
        get() = if (isMaxLevel) 100 else experience * 100 / experienceToNextLevel

    /** Status calculados a partir dos status base e do nível atual */
    val stats: Stats
        get() = baseStats.atLevel(level)

    val canEvolve: Boolean
        get() = evolvesTo != null && evolutionLevel != null && level >= evolutionLevel

    fun getPreviousLevel(): Pokemon? {
        return try {
            copy(level = level - 1, experience = 0)
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    fun getNextLevel(): Pokemon? {
        return try {
            copy(level = level + 1, experience = 0)
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    /**
     * Soma [gainedExperience] ao XP atual, subindo quantos níveis forem possíveis.
     * No nível máximo o XP fica zerado.
     */
    fun train(gainedExperience: Int): Pokemon {
        var newLevel = level
        var newExperience = experience + gainedExperience
        while (newLevel < maxLevel && newExperience >= experienceNeeded(newLevel)) {
            newExperience -= experienceNeeded(newLevel)
            newLevel++
        }
        if (newLevel == maxLevel) newExperience = 0
        return copy(level = newLevel, experience = newExperience)
    }

    fun toggleRegistered(): Pokemon = copy(isRegistered = !isRegistered)

    private fun experienceNeeded(atLevel: Int): Int = 20 + atLevel * 5
}

/**
 * Status de batalha. Em [Pokemon.baseStats] guarda os status base da espécie (valores dos jogos);
 * [atLevel] calcula os status reais para um nível.
 */
data class Stats(
    val hp: Int,
    val attack: Int,
    val defense: Int,
    val speed: Int,
) {
    /** Fórmula dos jogos simplificada (sem IVs, EVs e natureza) */
    fun atLevel(level: Int): Stats = Stats(
        hp = 2 * hp * level / 100 + level + 10,
        attack = 2 * attack * level / 100 + 5,
        defense = 2 * defense * level / 100 + 5,
        speed = 2 * speed * level / 100 + 5,
    )
}

/**
 * Cor do badge de um tipo (usada tanto para os tipos quanto para as fraquezas).
 */
@ColorRes
fun typeColorRes(type: String): Int = when (type) {
    "Fogo" -> R.color.type_fire
    "Água" -> R.color.type_water
    "Grama" -> R.color.type_grass
    "Elétrico" -> R.color.type_electric
    "Venenoso" -> R.color.type_poison
    "Dragão" -> R.color.type_dragon
    "Psíquico" -> R.color.type_psychic
    "Fantasma" -> R.color.type_ghost
    "Voador" -> R.color.type_flying
    "Gelo" -> R.color.type_ice
    "Pedra" -> R.color.type_rock
    "Terrestre" -> R.color.type_ground
    "Fada" -> R.color.type_fairy
    "Lutador" -> R.color.type_fighting
    "Inseto" -> R.color.type_bug
    "Sombrio" -> R.color.type_dark
    else -> R.color.type_normal
}

/**
 * Pinta o fundo da view (ex.: um badge) com a cor do tipo informado.
 */
fun View.tintByType(type: String) {
    backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, typeColorRes(type)))
}

/**
 * Dados simulados (mocks) usados nesta etapa do projeto, no lugar de uma API ou banco de dados.
 */
val pokedexItems: List<Pokemon> = listOf(
    Pokemon(
        id = "bulbasaur",
        number = 1,
        name = "Bulbasaur",
        imageRes = R.drawable.pokemon_bulbasaur,
        category = "Semente",
        types = listOf("Grama", "Venenoso"),
        description = "Carrega uma semente nas costas desde o nascimento. A semente cresce junto com ele e se alimenta da luz do sol.",
        heightInMeters = 0.7,
        weightInKilograms = 6.9,
        ability = "Overgrow",
        hiddenAbility = "Chlorophyll",
        evolvesTo = "Ivysaur",
        weaknesses = listOf("Fogo", "Gelo", "Voador", "Psíquico"),
        baseStats = Stats(hp = 45, attack = 49, defense = 49, speed = 45),
        evolutionLevel = 16,
    ),
    Pokemon(
        id = "charmander",
        number = 4,
        name = "Charmander",
        imageRes = R.drawable.pokemon_charmander,
        category = "Lagarto",
        types = listOf("Fogo"),
        description = "A chama na ponta da cauda indica o seu estado de saúde: quanto mais forte o fogo, melhor ele está.",
        heightInMeters = 0.6,
        weightInKilograms = 8.5,
        ability = "Blaze",
        hiddenAbility = "Solar Power",
        evolvesTo = "Charmeleon",
        weaknesses = listOf("Água", "Pedra", "Terrestre"),
        baseStats = Stats(hp = 39, attack = 52, defense = 43, speed = 65),
        evolutionLevel = 16,
    ),
    Pokemon(
        id = "charizard",
        number = 6,
        name = "Charizard",
        imageRes = R.drawable.pokemon_charizard,
        category = "Chama",
        types = listOf("Fogo", "Voador"),
        description = "Voa em grandes altitudes à procura de oponentes fortes. O sopro de fogo é quente o bastante para derreter rochas.",
        heightInMeters = 1.7,
        weightInKilograms = 90.5,
        ability = "Blaze",
        hiddenAbility = "Solar Power",
        evolvesTo = null,
        weaknesses = listOf("Água", "Elétrico", "Pedra"),
        baseStats = Stats(hp = 78, attack = 84, defense = 78, speed = 100),
        level = 36,
    ),
    Pokemon(
        id = "squirtle",
        number = 7,
        name = "Squirtle",
        imageRes = R.drawable.pokemon_squirtle,
        category = "Tartaruguinha",
        types = listOf("Água"),
        description = "Usa o casco para se proteger e se esconde dentro dele quando se sente ameaçado, disparando jatos de água.",
        heightInMeters = 0.5,
        weightInKilograms = 9.0,
        ability = "Torrent",
        hiddenAbility = "Rain Dish",
        evolvesTo = "Wartortle",
        weaknesses = listOf("Grama", "Elétrico"),
        baseStats = Stats(hp = 44, attack = 48, defense = 65, speed = 43),
        evolutionLevel = 16,
    ),
    Pokemon(
        id = "pikachu",
        number = 25,
        name = "Pikachu",
        imageRes = R.drawable.pokemon_pikachu,
        category = "Rato",
        types = listOf("Elétrico"),
        description = "Armazena eletricidade nas bochechas e a libera quando se sente ameaçado ou quando quer chamar atenção.",
        heightInMeters = 0.4,
        weightInKilograms = 6.0,
        ability = "Static",
        hiddenAbility = "Lightning Rod",
        evolvesTo = "Raichu",
        weaknesses = listOf("Terrestre"),
        baseStats = Stats(hp = 35, attack = 55, defense = 40, speed = 90),
        level = 12,
    ),
    Pokemon(
        id = "gengar",
        number = 94,
        name = "Gengar",
        imageRes = R.drawable.pokemon_gengar,
        category = "Sombra",
        types = listOf("Fantasma", "Venenoso"),
        description = "Esconde-se nas sombras e costuma aparecer em lugares escuros, onde prega peças em quem passa por perto.",
        heightInMeters = 1.5,
        weightInKilograms = 40.5,
        ability = "Cursed Body",
        hiddenAbility = null,
        evolvesTo = null,
        weaknesses = listOf("Psíquico", "Fantasma", "Sombrio", "Terrestre"),
        baseStats = Stats(hp = 60, attack = 65, defense = 60, speed = 110),
    ),
    Pokemon(
        id = "dratini",
        number = 147,
        name = "Dratini",
        imageRes = R.drawable.pokemon_dratini,
        category = "Dragão",
        types = listOf("Dragão"),
        description = "Vive em rios e lagos profundos. Troca de pele com frequência, porque cresce rápido demais para o próprio corpo.",
        heightInMeters = 1.8,
        weightInKilograms = 3.3,
        ability = "Shed Skin",
        hiddenAbility = "Marvel Scale",
        evolvesTo = "Dragonair",
        weaknesses = listOf("Gelo", "Dragão", "Fada"),
        baseStats = Stats(hp = 41, attack = 64, defense = 45, speed = 50),
        evolutionLevel = 30,
    ),
    Pokemon(
        id = "dragonair",
        number = 148,
        name = "Dragonair",
        imageRes = R.drawable.pokemon_dragonair,
        category = "Dragão",
        types = listOf("Dragão"),
        description = "Diz-se que consegue mudar o clima ao seu redor com a energia guardada nas esferas de cristal do seu corpo.",
        heightInMeters = 4.0,
        weightInKilograms = 16.5,
        ability = "Shed Skin",
        hiddenAbility = "Marvel Scale",
        evolvesTo = "Dragonite",
        weaknesses = listOf("Gelo", "Dragão", "Fada"),
        baseStats = Stats(hp = 61, attack = 84, defense = 65, speed = 70),
        evolutionLevel = 55,
        level = 40,
    ),
    Pokemon(
        id = "dragonite",
        number = 149,
        name = "Dragonite",
        imageRes = R.drawable.pokemon_dragonite,
        category = "Dragão",
        types = listOf("Dragão", "Voador"),
        description = "Apesar do tamanho, é gentil e voa por oceanos inteiros para guiar embarcações perdidas até a costa.",
        heightInMeters = 2.2,
        weightInKilograms = 210.0,
        ability = "Inner Focus",
        hiddenAbility = "Multiscale",
        evolvesTo = null,
        weaknesses = listOf("Gelo", "Pedra", "Dragão", "Fada"),
        baseStats = Stats(hp = 91, attack = 134, defense = 95, speed = 80),
        level = 55,
    ),
    Pokemon(
        id = "snorlax",
        number = 143,
        name = "Snorlax",
        imageRes = R.drawable.pokemon_snorlax,
        category = "Sonolento",
        types = listOf("Normal"),
        description = "Passa quase o dia inteiro dormindo e só acorda para comer. Nada além de fome costuma tirá-lo do lugar.",
        heightInMeters = 2.1,
        weightInKilograms = 460.0,
        ability = "Immunity",
        hiddenAbility = "Gluttony",
        evolvesTo = null,
        weaknesses = listOf("Lutador"),
        baseStats = Stats(hp = 160, attack = 110, defense = 65, speed = 30),
    ),
    Pokemon(
        id = "mewtwo",
        number = 150,
        name = "Mewtwo",
        imageRes = R.drawable.pokemon_mewtwo,
        category = "Genético",
        types = listOf("Psíquico"),
        description = "Criado a partir de longos experimentos genéticos, possui um poder psíquico fora do comum.",
        heightInMeters = 2.0,
        weightInKilograms = 122.0,
        ability = "Pressure",
        hiddenAbility = "Unnerve",
        evolvesTo = null,
        weaknesses = listOf("Inseto", "Fantasma", "Sombrio"),
        baseStats = Stats(hp = 106, attack = 110, defense = 90, speed = 130),
        level = 70,
    ),
    Pokemon(
        id = "eevee",
        number = 133,
        name = "Eevee",
        imageRes = R.drawable.pokemon_eevee,
        category = "Evolução",
        types = listOf("Normal"),
        description = "Possui uma estrutura genética instável, o que permite evoluir para formas bem diferentes entre si.",
        heightInMeters = 0.3,
        weightInKilograms = 6.5,
        ability = "Adaptability",
        hiddenAbility = "Anticipation",
        evolvesTo = "Vaporeon, Jolteon ou Flareon",
        weaknesses = listOf("Lutador"),
        baseStats = Stats(hp = 55, attack = 55, defense = 50, speed = 55),
    ),
)

/**
 * Busca um Pokémon pelo id recebido via [android.content.Intent].
 * Retorna `null` quando o id não existe na lista de mocks.
 */
fun findPokemonById(id: String?): Pokemon? = pokedexItems.firstOrNull { it.id == id }

/**
 * Busca um Pokémon pelo nome (usado para achar a evolução a partir de [Pokemon.evolvesTo]).
 * Retorna `null` quando a evolução ainda não faz parte da Pokédex.
 */
fun findPokemonByName(name: String?): Pokemon? = pokedexItems.firstOrNull { it.name == name }

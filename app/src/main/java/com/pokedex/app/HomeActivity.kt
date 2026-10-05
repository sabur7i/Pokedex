package com.pokedex.app

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pokedex.app.databinding.ActivityHomeBinding
import com.pokedex.app.databinding.HomeListItemLayoutBinding

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private val adapter = HomeListAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityHomeBinding.inflate(layoutInflater)

        setContentView(binding.root)

        binding.listView.adapter = adapter
        binding.listView.layoutManager = LinearLayoutManager(this)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // Recarrega o progresso ao voltar da tela de detalhe, para a lista mostrar o nível atualizado
    override fun onResume() {
        super.onResume()
        adapter.submitList(pokedexItems.map { TrainingStore.load(this, it) })
    }
}

class HomeListAdapter : RecyclerView.Adapter<HomeListAdapter.ViewHolder>() {
    private var list: List<Pokemon> = emptyList()

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newList: List<Pokemon>) {
        list = newList
        notifyDataSetChanged()
    }

    class ViewHolder(val binding: HomeListItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val context: Context = binding.root.context
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding =
            HomeListItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val pokemon = list[position]
        holder.binding.name.text = pokemon.name
        holder.binding.number.text =
            holder.context.getString(R.string.pokemon_number, pokemon.number)
        holder.binding.level.text = holder.context.getString(R.string.level, pokemon.level)
        holder.binding.category.text = pokemon.category
        holder.binding.badge.badgeText.text = pokemon.mainType
        holder.binding.badge.root.tintByType(pokemon.mainType)
        holder.binding.sprite.setImageResource(pokemon.imageRes)

        holder.binding.root.setOnClickListener {
            val intent = Intent(holder.context, PokemonDetailActivity::class.java)
            intent.putExtra(PokemonDetailActivity.EXTRA_POKEMON_ID, pokemon.id)
            holder.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = list.size
}

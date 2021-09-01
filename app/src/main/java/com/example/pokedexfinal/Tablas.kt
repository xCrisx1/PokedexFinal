package com.example.pokedexfinal

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Pokemons")

data class PokemonTable(
    @PrimaryKey val ID:Int,
    @NonNull val Nombre:String,
    val ImagenUrl:String,
    //los agrego como tipo1 y tipo2, ya que los tipos no tendran ninguna funcion especial, solo es estetico
    val Tipo1:String,
    val Tipo2:String
)
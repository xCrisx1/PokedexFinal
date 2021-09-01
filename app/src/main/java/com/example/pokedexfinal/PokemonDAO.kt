package com.example.pokedexfinal

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PokemonDAO {
    @Insert
    fun agregar(p:PokemonTable)

    @Query("SELECT * FROM Pokemons WHERE ID=:id")
    fun buscarporID(id:String):PokemonTable

    @Query("SELECT * FROM Pokemons")
    fun buscarTodos():List<PokemonTable>

    @Delete
    fun eliminar(p:PokemonTable)
}
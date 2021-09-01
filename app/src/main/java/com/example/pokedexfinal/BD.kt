package com.example.pokedexfinal

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [PokemonTable::class],
    version = 1
)

public abstract class BD:RoomDatabase() {
    abstract fun obtenerDAO_Pokemons():PokemonDAO

    companion object{
        @Volatile
        private var objeto:BD?=null

        fun obtenerObjeto(contexto: Context):BD?{

            if(objeto==null){
                synchronized(this){
                    //Room.databaseBuilder(this,BD::class.java,nombreBD).build()
                    this.objeto = Room.databaseBuilder(contexto,BD::class.java,"BDFavoritos").build()
                }
            }

            return this.objeto
        }
    }
}
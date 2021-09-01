package com.example.pokedexfinal

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface ConexionAPI {
    //las puse por generacion declaradas individual para evitar futuras confusiones
    @GET("?limit=151&offset=0")
    suspend fun getPrimeraGeneracion(): Response<Pokedex>

    @GET("?limit=100&offset=151")
    suspend fun getSegundaGeneracion(): Response<Pokedex>

    @GET("?limit=135&offset=251")
    suspend fun getTerceraGeneracion(): Response<Pokedex>

    @GET("?limit=107&offset=386")
    suspend fun getCuartaGeneracion(): Response<Pokedex>

    @GET("?limit=156&offset=493")
    suspend fun getQuintaGeneracion(): Response<Pokedex>

    @GET("?limit=72&offset=649")
    suspend fun getSextaGeneracion(): Response<Pokedex>

    @GET("?limit=88&offset=721")
    suspend fun getSeptimaGeneracion(): Response<Pokedex>

    @GET("?limit=89&offset=809")
    suspend fun getOctavaGeneracion(): Response<Pokedex>

    @GET
    suspend fun getPokemonPorID(@Url id:String):Response<DataPokemon>
}
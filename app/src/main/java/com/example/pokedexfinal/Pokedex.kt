package com.example.pokedexfinal

import com.google.gson.annotations.SerializedName

data class Pokedex(
    @SerializedName("count") var cantidad:Int,
    @SerializedName("results") var listaPokemon:ArrayList<urlPokemon>
)

data class urlPokemon(
    @SerializedName("name") var nombre:String,
    @SerializedName("url") var url:String,
)

data class DataPokemon(
    @SerializedName("id") var id:Int,
    @SerializedName("name") var nombre:String,
    @SerializedName("types") var tipos:List<Tipos>,
    @SerializedName("sprites") var Sprites:sprites
)

data class Tipos(
    @SerializedName("slot") var slot:Int,
    @SerializedName("type") var tipo:Tipo
)

data class Tipo(
    @SerializedName("name") var nombre:String
)

data class sprites(
    @SerializedName("other") var otro:other,
    @SerializedName("front_default") var imgFront:String
)

data class other(
    @SerializedName("official-artwork") var offi:officialArtWork
)

data class officialArtWork(
    @SerializedName("front_default") var img:String
)
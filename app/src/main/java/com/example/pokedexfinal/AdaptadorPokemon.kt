package com.example.pokedexfinal

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.coroutines.*
import kotlinx.coroutines.android.awaitFrame
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.Exception

public class AdaptadorPokemon:RecyclerView.Adapter<AdaptadorPokemon.ViewHolderPokemon> {
    lateinit var datos:Pokedex
    var initialPosition:Int = 1

    constructor(datos:Pokedex,initialPosition:Int){
        this.datos = datos
        this.initialPosition = initialPosition
    }

    public class ViewHolderPokemon:RecyclerView.ViewHolder{
        lateinit var foto:ImageView
        lateinit var nombre:TextView
        lateinit var info:TextView
        lateinit var tipo1:CardView
        lateinit var tipo2:CardView
        lateinit var txtTipo1:TextView
        lateinit var txtTipo2:TextView
        lateinit var btnFav:ImageView

        constructor(vista: View):super(vista){
            foto = vista.findViewById(R.id.imgFoto)
            nombre = vista.findViewById(R.id.txtNombre)
            info = vista.findViewById(R.id.txtInfo)
            tipo1 = vista.findViewById(R.id.tipo1)
            tipo2 = vista.findViewById(R.id.tipo2)
            txtTipo1 = vista.findViewById(R.id.txtTipo1)
            txtTipo2 = vista.findViewById(R.id.txtTipo2)
            btnFav = vista.findViewById(R.id.btnFav)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderPokemon {
        var v:View = LayoutInflater.from(parent.context).inflate(R.layout.plantilla,parent,false)
        return AdaptadorPokemon.ViewHolderPokemon(v)
    }

    override fun onBindViewHolder(holder: ViewHolderPokemon, position: Int) {
        var nombre:String = this.datos.listaPokemon[position].nombre
        this.datos.listaPokemon
        holder.nombre.setText(nombre)
        //holder.info.setText("Nº" + NumOrder(position + initialPosition + 1)) //antigua forma de mostrar ids
        var idDato:String = obtenerID(datos.listaPokemon[position].url) //obtenemos el id por url

        val DAOPokemons = BD.obtenerObjeto(holder.foto.context)?.obtenerDAO_Pokemons() //el context lo podemos obtener asi de facil, es un poco raro, pero mejor a mi parecer
        var eventthread = doAsync {
            var pokevas = DAOPokemons?.buscarporID(idDato) //el ID 1 puede o no estar ocupado, no podre hacerlo optimo por ahora, sin embargo hay una forma trucha xP
            uiThread{
                var valor = !pokevas?.Nombre.isNullOrEmpty()
                if(valor){ //si existe en la base de datos utilizamos esos datos
                    holder.btnFav.setBackgroundResource(R.drawable.favon)

                    if (pokevas != null) { //otra vez me obliga a poner esto -.- y ya comprobe si es null
                        //llamamos a la funcion para ordenar numeros de pokemon
                        holder.info.setText("Nº" + NumOrder(pokevas.ID))

                        val servicio = crearConexion()
                        //obtenemos la imagen
                        CoroutineScope(Dispatchers.IO).launch {
                            var request: Response<DataPokemon>
                            //si no tiene internet no cargamos la imagen ಥ_ಥ
                            try{
                                request = servicio.getPokemonPorID(idDato)
                            }catch (e:Exception){
                                return@launch
                            }

                            if(request.isSuccessful) {
                                val datos = request.body()
                                if (datos != null) {
                                    withContext(Dispatchers.Main) {
                                        Picasso.get().load(datos.Sprites.imgFront).noFade().into(holder.foto)
                                    }
                                }
                            }
                        }

                        if(!pokevas.Tipo2.isNullOrEmpty()){ //2tipos
                            establecerTipo(holder,pokevas.Tipo1,pokevas.Tipo2)
                        }else{ //1 tipo
                            establecerTipo(holder,pokevas.Tipo1,"")
                        }
                    }
                }else{
                    holder.btnFav.setBackgroundResource(R.drawable.favoff)

                    val servicio = crearConexion()
                    //comienza courutine
                    CoroutineScope(Dispatchers.IO).launch {
                        //val request=servicio.getPokemonPorID((position + initialPosition + 1).toString()) //antiguo metodo sin favoritos
                        val request=servicio.getPokemonPorID(idDato)
                        if(request.isSuccessful){
                            val datos=request.body()
                            if(datos!=null){
                                withContext(Dispatchers.Main) {
                                    //llamamos a la funcion para ordenar numeros de pokemon
                                    holder.info.setText("Nº" + NumOrder(datos.id))

                                    //activar este, y desactivar el de abajo para mejor imagen, pero es mas lento
                                    //Picasso.get().load(datos.Sprites.otro.offi.img).into(holder.foto)

                                    //se utiliza el "noFade" para quitar la animacion y cargue mas rapido
                                    Picasso.get().load(datos.Sprites.imgFront).noFade().into(holder.foto) //se utiliza para remplazar la imagen anterior debido a que el servidor es muy lento

                                    if(datos.tipos.size == 1){
                                        establecerTipo(holder,datos.tipos[0].tipo.nombre,"")
                                    }else if(datos.tipos.size == 2){
                                        establecerTipo(holder,datos.tipos[0].tipo.nombre,datos.tipos[1].tipo.nombre)
                                    }else{
                                        System.out.println("TIENE 3 O MAS TIPOS!!!!!, segun google SOLO EXISTEN 2 COMO MAXIMO en 2021!!!!!")
                                    }
                                }
                            }
                        }
                    }
                    //fin coroutine
                }

                holder.btnFav.setOnClickListener{
                    //it.setBackgroundResource(R.drawable.favon)
                    //revisamos si lo tenemos en favoritos primero :O
                    if(valor){
                        if(b.spnListaGeneracion.selectedItem == "Favoritos"){
                            if (pokevas != null) { //me obliga a poner la comprobacion otra vez!!! :c
                                borrarPokemonEnDB(holder,pokevas)
                                holder.btnFav.setBackgroundResource(R.drawable.favoff)

                                datos.listaPokemon.removeAt(holder.adapterPosition)
                                notifyItemRemoved(holder.adapterPosition)
                                notifyItemChanged(holder.adapterPosition,datos.listaPokemon.size)
                                valor = false
                            }

                        }else{
                            val servicio = crearConexion()
                            CoroutineScope(Dispatchers.IO).launch {
                                //val request=servicio.getPokemonPorID((position + initialPosition + 1).toString()) //antiguo metodo sin favoritos
                                val request = servicio.getPokemonPorID(idDato)
                                val datoss=request.body()
                                if(datoss!=null){
                                    withContext(Dispatchers.Main) {
                                        if (datoss.tipos.size == 1) {
                                            var pokemon: PokemonTable = PokemonTable(
                                                datoss.id,
                                                datoss.nombre,
                                                datoss.Sprites.otro.offi.img,
                                                datoss.tipos[0].tipo.nombre,
                                                ""
                                            )
                                            borrarPokemonEnDB(holder, pokemon)
                                        } else { //2 tipos
                                            var pokemon: PokemonTable = PokemonTable(
                                                datoss.id,
                                                datoss.nombre,
                                                datoss.Sprites.otro.offi.img,
                                                datoss.tipos[0].tipo.nombre,
                                                datoss.tipos[1].tipo.nombre
                                            )
                                            borrarPokemonEnDB(holder, pokemon)
                                        }
                                        valor = false
                                        holder.btnFav.setBackgroundResource(R.drawable.favoff)
                                    }
                                }
                            }
                        }

                        System.out.println("BORRAMOSSSSSSSSSSSSSSS!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
                    }else{
                        System.out.println("agregaaaaaaaaaaaaaaa!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
                        val servicio = crearConexion()
                        //agregamos
                        CoroutineScope(Dispatchers.IO).launch {
                            //val request=servicio.getPokemonPorID((position + initialPosition + 1).toString()) //antiguo metodo sin favoritos
                            val request=servicio.getPokemonPorID(idDato)
                            if(request.isSuccessful){
                                val datoss=request.body()
                                if(datoss!=null){
                                    withContext(Dispatchers.Main) {
                                        //llamamos a la funcion para ordenar numeros de pokemon
                                        if(datoss.tipos.size == 1){
                                            var pokemon:PokemonTable = PokemonTable(datoss.id,datoss.nombre,datoss.Sprites.otro.offi.img,datoss.tipos[0].tipo.nombre,"")
                                            escribirPokemonEnDB(holder,pokemon)
                                        }else{ //2 tipos
                                            var pokemon:PokemonTable = PokemonTable(datoss.id,datoss.nombre,datoss.Sprites.otro.offi.img,datoss.tipos[0].tipo.nombre,datoss.tipos[1].tipo.nombre)
                                            escribirPokemonEnDB(holder,pokemon)
                                        }
                                        valor = true
                                        holder.btnFav.setBackgroundResource(R.drawable.favon)
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }


    }

    override fun getItemCount(): Int {
        return this.datos.listaPokemon.size
    }

    private fun NumOrder(num: Int):String{ //funcion para ordenar numeros de pokemon
        var numx = num//posicion 1

        var numero = numx.toString() //si es 3 o mas, el numero quedara tal cual
        if(numero.length <= 1){
            numero = "00" + numx.toString() //si es de un solo digito, le agregamos ambos 0
        }else if(numero.length == 2){
            numero = "0" + numx.toString() //si es de dos digitos solo se le agrega un 0
        }
        return numero
    }

    private fun crearConexion() : ConexionAPI{
        return Retrofit.Builder()
            .baseUrl("https://pokeapi.co/api/v2/pokemon/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ConexionAPI::class.java)
    }

    private fun cambiarColor(tipo:String,card:CardView){
        when(tipo){
            in "normal"->card.setBackgroundColor(Color.rgb(255,128,192))
            in "grass"->card.setBackgroundColor(Color.rgb(128,255,0))
            in "fire"->card.setBackgroundColor(Color.rgb(255,0,0))
            in "water"->card.setBackgroundColor(Color.rgb(0,128,255))
            in "bug"->card.setBackgroundColor(Color.rgb(0,128,0))
            in "poison"->card.setBackgroundColor(Color.rgb(128,0,255))
            in "fighting"->card.setBackgroundColor(Color.rgb(255,128,0))
            in "flying"->card.setBackgroundColor(Color.rgb(230,230,230))
            in "ground"->card.setBackgroundColor(Color.rgb(128,64,0))
            in "rock"->card.setBackgroundColor(Color.rgb(125,125,125))
            in "ghost"->card.setBackgroundColor(Color.rgb(192,192,192))
            in "steel"->card.setBackgroundColor(Color.rgb(0,64,64))
            in "electric"->card.setBackgroundColor(Color.rgb(255,255,0))
            in "psychic"->card.setBackgroundColor(Color.rgb(128,0,255))
            in "ice"->card.setBackgroundColor(Color.rgb(0,255,255))
            in "dragon"->card.setBackgroundColor(Color.rgb(255,128,0))
            in "dark"->card.setBackgroundColor(Color.rgb(0,0,64))
            in "fairy"->card.setBackgroundColor(Color.rgb(255,128,255))
            in "unknown"->card.setBackgroundColor(Color.rgb(64,0,128))
            in "shadow"->card.setBackgroundColor(Color.rgb(64,0,64))
        }
    }

    private fun establecerTipo(holder: ViewHolderPokemon,tipo1:String, tipo2:String){
        if(!tipo1.isNullOrEmpty()){
            cambiarColor(tipo1,holder.tipo1)
            holder.txtTipo1.setText(tipo1)
        }

        if(!tipo2.isNullOrEmpty()){
            cambiarColor(tipo2,holder.tipo2)
            holder.tipo2.isVisible = true
            holder.txtTipo2.setText(tipo2)
        }else{
            holder.tipo2.isVisible = false
        }
    }

    private fun obtenerID(letra:String):String{
        //obtener ID para evitar usar conexiones distintas y reutilizar la misma url
        //tambien se puede hacer de otra manera, usando una funcion de conexion con la url que uno quiera (la del urlpokemon)
        //var letra = "https://pokeapi.co/api/v2/pokemon/101/"
        //lo dejaria como 101
        var letrasupr = letra.substring(0,letra.lastIndex) //suprimimos el ultimo / de sobra
        var letraID = letrasupr.substring(letrasupr.lastIndexOf('/') + 1,letrasupr.lastIndex + 1)
        return letraID
    }

    fun escribirPokemonEnDB(holder: ViewHolderPokemon,pokemon:PokemonTable){
        val DAOPokemons = BD.obtenerObjeto(holder.foto.context)?.obtenerDAO_Pokemons()
        GlobalScope.launch {
            DAOPokemons?.agregar(pokemon)
        }
        Toast.makeText(holder.foto.context,"Pokemon Agregado!",Toast.LENGTH_SHORT).show()
    }

    fun borrarPokemonEnDB(holder: ViewHolderPokemon,pokemon:PokemonTable){
        val DAOPokemons = BD.obtenerObjeto(holder.foto.context)?.obtenerDAO_Pokemons()
        GlobalScope.launch {
            DAOPokemons?.eliminar(pokemon)
        }
        Toast.makeText(holder.foto.context,"Pokemon Borrado!",Toast.LENGTH_SHORT).show()
    }
}
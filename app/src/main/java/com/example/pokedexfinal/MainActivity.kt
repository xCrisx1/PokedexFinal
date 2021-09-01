package com.example.pokedexfinal

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pokedexfinal.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.Exception

lateinit var b:ActivityMainBinding
lateinit var datosPokedex: Pokedex
lateinit var datosEmptyPokedex:Pokedex

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //inicializamos el Binding
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        //escondemos la barra molesta >.>
        supportActionBar!!.hide()
        this.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        //fin esconder barra molesta

        //Spinner (seleccion de generacion)
        val listaGen = listOf<String>("Generacion 1","Generacion 2", "Generacion 3","Generacion 4","Generacion 5","Generacion 6","Generacion 7","Generacion 8","Favoritos")
        val adaptadorspn = ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,listaGen)
        b.spnListaGeneracion.adapter = adaptadorspn

        //cargamos la primera generacion que viene por defecto
        cargar()

        //listener del spiner, para cambiar generaciones
        b.spnListaGeneracion.onItemSelectedListener = object:
            AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                //el p2 es la position del item seleccionado
                if(!listaGen[p2].equals("Favoritos")){
                    cargar()
                }else{ //favoritos :c
                    LeerBD()
                }

            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                //no usaremos esto, pero es obligatorio ponerlo >.<
                cargar()
            }

        }
    }

    private fun inicializarRecycler(initialPos:Int){
        var r: RecyclerView = b.rv1
        var g: GridLayoutManager = GridLayoutManager(this,3)
        r.layoutManager = g
        var a = AdaptadorPokemon(datosPokedex,initialPos)
        r.adapter = a

    }

    private fun crearConexion() : ConexionAPI{
        return Retrofit.Builder()
            .baseUrl("https://pokeapi.co/api/v2/pokemon/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ConexionAPI::class.java)
    }

    private fun cargar(){
        //lo vaceamos primero
        EmptyAdapter()

        val servicio=crearConexion()

        CoroutineScope(Dispatchers.IO).launch {
            var request: Response<Pokedex>
            var iniPos:Int = 0
            //el try esta hecho para detectar problemas de no tener internet, lo manejo asi por mas
            //comodidad, hay una forma de averiguar tambien antes si no tiene internet
            //pero prefiero mantenerlo asi, es menos engorroso, tambien previene problemas futuros.
            try {
                request = servicio.getOctavaGeneracion()
            }catch (e: Exception){
                return@launch //si no tiene internet terminamos esta parte (no es necesaria para los favoritos (●'◡'●))
            }
            if(b.spnListaGeneracion.selectedItem == "Generacion 1"){
                request = servicio.getPrimeraGeneracion()
                iniPos = 0
            }else if(b.spnListaGeneracion.selectedItem == "Generacion 2"){
                request = servicio.getSegundaGeneracion()
                iniPos = 151
            }else if(b.spnListaGeneracion.selectedItem == "Generacion 3"){
                request = servicio.getTerceraGeneracion()
                iniPos = 251
            }else if(b.spnListaGeneracion.selectedItem == "Generacion 4"){
                request = servicio.getCuartaGeneracion()
                iniPos = 386
            }else if(b.spnListaGeneracion.selectedItem == "Generacion 5"){
                request = servicio.getQuintaGeneracion()
                iniPos = 493
            }else if(b.spnListaGeneracion.selectedItem == "Generacion 6"){
                request = servicio.getSextaGeneracion()
                iniPos = 649
            }else if(b.spnListaGeneracion.selectedItem == "Generacion 7"){
                request = servicio.getSeptimaGeneracion()
                iniPos = 721
            }else{ //GENERACION 8
                request = servicio.getOctavaGeneracion()
                iniPos = 809
            }

            if(request.isSuccessful){
                val datos=request.body()
                if(datos!=null){
                    withContext(Dispatchers.Main) {
                        System.out.println("vamos bien")
                        datosPokedex = datos
                        inicializarRecycler(iniPos)
                    }
                }
            }

        }
    }

    private fun LeerBD(){
        val DAOPokemons = BD.obtenerObjeto(this)?.obtenerDAO_Pokemons()
        doAsync {
            //val pokevas = DAOPokemons?.buscarporID("1") //el ID 1 puede o no estar ocupado, no podre hacerlo optimo por ahora, sin embargo hay una forma trucha xP
            val pokemons = DAOPokemons?.buscarTodos() //buscamos para saber si cargar los datos o no
            uiThread{
                if(pokemons?.size == 0){
                    //avisamos que no hay pokemon guardados
                    Toast.makeText(this@MainActivity,"No tiene Pokemon en sus favoritos!", Toast.LENGTH_LONG).show()

                    //comenzamos a vaciar el adapter, para que se actualize a uno vacio y sea mas simple y rapido(optimo)
                    EmptyAdapter()

                }else{
                    //rellenar datos
                    if (pokemons != null) { //me ibliga a ponerlo, inutil pero bueno... dejemos al lenguaje ser feliz xD
                        var listapokemons =ArrayList<urlPokemon>()
                        var pokedexLlenar:Pokedex = Pokedex(0,listapokemons)
                        for(p in pokemons){
                            pokedexLlenar.cantidad = pokedexLlenar.cantidad + 1
                            var tempurl:urlPokemon = urlPokemon(p.Nombre,"https://pokeapi.co/api/v2/pokemon/" + p.ID.toString() + "/")//el url nunca se utilizo anteriormente, pero esta vez si lo usaremos para sacar el id :3
                            listapokemons.add(tempurl)
                        }
                        //ajustamos el layout en caso de no tener conexion
                        var g: GridLayoutManager = GridLayoutManager(this@MainActivity,3)
                        b.rv1.layoutManager = g
                        //Asignamos el adapter
                        b.rv1.adapter = AdaptadorPokemon(pokedexLlenar,0)
                    }
                }
            }
        }
    }

    private fun EmptyAdapter(){
        //EMPTY ADAPTER FIRST
        var listEmpty = ArrayList<urlPokemon>()
        var pNull:Pokedex = Pokedex(0,listEmpty)

        //ajustamos el layout en caso de no tener conexion
        var g: GridLayoutManager = GridLayoutManager(this@MainActivity,3)
        b.rv1.layoutManager = g
        //Asignamos el adapter
        b.rv1.adapter = AdaptadorPokemon(pNull,0)
        //FIN EMPTY ADAPTER
    }
}
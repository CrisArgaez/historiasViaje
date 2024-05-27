package com.example.historiasdeviaje.ui.theme

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.historiasdeviaje.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

// Clase de datos para representar una historia
data class Historia(
    val historiaId: Int,
    val titulo: String,
    val descripcion: String,
    val imagenBase64: String
)

class VerHistoriaActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HistoriaAdapter
    private lateinit var searchEditText: EditText
    private var historiasList: ArrayList<Historia> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ver_historia)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        searchEditText = findViewById(R.id.searchEditText)

        val sharedPreferences = getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE)

        // Obtener el ID del usuario del intent o de SharedPreferences
        var idUsuario = intent.getIntExtra("usuarioID", -1)

        // Si el ID no está en el Intent, intenta obtenerlo de SharedPreferences
        if (idUsuario == -1) {
            idUsuario = sharedPreferences.getInt("usuarioID", -1)
        } else {
            // Si el ID está en el Intent, guárdalo en SharedPreferences (primera vez que entra)
            sharedPreferences.edit().putInt("usuarioID", idUsuario).apply()
        }

        // Verificar que se haya recibido el ID de usuario
        if (idUsuario != -1) {
            adapter = HistoriaAdapter(this, ArrayList(), idUsuario)
            recyclerView.adapter = adapter

            ObtenerHistoriasTask().execute()
        } else {
            Toast.makeText(this, "Error al obtener el ID del usuario", Toast.LENGTH_SHORT).show()
            // Puedes manejar el error de forma más apropiada aquí, como redirigir al inicio de sesión
            finish()
        }

        // Referencia al botón "Favoritos" en la barra de navegación inferior
        val favoritosButton = findViewById<LinearLayout>(R.id.favoritosButton)

        favoritosButton.setOnClickListener {
            // Iniciar FavoritosActivity y pasar el usuarioID
            val intent = Intent(this@VerHistoriaActivity, FavoritosActivity::class.java)
            intent.putExtra("usuarioID", idUsuario)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        adapter.onItemClickListener = { historia -> // Asigna el listener al adaptador
            val intent = Intent(this@VerHistoriaActivity, DetalleHistoriaActivity::class.java)
            intent.putExtra("HISTORIA_ID", historia.historiaId)
            startActivity(intent)
        }

        val addButton: FloatingActionButton = findViewById(R.id.addButton)
        val intent = Intent(this, PublicarHistoriaActivity::class.java)

        addButton.setOnClickListener {
            intent.putExtra("usuarioID", idUsuario) // Agrega el ID del usuario al Intent
            startActivity(intent)
        }

        // Añadir el TextWatcher al EditText de búsqueda
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filter(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    // Método para filtrar la lista de historias basado en el título
    private fun filter(text: String) {
        val filteredList = ArrayList<Historia>()
        for (historia in historiasList) {
            if (historia.titulo.contains(text, true)) {
                filteredList.add(historia)
            }
        }
        adapter.updateData(filteredList)
    }

    inner class ObtenerHistoriasTask : AsyncTask<Void, Void, String>() {
        override fun doInBackground(vararg params: Void?): String {
            val urlString = URL("http://192.168.0.8:80/obtener_historia.php")
            return try {
                URL(urlString.toString()).readText()
            } catch (e: Exception) {
                Log.e("ObtenerHistoriasTask", "Error: ${e.message}")
                "" // Devuelve una cadena vacía en caso de error
            }
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            try {
                if (!result.isNullOrEmpty()) {
                    val jsonArray = JSONArray(result)
                    val historias = ArrayList<Historia>()
                    for (i in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(i)
                        val historia = Historia(
                            jsonObject.getInt("HistoriaID"),
                            jsonObject.getString("Titulo"),
                            jsonObject.getString("Descripcion"),
                            jsonObject.getString("Imagen")
                        )
                        historias.add(historia)
                    }
                    historiasList = historias // Guarda la lista completa
                    adapter.updateData(historias)
                } else {
                    Toast.makeText(this@VerHistoriaActivity, "No hay historias disponibles", Toast.LENGTH_SHORT).show()
                }
            } catch (e: JSONException) {
                Log.e("ObtenerHistoriasTask", "Error al analizar JSON: ${e.message}")
                Toast.makeText(this@VerHistoriaActivity, "Error al obtener las historias", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

// Adaptador para el RecyclerView
class HistoriaAdapter(private val context: Context, private var historias: ArrayList<Historia>, private val idUsuario: Int) : RecyclerView.Adapter<HistoriaAdapter.ViewHolder>() {

    // Listener para el clic en un elemento
    var onItemClickListener: ((Historia) -> Unit)? = null

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tituloTextView: TextView = itemView.findViewById(R.id.tituloTextView)
        val descripcionTextView: TextView = itemView.findViewById(R.id.descripcionTextView)
        val imagenImageView: ImageView = itemView.findViewById(R.id.imagenImageView)
        val favoriteButton: FloatingActionButton = itemView.findViewById(R.id.favoriteButton) // <-- Referencia al botón
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_historia, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val historia = historias[position]
        holder.tituloTextView.text = historia.titulo
        holder.descripcionTextView.text = historia.descripcion

        // Cargar imagen (sin cambios)
        val decodedString = Base64.decode(historia.imagenBase64, Base64.DEFAULT)
        val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
        Glide.with(holder.itemView.context).load(decodedByte).into(holder.imagenImageView)

        holder.favoriteButton.setOnClickListener {
            MarcarFavoritoTask(historia.historiaId, idUsuario, holder).execute()
        }

        holder.itemView.setOnClickListener { // Maneja el clic en el itemView
            onItemClickListener?.invoke(historia) // Llama al listener
        }
    }

    override fun getItemCount(): Int {
        return historias.size
    }

    // Función para actualizar los datos del adaptador
    fun updateData(newHistorias: List<Historia>) {
        historias = newHistorias as ArrayList<Historia>
        notifyDataSetChanged()
    }

    inner class MarcarFavoritoTask(
        private val historiaId: Int,
        private val usuarioId: Int,
        private val holder: ViewHolder
    ) : AsyncTask<Void, Void, String>() {

        override fun doInBackground(vararg params: Void?): String {
            val urlString = "http://192.168.0.8:80/guardar_favorito.php?historia_id=$historiaId&usuario_id=$usuarioId"

            return try {
                URL(urlString).readText()
            } catch (e: Exception) {
                Log.e("MarcarFavoritoTask", "Error: ${e.message}")
                "Error: ${e.message}" // Devuelve el mensaje de error
            }
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            try {
                val jsonResponse = JSONObject(result)
                val status = jsonResponse.getString("status")
                val message = jsonResponse.getString("message")

                if (status == "Success") {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Error: $message", Toast.LENGTH_SHORT).show()
                }
            } catch (e: JSONException) {
                Log.e("MarcarFavoritoTask", "Error al analizar JSON: ${e.message}")
                Toast.makeText(context, "Error al procesar la respuesta del servidor", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

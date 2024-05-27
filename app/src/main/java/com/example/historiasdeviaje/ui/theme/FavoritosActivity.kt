package com.example.historiasdeviaje.ui.theme

import android.content.Context
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.historiasdeviaje.R
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class HistoriaFavoritos(
    val historiaId: Int,
    val titulo: String,
    val descripcion: String,
    val imagenBase64: String
)

class FavoritosActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FavoritosAdapter
    private var historiasFavoritas = mutableListOf<HistoriaFavoritos>()

    override fun onResume() {
        super.onResume()
        setContentView(R.layout.activity_favoritos)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Inicializamos el adaptador con una lista vacía
        adapter = FavoritosAdapter(this, historiasFavoritas)
        recyclerView.adapter = adapter

        // Cargamos las historias favoritas
        ObtenerHistoriasFavoritasTask().execute()

        // Referencia a los botones de navegación en FavoritosActivity
        val historiasButton = findViewById<LinearLayout>(R.id.historiasButton)
        val favoritosButton = findViewById<LinearLayout>(R.id.favoritosButton)

        historiasButton.setOnClickListener {
            // Regresar a VerHistoriaActivity
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right) // Animaciones
        }

        // Manejar el clic del botón "Favoritos" (puedes agregar lógica aquí)
        favoritosButton.setOnClickListener {
            // ... (tu lógica para el botón de favoritos)
        }
    }

    private fun obtenerIdUsuario(): Int {
        // ... (lógica para obtener el ID del usuario)
        return 1 // Ejemplo: Devuelve un ID de usuario de prueba
    }

    inner class ObtenerHistoriasFavoritasTask : AsyncTask<Void, Void, String>() {
        override fun doInBackground(vararg params: Void?): String {
            val urlString = URL("http://192.168.0.8:80/obtener_favoritos.php?usuario_id=1") // Reemplaza con tu IP real
            return try {
                URL(urlString.toString()).readText()
            } catch (e: Exception) {
                Log.e("ObtenerHistoriasFavTask", "Error: ${e.message}")
                "[]" // Devuelve un array vacío en caso de error
            }
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            Log.d("JSON recibido", result ?: "Resultado nulo") // Imprimir el JSON en la consola

            try {
                val jsonArray = JSONArray(result)
                val historiasFavoritasIds = mutableListOf<Int>()
                for (i in 0 until jsonArray.length()) {
                    historiasFavoritasIds.add(jsonArray.getInt(i))
                }

                historiasFavoritas.clear()
                for (historiaId in historiasFavoritasIds) {
                    ObtenerHistoriaEspecificaTask(historiaId).execute()
                }
            } catch (e: JSONException) {
                Log.e("ObtenerHistoriasFavTask", "Error al analizar JSON: ${e.message}")
                Toast.makeText(this@FavoritosActivity, "Error al obtener las historias favoritas", Toast.LENGTH_SHORT).show()
            }
        }
    }

    inner class ObtenerHistoriaEspecificaTask(private val historiaId: Int) : AsyncTask<Void, Void, HistoriaFavoritos?>() {
        override fun doInBackground(vararg params: Void?): HistoriaFavoritos? {
            val urlString = URL("http://192.168.0.8:80/obtener_historia_especifica.php?historia_id=$historiaId")
            return try {
                val result = URL(urlString.toString()).readText()
                Log.d("JSONHistoriaEspecifica", result)  // Imprimir el JSON en la consola
                val jsonObject = JSONObject(result)
                HistoriaFavoritos(
                    jsonObject.getInt("HistoriaID"),
                    jsonObject.getString("Titulo"),
                    jsonObject.getString("Descripcion"),
                    jsonObject.getString("Imagen")
                )
            } catch (e: Exception) {
                Log.e("ObtenerHistoriaEspecificaTask", "Error: ${e.message}")
                null
            }
        }

        override fun onPostExecute(historia: HistoriaFavoritos?) {
            super.onPostExecute(historia)
            if (historia != null) {
                historiasFavoritas.add(historia)
                adapter.notifyItemInserted(historiasFavoritas.size - 1)
            }
        }
    }

}

// Adaptador para el RecyclerView
class FavoritosAdapter(private val context: Context, private val historias: List<HistoriaFavoritos>) : RecyclerView.Adapter<FavoritosAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tituloTextView: TextView = itemView.findViewById(R.id.tituloTextView)
        val descripcionTextView: TextView = itemView.findViewById(R.id.descripcionTextView)
        val imagenImageView: ImageView = itemView.findViewById(R.id.imagenImageView)
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
    }

    override fun getItemCount(): Int {
        return historias.size
    }
}

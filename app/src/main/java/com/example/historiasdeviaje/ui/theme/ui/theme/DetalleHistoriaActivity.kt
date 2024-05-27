package com.example.historiasdeviaje.ui.theme

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.historiasdeviaje.R
import org.json.JSONException
import org.json.JSONObject
import java.net.URL

class DetalleHistoriaActivity : AppCompatActivity() {

    private val REQUEST_CODE_PERMISSIONS = 101
    private var latitud: Double = 0.0
    private var longitud: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_historia)

        val historiaId = intent.getIntExtra("HISTORIA_ID", -1)
        if (historiaId != -1) {
            ObtenerDetalleHistoriaTask().execute(historiaId)
        }

        // Solicitar permisos necesarios (ajústalo según tus necesidades)
        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_CODE_PERMISSIONS
            )
        }

        val botonVerMapa = findViewById<Button>(R.id.botonVerMapa)
        botonVerMapa.setOnClickListener {
            val intent = Intent(this@DetalleHistoriaActivity, MapaActivity::class.java)
            intent.putExtra("LATITUD", latitud)
            intent.putExtra("LONGITUD", longitud)
            startActivity(intent)
        }
    }

    private fun allPermissionsGranted() = arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE).all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                // Los permisos fueron concedidos, puedes realizar la acción que requiera los permisos
            } else {
                Toast.makeText(this, "Se necesitan permisos para utilizar esta funcionalidad", Toast.LENGTH_SHORT).show()
            }
        }
    }

    inner class ObtenerDetalleHistoriaTask : AsyncTask<Int, Void, String>() {
        override fun doInBackground(vararg params: Int?): String {
            val historiaId = params[0]
            val urlString = "http://192.168.0.8:80/obtener_detalle_historia.php?id=$historiaId" // Reemplaza con tu IP
            return try {
                URL(urlString).readText()
            } catch (e: Exception) {
                Log.e("ObtenerDetalleHistoria", "Error: ${e.message}")
                ""
            }
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            try {
                val jsonObject = JSONObject(result)
                val titulo = jsonObject.getString("Titulo")
                val descripcion = jsonObject.getString("Descripcion")
                val imagenBase64 = jsonObject.getString("Imagen")
                latitud = jsonObject.getDouble("Latitud")
                longitud = jsonObject.getDouble("Longitud")

                findViewById<TextView>(R.id.detalleTitulo).text = titulo
                findViewById<TextView>(R.id.detalleDescripcion).text = descripcion
                Glide.with(this@DetalleHistoriaActivity).load(Base64.decode(imagenBase64, Base64.DEFAULT)).into(findViewById(R.id.detalleImagen))
                // Aquí puedes manejar los datos de latitud y longitud como necesites
            } catch (e: JSONException) {
                Log.e("ObtenerDetalleHistoria", "Error al analizar JSON: ${e.message}")
                Toast.makeText(this@DetalleHistoriaActivity, "Error al obtener los detalles de la historia", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
package com.example.historiasdeviaje.ui.theme

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.historiasdeviaje.R
import org.json.JSONException
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val nombreUsuario = findViewById<EditText>(R.id.nombre_usuario)
        val contrasena = findViewById<EditText>(R.id.contrasena)
        val botonIniciarSesion = findViewById<Button>(R.id.boton_iniciar_sesion)
        val botonRegistrar = findViewById<Button>(R.id.boton_registrar)

        botonIniciarSesion.setOnClickListener {
            val dataToSend = JSONObject()
            dataToSend.put("NombreUsuario", nombreUsuario.text.toString())
            dataToSend.put("Contrasena", contrasena.text.toString())

            SendDataTask().execute(dataToSend.toString())
        }

        botonRegistrar.setOnClickListener {
            val intent = Intent(this, RegistroActivity::class.java)
            startActivity(intent)
        }
    }

    inner class SendDataTask : AsyncTask<String, Void, String>() {
        override fun doInBackground(vararg params: String?): String {
            var response = ""
            try {
                // Cambia la URL por la de tu servidor
                val url = URL("http://192.168.0.8:80/inicio_sesion.php")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json;charset=utf-8")
                conn.doOutput = true

                val os = OutputStreamWriter(conn.outputStream)
                os.write(params[0])
                os.flush()
                os.close()

                if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                    response = conn.inputStream.bufferedReader().use { it.readText() }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return response
        }

        override fun onPostExecute(result: String) {
            super.onPostExecute(result)
            Log.d("LoginActivity", "Respuesta del servidor: $result")

            try {
                val jsonResponse = JSONObject(result)
                if (jsonResponse.getBoolean("success")) {
                    val usuarioID = jsonResponse.getInt("usuarioID") // Obtener el ID del usuario

                    // Iniciar PublicarHistoriaActivity y pasar el usuarioID
                    val intent = Intent(this@LoginActivity, VerHistoriaActivity::class.java)
                    intent.putExtra("usuarioID", usuarioID) // Pasar el ID como extra
                    startActivity(intent)
                } else {
                    Log.d("LoginActivity", "Error al iniciar sesión")
                    // Manejar el error de inicio de sesión (mostrar mensaje al usuario, etc.)
                }
            } catch (e: JSONException) {
                Log.e("LoginActivity", "Error al analizar JSON: ${e.message}")
                // Manejar el error de análisis JSON (mostrar mensaje al usuario, etc.)
            }
        }

    }
}

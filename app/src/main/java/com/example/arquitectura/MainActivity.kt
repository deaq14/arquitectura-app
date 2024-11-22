package com.example.arquitectura

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.arquitectura.databinding.ActivityMainBinding
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar el toolbar
        setSupportActionBar(binding.toolbar)

        // Botón para enviar correo
        binding.Correo.setOnClickListener {
            enviarCorreo()
        }

        // Botón para iniciar pago PSE
        findViewById<Button>(R.id.Pagos).setOnClickListener {
            iniciarPagoPSE()
        }

        // Configuración de los fragmentos
        findViewById<Button>(R.id.POT).setOnClickListener {
            loadFragment(Fragment_pot())
        }

        findViewById<Button>(R.id.PlanesParciales).setOnClickListener {
            loadFragment(Fragment_PlanesP)
        }

        findViewById<Button>(R.id.Obras).setOnClickListener {
            loadFragment(ObrasFragment())
        }
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentContainer, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    // Función para enviar correos
    private fun enviarCorreo() {
        val destinatario = arrayOf("example@dominio.com") // Cambia por tu dirección
        val asunto = "Consulta desde la app"
        val mensaje = "Hola, escribo porque tengo la siguiente consulta:"

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:") // Solo manejará aplicaciones de correo
            putExtra(Intent.EXTRA_EMAIL, destinatario)
            putExtra(Intent.EXTRA_SUBJECT, asunto)
            putExtra(Intent.EXTRA_TEXT, mensaje)
        }

        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            // Mostrar mensaje si no hay apps de correo
            Snackbar.make(binding.root, "No hay aplicaciones de correo instaladas", Snackbar.LENGTH_SHORT).show()
        }
    }

    // Función para iniciar el pago con PSE
    private fun iniciarPagoPSE() {
        val url = "https://sandbox.wompi.co/v1/transactions"
        val client = OkHttpClient()

        // Crear JSON del cuerpo de la solicitud
        val jsonBody = JSONObject()
        jsonBody.put("amount_in_cents", 100000) // Monto: 10,000.00 COP
        jsonBody.put("currency", "COP")
        jsonBody.put("customer_email", "example@correo.com")
        jsonBody.put("payment_method", JSONObject().apply {
            put("type", "PSE")
            put("user_type", 0) // Persona natural
            put("user_legal_id", "123456789")
            put("user_legal_id_type", "CC") // Tipo de documento
            put("financial_institution_code", "1040") // Código del banco
        })
        jsonBody.put("redirect_url", "https://tu-app.com/retorno")

        val request = Request.Builder()
            .url(url)
            .post(RequestBody.create(MediaType.parse("application/json"), jsonBody.toString()))
            .addHeader("Authorization", "Bearer YOUR_PUBLIC_API_KEY") // Agrega tu API Key
            .build()

        // Enviar la solicitud
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    Snackbar.make(binding.root, "Error al iniciar pago con PSE", Snackbar.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body()?.string()
                val jsonResponse = JSONObject(responseData ?: "")
                val transactionData = jsonResponse.optJSONObject("data")
                val paymentLink = transactionData?.optString("payment_link")

                if (paymentLink != null) {
                    // Abrir la URL en el navegador
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(paymentLink))
                    startActivity(intent)
                } else {
                    runOnUiThread {
                        Snackbar.make(binding.root, "No se pudo generar el enlace de pago", Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}

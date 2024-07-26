package com.jeantituana2024.tesis.payment

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.jeantituana2024.tesis.databinding.ActivityPdfViewBinding
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Callback
import okhttp3.Call
import okhttp3.Response
import java.io.IOException

class PdfViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPdfViewBinding

    var pdfUrl = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtén la URL del PDF desde el Intent
        pdfUrl = intent.getStringExtra("pdfUrl") ?: ""

        loadPdf()

        setSupportActionBar(binding.toolbar)

        // Establecer el título del Toolbar
        supportActionBar?.title = "Comprobante de Pago"

        // Habilitar el botón de regreso en el Toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // Configurar el comportamiento del botón de regreso
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

    }

    private fun loadPdf() {
        if (pdfUrl.isEmpty()) {
            showToast("URL del PDF no válida")
            binding.progressBar.visibility = View.GONE
            return
        }

        binding.progressBar.visibility = View.VISIBLE

        val client = OkHttpClient()
        val request = Request.Builder()
            .url(pdfUrl)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    binding.progressBar.visibility = View.GONE
                    showToast("Error al cargar el PDF: ${e.message}")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val inputStream = response.body?.byteStream()
                    runOnUiThread {
                        inputStream?.let {
                            binding.pdfView.fromStream(it)
                                .defaultPage(0)
                                .enableSwipe(true)
                                .enableDoubletap(true)
                                .enableAnnotationRendering(true)
                                .enableAntialiasing(true)
                                .load()
                        }
                        binding.progressBar.visibility = View.GONE
                    }
                } else {
                    runOnUiThread {
                        binding.progressBar.visibility = View.GONE
                        showToast("Error al cargar el PDF: ${response.message}")
                    }
                }
            }
        })
    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
    }
}
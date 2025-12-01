package com.example.resnet_app

import android.content.res.AssetFileDescriptor
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import android.view.View

class MainActivity : AppCompatActivity() {

    private var isImageLoaded = false
    lateinit var selectBtn: Button
    lateinit var takephotoBtn: Button
    lateinit var predictBtn: Button
    lateinit var imageView: ImageView
    lateinit var resultTextView: TextView

    private val MODEL_PATH = "lite_model_v2.tflite"
    private lateinit var tflite: Interpreter
    private val INPUT_SIZE = 128
    private val classes = arrayOf(
        "Biodegradowalne",
        "Elektroodpady",
        "Szkło",
        "Zmieszane",
        "Papier",
        "Metal i tworzywa sztuczne",
        "Tekstylia"
    )

    val galleryLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { galleryUri ->
                try {
                    imageView.setImageURI(galleryUri)
                    imageView.scaleType = ImageView.ScaleType.FIT_CENTER
                    isImageLoaded = true
                    resultTextView.visibility = View.INVISIBLE
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

    val cameraLauncher: ActivityResultLauncher<Void?> =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
            bitmap?.let { photoBitmap ->
                try {
                    imageView.setImageBitmap(photoBitmap)
                    imageView.scaleType = ImageView.ScaleType.FIT_CENTER
                    isImageLoaded = true
                    resultTextView.visibility = View.INVISIBLE
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        selectBtn = findViewById(R.id.button_wybierz)
        takephotoBtn = findViewById(R.id.button_zrob_zdjecie)
        predictBtn = findViewById(R.id.button_predykcja)
        imageView = findViewById(R.id.image_header)
        resultTextView = findViewById(R.id.text_wynik)

        // 🔹 1. Inicjalizacja interpretera TFLite
        try {
            tflite = Interpreter(loadModelFile())
            // Dodajmy log, jeśli się uda
            android.util.Log.d("TFLite", "Model załadowany poprawnie!")
        } catch (e: Exception) {
            e.printStackTrace()
            // To wypisze dokładny błąd w Logcat (na dole Android Studio)
            android.util.Log.e("TFLite", "Błąd ładowania: ${e.message}")
            Toast.makeText(this, "Błąd modelu: ${e.message}", Toast.LENGTH_LONG).show()
        }

        selectBtn.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        takephotoBtn.setOnClickListener {
            cameraLauncher.launch(null)
        }

        predictBtn.setOnClickListener {
            // 🔹 2. Pobranie Bitmapy z ImageView

            if (!isImageLoaded) {
                // Oraz krótki dymek
                Toast.makeText(this, "Wybierz najpierw zdjęcie", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Przerwij działanie funkcji
            }

            val drawable = imageView.drawable
            if (drawable != null && drawable is BitmapDrawable) {
                val bitmap = drawable.bitmap
                classifyImage(bitmap)
            } else {
                Toast.makeText(this, "Wybierz najpierw zdjęcie", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 🔹 Logika klasyfikacji
    // 🔹 Logika klasyfikacji
    private fun classifyImage(originalBitmap: Bitmap) {
        // 1. Skalowanie obrazu do 128x128
        val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, INPUT_SIZE, INPUT_SIZE, true)

        // 2. Przygotowanie ByteBuffer (wejście modelu)
        val inputBuffer = convertBitmapToByteBuffer(resizedBitmap)

        // 3. Przygotowanie tablicy na wynik
        val output = Array(1) { FloatArray(classes.size) }

        // 4. Uruchomienie inferencji
        tflite.run(inputBuffer, output)

        // 5. Interpretacja wyników
        val probabilities = output[0]
        var maxPos = 0
        var maxConfidence = 0.0f

        for (i in probabilities.indices) {
            if (probabilities[i] > maxConfidence) {
                maxConfidence = probabilities[i]
                maxPos = i
            }
        }

        val rawClass = classes[maxPos] // Surowa nazwa klasy, np. "Papier"
        val confidencePercent = maxConfidence * 100 // Zamiana 0.95 na 95.0

        // 🔹 6. Logika instrukcji i kolorów
        var instruction = ""
        var colorCode = 0xFF000000.toInt() // Domyślny czarny

        when (rawClass) {
            "Biodegradowalne" -> {
                instruction = "BIO\nWyrzuć do brązowego pojemnika"
                colorCode = 0xFF795548.toInt() // Brązowy
            }
            "Papier" -> {
                instruction = "PAPIER\nWyrzuć do niebieskiego pojemnika"
                colorCode = 0xFF2196F3.toInt() // Niebieski
            }
            "Szkło" -> {
                instruction = "SZKŁO\nWyrzuć do zielonego pojemnika"
                colorCode = 0xFF4CAF50.toInt() // Zielony
            }
            "Metal i tworzywa sztuczne" -> {
                instruction = "METALE I TWORZYWA SZTUCZNE\nWyrzuć do żółtego pojemnika"
                colorCode = 0xFFFFC107.toInt() // Żółty / Bursztynowy
            }
            "Zmieszane" -> {
                instruction = "ZMIESZANE\nWyrzuć do czarnego pojemnika"
                colorCode = 0xFF607D8B.toInt() // Szary
            }
            "Elektroodpady" -> {
                instruction = "ELEKTROODPADY\nOddaj do punktu zbiórki, np. w sklepach z elektroniką"
                colorCode = 0xFFF44336.toInt() // Czerwony
            }
            "Tekstylia" -> {
                instruction = "TEKSTYLIA\nOddaj do pojemnika na odzież, jeśli przedmiot jest w dobrym stanie lub\n do punktu zbiórki (PSZOK)"
                colorCode = 0xFF9C27B0.toInt() // Fioletowy
            }
            else -> {
                instruction = "Nie rozpoznano kategorii: $rawClass"
            }
        }

        // 🔹 7. Budowanie tekstu końcowego z procentami
        // %.1f%% oznacza: jedna cyfra po przecinku i znak procenta na końcu
        val finalResult = "$instruction\nPewność: %.1f%%".format(confidencePercent)

        // Ustawienie tekstu i koloru
        resultTextView.text = finalResult
        resultTextView.setTextColor(colorCode)

        resultTextView.visibility = View.VISIBLE
    }

    // 🔹 Helper: Ładowanie pliku z assets
    @Throws(IOException::class)
    private fun loadModelFile(): ByteBuffer {
        val fileDescriptor: AssetFileDescriptor = assets.openFd(MODEL_PATH)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel: FileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    // 🔹 Helper: Konwersja Bitmap -> ByteBuffer (PREPROCESSING)
    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        // Alokacja pamięci:
        // 4 bajty na float * 128 (wys) * 128 (szer) * 3 (kanały RGB)
        val byteBuffer = ByteBuffer.allocateDirect(4 * INPUT_SIZE * INPUT_SIZE * 3)
        byteBuffer.order(ByteOrder.nativeOrder())

        val intValues = IntArray(INPUT_SIZE * INPUT_SIZE)
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        var pixel = 0
        for (i in 0 until INPUT_SIZE) {
            for (j in 0 until INPUT_SIZE) {
                val input = intValues[pixel++]

                // Wyciąganie składowych RGB z formatu ARGB
                val r = (input shr 16 and 0xFF).toFloat()
                val g = (input shr 8 and 0xFF).toFloat()
                val b = (input and 0xFF).toFloat()

                // ⚠️ KLUCZOWE DLA RESNET V2 ⚠️
                // Pythonowy preprocess_input dla ResNetV2 robi: (x - 127.5) / 127.5
                // Dzięki temu wartości są w przedziale [-1, 1]

                byteBuffer.putFloat((r - 127.5f) / 127.5f)
                byteBuffer.putFloat((g - 127.5f) / 127.5f)
                byteBuffer.putFloat((b - 127.5f) / 127.5f)
            }
        }
        return byteBuffer
    }

    override fun onDestroy() {
        super.onDestroy()
        // Zwalnianie zasobów modelu
        if (::tflite.isInitialized) {
            tflite.close()
        }
    }
}
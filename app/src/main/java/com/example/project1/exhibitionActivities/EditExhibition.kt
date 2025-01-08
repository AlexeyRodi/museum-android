package com.example.project1.exhibitionActivities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Toast
import com.example.project1.ApiClient
import com.example.project1.MainList
import com.example.project1.R
import com.example.project1.exhibition.Exhibition
import com.example.project1.exhibition.ExhibitionRepository
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException

class EditExhibition : AppCompatActivity() {
    private lateinit var exhibitionStartDate: EditText
    private lateinit var exhibitionEndDate: EditText
    private lateinit var exhibitionResponsiblePerson: EditText
    private lateinit var buttonSaveExhibition: Button
    private lateinit var buttonChooseImage: Button
    private lateinit var initialImage: String
    private var selectedImageUri: Uri? = null

    private val PICK_IMAGE_REQUEST = 1

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_exhibition)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val myToolbar: Toolbar = findViewById(R.id.my_toolbar)
        setSupportActionBar(myToolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        exhibitionStartDate = findViewById(R.id.exhibitionStartDate)
        exhibitionEndDate = findViewById(R.id.exhibitionEndDate)
        exhibitionResponsiblePerson = findViewById(R.id.exhibitionResponsiblePerson)
        buttonSaveExhibition = findViewById(R.id.buttonSaveExhibition)
        buttonChooseImage = findViewById(R.id.buttonСhooseImage)

        val exhibitionId = intent.getIntExtra("EXHIBITION_ID", -1)

        if (exhibitionId != -1) {
            loadExhibitionDetails(exhibitionId)
        } else {
            Toast.makeText(this, "Ошибка: выставка не найдена", Toast.LENGTH_SHORT).show()
        }

        buttonChooseImage.setOnClickListener {
            openImageChooser()
        }

        buttonSaveExhibition.setOnClickListener {
            val exhibitionName = myToolbar.title.toString()
            val exhibitionStartDate = exhibitionStartDate.text.toString()
            val exhibitionEndDate = exhibitionEndDate.text.toString()
            val exhibitionResponsiblePerson = exhibitionResponsiblePerson.text.toString()


            if (exhibitionName.isBlank() || exhibitionStartDate.isBlank() || exhibitionEndDate.isBlank() || exhibitionResponsiblePerson.isBlank()) {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val exhibitionImage = selectedImageUri?.let { convertImageToBase64(it) } ?: ""

            val exhibitionImg = convertImagePathToUri(initialImage)

            val base64Image = exhibitionImg?.let { convertImageToBase64(it) }

            if (exhibitionImage == "") {
                val exhibition = Exhibition(
                    exhibition_id = exhibitionId,
                    name = exhibitionName,
                    start_date = exhibitionStartDate,
                    end_date = exhibitionEndDate,
                    responsible_person = exhibitionResponsiblePerson,
                    image_upload = base64Image
                )
                val exhibitionJson = Gson().toJson(exhibition)
                Log.d("ExhibitJson", exhibitionJson)

                updateExhibition(exhibition)
            } else {
                val exhibition = Exhibition(
                    exhibition_id = exhibitionId,
                    name = exhibitionName,
                    start_date = exhibitionStartDate,
                    end_date = exhibitionEndDate,
                    responsible_person = exhibitionResponsiblePerson,
                    image_upload = exhibitionImage,
                )
                val exhibitionJson = Gson().toJson(exhibition)
                Log.d("ExhibitJson", exhibitionJson)

                updateExhibition(exhibition)
            }
        }
    }


    @SuppressLint("SuspiciousIndentation")
    private fun loadExhibitionDetails(exhibitionId: Int) {
        val myToolbar: Toolbar = findViewById(R.id.my_toolbar)
        val api = ApiClient.retrofit.create(ExhibitionRepository::class.java)
        val baseUrl = "http://10.0.2.2:8000"


        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = api.getExhibitionDetail(exhibitionId).execute()

                if (response.isSuccessful) {
                    val exhibit = response.body()

                    withContext(Dispatchers.Main) {
                        exhibit?.let {
                            myToolbar.setTitle(it.name)
                            exhibitionStartDate.setText(it.start_date)
                            exhibitionEndDate.setText(it.end_date)
                            exhibitionResponsiblePerson.setText(it.responsible_person)
                            initialImage = it.image.toString()

                        } ?: run {
                            Toast.makeText(
                                this@EditExhibition,
                                "Детали выставки не найдены",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@EditExhibition,
                            "Ошибка загрузки данных: ${response.code()} ${response.message()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@EditExhibition,
                        "Ошибка: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }


        }
    }

    private fun updateExhibition(exhibition: Exhibition) {
        Log.d(
            "UpdateExhibition",
            "Отправка запроса на сервер для выставки с ID: ${exhibition.exhibition_id}"
        )
        val repository = ApiClient.retrofit.create(ExhibitionRepository::class.java)

        val call = repository.updateExhibition(exhibition.exhibition_id!!, exhibition)

        call.enqueue(object : Callback<Exhibition> {
            override fun onResponse(call: Call<Exhibition>, response: Response<Exhibition>) {
                if (response.isSuccessful) {
                    Toast.makeText(
                        this@EditExhibition,
                        "Данные обновлены успешно!",
                        Toast.LENGTH_SHORT
                    ).show()
                    val intent = Intent(this@EditExhibition, MainList::class.java)

                    intent.putExtra(
                        "EXHIBITION_ID",
                        exhibition.exhibition_id
                    )
                    startActivity(intent)
                } else {
                    Toast.makeText(
                        this@EditExhibition,
                        "Ошибка обновления: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<Exhibition>, t: Throwable) {
                Toast.makeText(this@EditExhibition, "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    private fun openImageChooser() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "image/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.data!!
            Log.d("SelectedImageUri", selectedImageUri.toString())
        }
    }

    private fun convertImageToBase64(uri: Uri): String? {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()

            return Base64.encodeToString(byteArray, Base64.DEFAULT)
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }

    private fun convertImagePathToUri(imagePath: String): Uri? {
        val file = File(filesDir, imagePath)
        return if (file.exists()) Uri.fromFile(file) else null
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}
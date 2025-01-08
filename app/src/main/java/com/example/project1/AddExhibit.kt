package com.example.project1

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
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Toast
import com.example.project1.exhibit.Exhibit
import com.example.project1.exhibit.ExhibitRepository
import com.example.project1.museumroom.MuseumRoom
import com.example.project1.museumroom.MuseumRoomRepository
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.IOException


class AddExhibit : AppCompatActivity() {
    private lateinit var exhibitNameEditText: EditText
    private lateinit var exhibitDescriptionEditText: EditText
    private lateinit var exhibitCreatorEditText: EditText
    private lateinit var exhibitYearEditText: EditText
    private lateinit var exhibitRoomSpinner: Spinner
    private lateinit var buttonSaveExhibit: Button
    private lateinit var buttonChooseImage: Button

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
        setContentView(R.layout.activity_add_exhibit)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val myToolbar: Toolbar = findViewById(R.id.my_toolbar)
        setSupportActionBar(myToolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        exhibitNameEditText = findViewById(R.id.exhibitName)
        exhibitDescriptionEditText = findViewById(R.id.exhibitDescription)
        exhibitCreatorEditText = findViewById(R.id.exhibitCreator)
        exhibitYearEditText = findViewById(R.id.exhibitYear)
        exhibitRoomSpinner = findViewById(R.id.roomSpinner)
        buttonSaveExhibit = findViewById(R.id.buttonSaveExhibit)
        buttonChooseImage = findViewById(R.id.buttonСhooseImage)

        loadRooms()

        buttonChooseImage.setOnClickListener {
            openImageChooser()
        }

        buttonSaveExhibit.setOnClickListener {
            saveExhibit()
        }
    }

    private fun loadRooms() {
        val api = ApiClient.retrofit.create(MuseumRoomRepository::class.java)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = api.getMuseumRoom().execute()

                if (response.isSuccessful) {
                    val rooms = response.body() ?: emptyList()

                    withContext(Dispatchers.Main) {
                        val roomNumbers = rooms.map { it.room_number }
                        val adapter = ArrayAdapter(
                            this@AddExhibit,
                            android.R.layout.simple_spinner_item,
                            roomNumbers
                        )
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        exhibitRoomSpinner.adapter = adapter
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@AddExhibit,
                            "Ошибка загрузки комнат: ${response.code()} ${response.message()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@AddExhibit,
                        "Ошибка: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun saveExhibit() {
        val exhibitName = exhibitNameEditText.text.toString()
        val exhibitDescription = exhibitDescriptionEditText.text.toString()
        val exhibitCreator = exhibitCreatorEditText.text.toString()
        val exhibitYear = exhibitYearEditText.text.toString().toIntOrNull()
        val exhibitRoom = exhibitRoomSpinner.selectedItem.toString().toIntOrNull()

        if (exhibitName.isBlank() || exhibitDescription.isBlank() || exhibitYear == null || exhibitCreator.isBlank() || exhibitRoom == null) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
            return
        }

        val exhibitImage = selectedImageUri?.let { convertImageToBase64(it) } ?: ""

        val exhibit = Exhibit(
            exhibit_id = null,
            name = exhibitName,
            description = exhibitDescription,
            creation_year = exhibitYear,
            creator = exhibitCreator,
            room = exhibitRoom,
            image_upload = exhibitImage
        )

        val repository = ApiClient.retrofit.create(ExhibitRepository::class.java)
        val call = repository.addExhibit(exhibit)

        call.enqueue(object : Callback<Exhibit> {
            override fun onResponse(call: Call<Exhibit>, response: Response<Exhibit>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@AddExhibit, "Экспонат добавлен!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@AddExhibit, ExhibitList::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(
                        this@AddExhibit,
                        "Ошибка добавления: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<Exhibit>, t: Throwable) {
                Toast.makeText(this@AddExhibit, "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT)
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
            Log.d("SelectedImageUri", selectedImageUri.toString()) // Выводим URI в лог
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
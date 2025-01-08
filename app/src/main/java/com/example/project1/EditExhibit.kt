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
import java.io.File
import java.io.IOException


class EditExhibit : AppCompatActivity() {
    private lateinit var exhibitDescriptionEditText: EditText
    private lateinit var exhibitCreatorEditText: EditText
    private lateinit var exhibitYearEditText: EditText
    private lateinit var exhibitRoomSpinner: Spinner
    private lateinit var buttonSaveExhibit: Button
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
        setContentView(R.layout.edit_exhibit)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val myToolbar: Toolbar = findViewById(R.id.my_toolbar)
        setSupportActionBar(myToolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        exhibitDescriptionEditText = findViewById(R.id.exhibitDescription)
        exhibitCreatorEditText = findViewById(R.id.exhibitCreator)
        exhibitYearEditText = findViewById(R.id.exhibitYear)
        exhibitRoomSpinner = findViewById(R.id.roomSpinner)
        buttonSaveExhibit = findViewById(R.id.buttonSaveExhibit)
        buttonChooseImage = findViewById(R.id.buttonСhooseImage)

        val exhibitId = intent.getIntExtra("EXHIBIT_ID", -1)

        if (exhibitId != -1) {
            loadExhibitDetails(exhibitId)
        } else {
            Toast.makeText(this, "Ошибка: экспонат не найден", Toast.LENGTH_SHORT).show()
        }

        buttonChooseImage.setOnClickListener {
            openImageChooser()
        }

        buttonSaveExhibit.setOnClickListener {
            val exhibitName = myToolbar.title.toString()
            val exhibitDescription = exhibitDescriptionEditText.text.toString()
            val exhibitCreator = exhibitCreatorEditText.text.toString()
            val exhibitYear = exhibitYearEditText.text.toString()
            val exhibitRoom = exhibitRoomSpinner.selectedItem.toString().toIntOrNull()


            if (exhibitName.isBlank() || exhibitDescription.isBlank() || exhibitYear == null || exhibitCreator.isBlank() || exhibitRoom == null) {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val exhibitImage = selectedImageUri?.let { convertImageToBase64(it) } ?: ""

            val exhibitionImg = convertImagePathToUri(initialImage)

            val base64Image = exhibitionImg?.let { convertImageToBase64(it) }

            if (exhibitImage == "") {
                val exhibit = Exhibit(
                    exhibit_id = exhibitId,
                    name = exhibitName,
                    description = exhibitDescription,
                    creation_year = exhibitYear.toInt(),
                    creator = exhibitCreator,
                    room = exhibitRoom,
                    image_upload = base64Image
                )

                val exhibitJson = Gson().toJson(exhibit)
                Log.d("ExhibitJson", exhibitJson)

                updateExhibit(exhibit)
            } else {
                val exhibit = Exhibit(
                    exhibit_id = exhibitId,
                    name = exhibitName,
                    description = exhibitDescription,
                    creation_year = exhibitYear.toInt(),
                    creator = exhibitCreator,
                    room = exhibitRoom,
                    image_upload = exhibitImage
                )

                val exhibitJson = Gson().toJson(exhibit)
                Log.d("ExhibitJson", exhibitJson)

                updateExhibit(exhibit)
            }


        }
    }

    private fun loadRooms(exhibitRoomId: Int) {
        val api = ApiClient.retrofit.create(MuseumRoomRepository::class.java)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = api.getMuseumRoom().execute()

                if (response.isSuccessful) {
                    val rooms = response.body() ?: emptyList()

                    withContext(Dispatchers.Main) {
                        val roomNumbers = rooms.map { it.room_number }
                        val adapter = ArrayAdapter(
                            this@EditExhibit,
                            android.R.layout.simple_spinner_item,
                            roomNumbers
                        )
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        exhibitRoomSpinner.adapter = adapter

                        setRoomSelectionById(exhibitRoomId, rooms)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@EditExhibit,
                            "Ошибка загрузки комнат: ${response.code()} ${response.message()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@EditExhibit,
                        "Ошибка: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun setRoomSelectionById(exhibitRoomId: Int, rooms: List<MuseumRoom>) {
        val room = rooms.find { it.room_id == exhibitRoomId }
        room?.let {
            val position =
                (exhibitRoomSpinner.adapter as ArrayAdapter<String>).getPosition(it.room_number)
            if (position >= 0) {
                exhibitRoomSpinner.setSelection(position)
            }
        }
    }


    @SuppressLint("SuspiciousIndentation")
    private fun loadExhibitDetails(exhibitId: Int) {
        val myToolbar: Toolbar = findViewById(R.id.my_toolbar)
        val api = ApiClient.retrofit.create(ExhibitRepository::class.java)
        val baseUrl = "http://10.0.2.2:8000"


        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = api.getExhibitDetails(exhibitId).execute()

                if (response.isSuccessful) {
                    val exhibit = response.body()

                    withContext(Dispatchers.Main) {
                        exhibit?.let {
                            myToolbar.setTitle(it.name)
                            exhibitDescriptionEditText.setText(it.description)
                            exhibitYearEditText.setText(it.creation_year.toString())
                            exhibitCreatorEditText.setText(it.creator)
                            initialImage = it.image.toString()

                            loadRooms(it.room)

                        } ?: run {
                            Toast.makeText(
                                this@EditExhibit,
                                "Детали экспоната не найдены",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@EditExhibit,
                            "Ошибка загрузки данных: ${response.code()} ${response.message()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@EditExhibit,
                        "Ошибка: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }


        }
    }

    private fun updateExhibit(exhibit: Exhibit) {
        Log.d(
            "UpdateExhibit",
            "Отправка запроса на сервер для экспоната с ID: ${exhibit.exhibit_id}"
        )
        val repository = ApiClient.retrofit.create(ExhibitRepository::class.java)

        val call = repository.updateExhibit(exhibit.exhibit_id!!, exhibit)

        call.enqueue(object : Callback<Exhibit> {
            override fun onResponse(call: Call<Exhibit>, response: Response<Exhibit>) {
                if (response.isSuccessful) {
                    Toast.makeText(
                        this@EditExhibit,
                        "Данные обновлены успешно!",
                        Toast.LENGTH_SHORT
                    ).show()
                    val intent = Intent(this@EditExhibit, ExhibitDetails::class.java)

                    intent.putExtra(
                        "EXHIBIT_ID",
                        exhibit.exhibit_id
                    )
                    startActivity(intent)
                } else {
                    Toast.makeText(
                        this@EditExhibit,
                        "Ошибка обновления: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<Exhibit>, t: Throwable) {
                Toast.makeText(this@EditExhibit, "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    private fun openImageChooser() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "image/*" // Фильтр для выбора только изображений
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
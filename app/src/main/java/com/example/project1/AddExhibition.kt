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
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Toast
import com.example.project1.exhibition.Exhibition
import com.example.project1.exhibition.ExhibitionRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.IOException


class AddExhibition : AppCompatActivity() {
    private lateinit var exhibitionStartDate: EditText
    private lateinit var exhibitionEndDate: EditText
    private lateinit var exhibitionResponsiblePerson: EditText
    private lateinit var buttonSaveExhibition: Button
    private lateinit var buttonChooseImage: Button
    private lateinit var exhibitionNameEditText: EditText
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
        setContentView(R.layout.activity_add_exhibition)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val myToolbar: Toolbar = findViewById(R.id.my_toolbar)
        setSupportActionBar(myToolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        exhibitionNameEditText = findViewById(R.id.exhibitionName)
        exhibitionStartDate = findViewById(R.id.exhibitionStartDate)
        exhibitionEndDate = findViewById(R.id.exhibitionEndDate)
        exhibitionResponsiblePerson = findViewById(R.id.exhibitionResponsiblePerson)
        buttonSaveExhibition = findViewById(R.id.buttonSaveExhibition)
        buttonChooseImage = findViewById(R.id.buttonСhooseImage)

        buttonChooseImage.setOnClickListener {
            openImageChooser()
        }

        buttonSaveExhibition.setOnClickListener {
            saveExhibition()
        }
    }

    private fun saveExhibition() {
        val exhibitionName = exhibitionNameEditText.text.toString()
        val exhibitionStartDate = exhibitionStartDate.text.toString()
        val exhibitionEndDate = exhibitionEndDate.text.toString()
        val exhibitionResponsiblePerson = exhibitionResponsiblePerson.text.toString()


        if (exhibitionName.isBlank() || exhibitionStartDate.isBlank() || exhibitionEndDate.isBlank() || exhibitionResponsiblePerson.isBlank()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
            return
        }

        val exhibitionImage = selectedImageUri?.let { convertImageToBase64(it) } ?: ""

        val exhibition = Exhibition(
            exhibition_id = null,
            name = exhibitionName,
            start_date = exhibitionStartDate,
            end_date = exhibitionEndDate,
            responsible_person = exhibitionResponsiblePerson,
            image_upload = exhibitionImage,
        )

        val repository = ApiClient.retrofit.create(ExhibitionRepository::class.java)
        val call = repository.addExhibition(exhibition)

        call.enqueue(object : Callback<Exhibition> {
            override fun onResponse(call: Call<Exhibition>, response: Response<Exhibition>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@AddExhibition, "Экспонат добавлен!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@AddExhibition, ExhibitionList::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(
                        this@AddExhibition,
                        "Ошибка добавления: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<Exhibition>, t: Throwable) {
                Toast.makeText(this@AddExhibition, "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT)
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



package com.example.project1

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.project1.exhibit.ExhibitRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ExhibitDetails : AppCompatActivity() {
    private lateinit var exhibitDescriptionTextView: TextView
    private lateinit var exhibitCreatorTextView: TextView
    private lateinit var exhibitYearTextView: TextView
    private lateinit var exhibitRoomTextView: TextView
    private lateinit var exhibitImageView: ImageView
    private lateinit var exhibitEditButton: Button

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        enableEdgeToEdge()
        setContentView(R.layout.exhibit_details)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val myToolbar: Toolbar = findViewById(R.id.my_toolbar)
        setSupportActionBar(myToolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        exhibitDescriptionTextView = findViewById(R.id.exhibitDescription)
        exhibitCreatorTextView = findViewById(R.id.exhibitCreator)
        exhibitYearTextView = findViewById(R.id.exhibitYear)
        exhibitRoomTextView = findViewById(R.id.exhibitRoom)
        exhibitImageView = findViewById(R.id.exhibitImage)
        exhibitEditButton = findViewById(R.id.buttonEditExhibit)

        val exhibitId = intent.getIntExtra("EXHIBIT_ID", -1)
        val exhibitRoomId = intent.getIntExtra("EXHIBIT_ROOM", -1) // Если room - ID (число)

        if (exhibitId != -1) {
            loadExhibitDetails(exhibitId)
        } else {
            Toast.makeText(this, "Ошибка: экспонат не найден", Toast.LENGTH_SHORT).show()
        }

        exhibitEditButton.setOnClickListener{
            val intent = Intent(this, EditExhibit::class.java)
            intent.putExtra("EXHIBIT_ID", exhibitId)
            intent.putExtra("EXHIBIT_ROOM", exhibitRoomId)
            startActivity(intent)
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
                            exhibitDescriptionTextView.text = it.description
                            exhibitCreatorTextView.text = it.creator
                            exhibitYearTextView.text = it.creation_year.toString()
                            exhibitRoomTextView.text = it.room.toString()
                            Glide.with(this@ExhibitDetails)
                                .load(baseUrl + it.image)
                                .into(exhibitImageView)
                        } ?: run {
                            Toast.makeText(
                                this@ExhibitDetails,
                                "Детали экспоната не найдены",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@ExhibitDetails,
                            "Ошибка загрузки данных: ${response.code()} ${response.message()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ExhibitDetails,
                        "Ошибка: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
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
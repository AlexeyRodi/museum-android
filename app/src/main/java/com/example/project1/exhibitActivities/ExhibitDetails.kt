package com.example.project1.exhibitActivities

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
import com.example.project1.ApiClient
import com.example.project1.MainList
import com.example.project1.R
import com.example.project1.exhibit.ExhibitRepository
import com.example.project1.museumroom.MuseumRoomRepository
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
    private lateinit var exhibitDeleteButton: Button

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
        exhibitDeleteButton = findViewById(R.id.buttonDeleteExhibit)

        val exhibitId = intent.getIntExtra("EXHIBIT_ID", -1)
        val exhibitRoomId = intent.getIntExtra("EXHIBIT_ROOM", -1)

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

        exhibitDeleteButton.setOnClickListener {
            deleteExhibit(exhibitId)
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
                            loadRoomNumber(it.room) { roomNumber ->
                                exhibitRoomTextView.text = roomNumber
                            }
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

    private fun loadRoomNumber(roomId:Int, onRoomLoaded: (String) -> Unit){
        val api = ApiClient.retrofit.create(MuseumRoomRepository::class.java)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = api.getMuseumRoom().execute()

                if (response.isSuccessful) {
                    val rooms = response.body() ?: emptyList()
                    val room = rooms.find { it.room_id == roomId }

                    withContext(Dispatchers.Main) {
                        if (room != null) {
                            onRoomLoaded(room.room_number)
                        } else {
                            exhibitRoomTextView.text = "Комната не найдена"
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@ExhibitDetails,
                            "Ошибка загрузки комнат: ${response.code()} ${response.message()}",
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

    private fun deleteExhibit(exhibitId: Int) {
        val api = ApiClient.retrofit.create(ExhibitRepository::class.java)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = api.deleteExhibit(exhibitId).execute()
                if (response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@ExhibitDetails,
                            "Экспонат успешно удалён",
                            Toast.LENGTH_SHORT
                        ).show()
                        val intent = Intent(this@ExhibitDetails, MainList::class.java)
                        startActivity(intent)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@ExhibitDetails,
                            "Ошибка удаления: ${response.code()} ${response.message()}",
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
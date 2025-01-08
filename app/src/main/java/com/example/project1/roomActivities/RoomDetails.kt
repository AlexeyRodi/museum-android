package com.example.project1.roomActivities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.project1.ApiClient
import com.example.project1.MainList
import com.example.project1.R
import com.example.project1.exhibitActivities.ExhibitDetails
import com.example.project1.museumroom.MuseumRoomRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RoomDetails : AppCompatActivity() {

    private lateinit var roomDescriptionTextView: TextView
    private lateinit var buttonsContainer: LinearLayout
    private lateinit var EditRoomButton: Button
    private lateinit var DeleteRoomButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        enableEdgeToEdge()
        setContentView(R.layout.activity_room_details)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val toolbar: Toolbar = findViewById(R.id.my_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val roomId = intent.getIntExtra("ROOM_ID", -1)
        if (roomId == -1) {
            Toast.makeText(this, "Ошибка: идентификатор комнаты не найден", Toast.LENGTH_SHORT)
                .show()
            finish()
            return
        }

        buttonsContainer = findViewById(R.id.buttonsContainer)
        roomDescriptionTextView = findViewById(R.id.roomDescription)
        EditRoomButton = findViewById(R.id.buttonEditRoom)
        DeleteRoomButton = findViewById(R.id.buttonDeleteRoom)

        EditRoomButton.setOnClickListener {
            val intent = Intent(this, EditRoom::class.java)
            intent.putExtra("ROOM_ID", roomId)
            startActivity(intent)
        }

        DeleteRoomButton.setOnClickListener {
            deleteRoom(roomId)
        }

        CoroutineScope(Dispatchers.IO).launch {
            val api = ApiClient.retrofit.create(MuseumRoomRepository::class.java)
            try {
                val response = api.getExhibitsByRoomId(roomId)
                    .execute()
                if (response.isSuccessful) {
                    val exhibits = response.body() ?: emptyList()
                    withContext(Dispatchers.Main) {
                        for (exhibit in exhibits) {
                            val button = createExhibitButton(exhibit.name, exhibit.exhibit_id!!)
                            buttonsContainer.addView(button)
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@RoomDetails,
                            "Ошибка загрузки комнат: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@RoomDetails, "Ошибка: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
        loadRoomDetails(roomId)

    }

    private fun loadRoomDetails(roomId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val api = ApiClient.retrofit.create(MuseumRoomRepository::class.java)
            try {
                val response = api.getRoomDetails(roomId).execute()
                if (response.isSuccessful) {
                    val roomDetails = response.body()
                    withContext(Dispatchers.Main) {
                        if (roomDetails != null) {
                            supportActionBar?.title = "Комната ${roomDetails.room_number}"
                            roomDescriptionTextView.text = roomDetails.description
                        } else {
                            Toast.makeText(
                                this@RoomDetails,
                                "Детали комнаты не найдены",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@RoomDetails,
                            "Ошибка загрузки комнаты: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@RoomDetails, "Ошибка: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun createExhibitButton(exhibitName: String, exhibitId: Int): Button {
        return Button(this).apply {
            text = exhibitName
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 16)
            }
            setTextColor(resources.getColor(android.R.color.white, null))
            background = resources.getDrawable(R.drawable.button_bg, null)
            setPadding(16, 16, 16, 16)
            setAllCaps(false)
            val typeface = ResourcesCompat.getFont(context, R.font.inter_light)
            setTypeface(typeface)

            setOnClickListener {
                val intent = Intent(this@RoomDetails, ExhibitDetails::class.java)

                intent.putExtra("EXHIBIT_ID", exhibitId)
                startActivity(intent)

                Toast.makeText(
                    this@RoomDetails,
                    "Выбран экспонат: ${exhibitName}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun deleteRoom(roomId: Int) {
        val api = ApiClient.retrofit.create(MuseumRoomRepository::class.java)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = api.deleteRoom(roomId).execute()
                if (response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@RoomDetails,
                            "Комната успешно удалена",
                            Toast.LENGTH_SHORT
                        ).show()
                        val intent = Intent(this@RoomDetails, MainList::class.java)
                        startActivity(intent)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@RoomDetails,
                            "Ошибка удаления: ${response.code()} ${response.message()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@RoomDetails,
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

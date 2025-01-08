package com.example.project1.roomActivities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.project1.ApiClient
import com.example.project1.R
import com.example.project1.museumroom.MuseumRoomRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class RoomList : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        enableEdgeToEdge()
        setContentView(R.layout.room_list)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val myToolbar: Toolbar = findViewById(R.id.my_toolbar)
        setSupportActionBar(myToolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val buttonsContainer: LinearLayout = findViewById(R.id.buttonsContainer)
        val buttonAddRoom: Button = findViewById(R.id.buttonAddRoom)

        val api = ApiClient.retrofit.create(MuseumRoomRepository::class.java)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = api.getMuseumRoom().execute()

                if (response.isSuccessful) {
                    val rooms = response.body() ?: emptyList()

                    withContext(Dispatchers.Main) {
                        for (room in rooms) {
                            val button = Button(this@RoomList).apply {
                                text = "Комната ${room.room_number}"
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
                            }
                            button.setOnClickListener {
                                Toast.makeText(
                                    this@RoomList,
                                    "Выбрана комната: ${room.room_id}",
                                    Toast.LENGTH_SHORT
                                ).show()

                                val intent = Intent(this@RoomList, RoomDetails::class.java)
                                intent.putExtra("ROOM_ID", room.room_id)
                                intent.putExtra("room_description", room.description)
                                startActivity(intent)
                            }
                            buttonsContainer.addView(button)
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@RoomList,
                            "Ошибка: ${response.code()} ${response.message()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@RoomList,
                        "Ошибка загрузки комнат: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        buttonAddRoom.setOnClickListener {
            val intent = Intent(this@RoomList, AddRoom::class.java)
            startActivity(intent)
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
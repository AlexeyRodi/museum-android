package com.example.project1

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
import com.example.project1.museumroom.MuseumRoomRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RoomDetails : AppCompatActivity() {

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

        val roomNumber = intent.getIntExtra("room_number", -1)
        val buttonsContainer: LinearLayout = findViewById(R.id.buttonsContainer)

        if (roomNumber == -1) {
            Toast.makeText(this, "Ошибка: номер комнаты не найден", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            CoroutineScope(Dispatchers.IO).launch {
                val api = ApiClient.retrofit.create(MuseumRoomRepository::class.java)
                try {
                    val response = api.getExhibitsByRoomNumber(roomNumber).execute()

                    if (response.isSuccessful) {
                        val exhibits = response.body() ?: emptyList()

                        withContext(Dispatchers.Main) {
                            for (exhibit in exhibits) {
                                val button = Button(this@RoomDetails).apply {
                                    text = exhibit.name
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
                                    val intent = Intent(this@RoomDetails, ExhibitDetails::class.java)

                                    intent.putExtra(
                                        "EXHIBIT_ID",
                                        exhibit.exhibit_id
                                    )
                                    startActivity(intent)

                                    Toast.makeText(
                                        this@RoomDetails,
                                        "Выбран экспонат: ${exhibit.name}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                buttonsContainer.addView(button)
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@RoomDetails,
                                "Ошибка загрузки экспонатов: ${response.code()}",
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

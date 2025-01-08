package com.example.project1

import android.annotation.SuppressLint
import android.content.Intent
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
import com.example.project1.museumroom.MuseumRoom
import com.example.project1.museumroom.MuseumRoomRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class EditRoom : AppCompatActivity() {
    private lateinit var roomNumberEditText: EditText
    private lateinit var roomDescriptionEditText: EditText
    private lateinit var buttonSaveRoom: Button

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_room)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val myToolbar: Toolbar = findViewById(R.id.my_toolbar)
        setSupportActionBar(myToolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val roomId = intent.getIntExtra("ROOM_ID", -1)
        if (roomId != -1) {
            loadRoomDetails(roomId)
        } else {
            Toast.makeText(this, "Ошибка: комната не найдена", Toast.LENGTH_SHORT).show()
        }

        roomNumberEditText = findViewById(R.id.roomNumber)
        roomDescriptionEditText = findViewById(R.id.roomDescription)
        buttonSaveRoom = findViewById(R.id.buttonSaveRoom)

        buttonSaveRoom.setOnClickListener {
            val roomNumber = roomNumberEditText.text.toString()
            val roomDescription = roomDescriptionEditText.text.toString()


            if (roomNumber.isBlank() || roomDescription.isBlank()) {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val room = MuseumRoom(
                room_id = roomId,
                room_number = roomNumber,
                description = roomDescription
            )

            updateRoom(room)
        }
    }

    private fun loadRoomDetails(roomId: Int) {
        val myToolbar: Toolbar = findViewById(R.id.my_toolbar)

        CoroutineScope(Dispatchers.IO).launch {
            val api = ApiClient.retrofit.create(MuseumRoomRepository::class.java)
            try {
                val response = api.getRoomDetails(roomId).execute()
                if (response.isSuccessful) {
                    val roomDetails = response.body()

                    withContext(Dispatchers.Main) {
                        if (roomDetails != null) {
                            roomDetails?.let {
                                myToolbar.setTitle("Комната ${it.room_number}")
                                roomNumberEditText.setText(it.room_number)
                                roomDescriptionEditText.setText(it.description)
                            }
                        } else {
                            Toast.makeText(
                                this@EditRoom,
                                "Детали комнаты не найдены",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@EditRoom,
                            "Ошибка загрузки комнаты: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EditRoom, "Ошибка: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun updateRoom(room: MuseumRoom) {
        val repository = ApiClient.retrofit.create(MuseumRoomRepository::class.java)

        val call = repository.updateRoom(room.room_id!!, room)

        call.enqueue(object : Callback<MuseumRoom> {
            override fun onResponse(call: Call<MuseumRoom>, response: Response<MuseumRoom>) {
                if (response.isSuccessful) {
                    Toast.makeText(
                        this@EditRoom,
                        "Данные обновлены успешно!",
                        Toast.LENGTH_SHORT
                    ).show()
                    val intent = Intent(this@EditRoom, RoomDetails::class.java)

                    intent.putExtra(
                        "ROOM_ID",
                        room.room_id
                    )
                    startActivity(intent)
                } else {
                    Toast.makeText(
                        this@EditRoom,
                        "Ошибка обновления: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<MuseumRoom>, t: Throwable) {
                Toast.makeText(this@EditRoom, "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        })
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






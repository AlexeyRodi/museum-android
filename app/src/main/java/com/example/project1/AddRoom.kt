package com.example.project1

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
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
import com.example.project1.museumroom.MuseumRoom
import com.example.project1.museumroom.MuseumRoomRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class AddRoom : AppCompatActivity() {
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
        setContentView(R.layout.activity_add_room)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val myToolbar: Toolbar = findViewById(R.id.my_toolbar)
        setSupportActionBar(myToolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        roomNumberEditText = findViewById(R.id.roomNumber)
        roomDescriptionEditText = findViewById(R.id.roomDescription)
        buttonSaveRoom = findViewById(R.id.buttonSaveRoom)

        buttonSaveRoom.setOnClickListener {
            saveRoom()
        }
    }

    private fun saveRoom() {
        val roomNumber = roomNumberEditText.text.toString()
        val roomDescription = roomDescriptionEditText.text.toString()


        if (roomNumber.isBlank() || roomDescription.isBlank()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
            return
        }

        val room = MuseumRoom(
            room_id = null,
            room_number = roomNumber,
            description = roomDescription
        )

        val repository = ApiClient.retrofit.create(MuseumRoomRepository::class.java)
        val call = repository.addMuseumRoom(room)

        call.enqueue(object : Callback<MuseumRoom> {
            override fun onResponse(call: Call<MuseumRoom>, response: Response<MuseumRoom>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@AddRoom, "Комната добавлена!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@AddRoom, MainList::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(
                        this@AddRoom,
                        "Ошибка добавления: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<MuseumRoom>, t: Throwable) {
                Toast.makeText(this@AddRoom, "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT)
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



package com.example.project1

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MenuItem
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.project1.exhibition.ExhibitionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity7 : AppCompatActivity() {
    private lateinit var exhibitionStartDateTextView: TextView
    private lateinit var exhibitionEndDateTextView: TextView
    private lateinit var exhibitionCountryTextView: TextView
    private lateinit var exhibitionCityTextView: TextView
    private lateinit var exhibitionPlaceTextView: TextView
    private lateinit var exhibitionImageView: ImageView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        enableEdgeToEdge()
        setContentView(R.layout.activity_main7)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val myToolbar: Toolbar = findViewById(R.id.my_toolbar7)
        setSupportActionBar(myToolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        exhibitionStartDateTextView = findViewById(R.id.exhibitionStartDate)
        exhibitionEndDateTextView = findViewById(R.id.exhibitionEndDate)
        exhibitionCountryTextView = findViewById(R.id.exhibitionCountry)
        exhibitionCityTextView = findViewById(R.id.exhibitionCity)
        exhibitionPlaceTextView = findViewById(R.id.exhibitionPlace)
        exhibitionImageView = findViewById(R.id.exhibitionImage)

        val exhibitionId = intent.getIntExtra("EXHIBITION_ID", -1)

        if (exhibitionId != -1) {
            loadExhibitionDetails(exhibitionId)
        } else {
            Toast.makeText(this, "Ошибка: выставка не найдена", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("SuspiciousIndentation")
    private fun loadExhibitionDetails(exhibitionId: Int) {
        val myToolbar: Toolbar = findViewById(R.id.my_toolbar7)
        val api = ApiClient.retrofit.create(ExhibitionRepository::class.java)
        val baseUrl = "http://10.0.2.2:8000"


        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = api.getExhibitionDetail(exhibitionId).execute()

                if (response.isSuccessful) {
                    val exhibition = response.body()

                    withContext(Dispatchers.Main) {
                        exhibition?.let {
                            myToolbar.setTitle(it.name)
                            exhibitionStartDateTextView.text = it.start_date
                            exhibitionEndDateTextView.text = it.end_date
                            exhibitionCountryTextView.text = it.country
                            exhibitionCityTextView.text = it.country
                            exhibitionPlaceTextView.text = it.venue
                            Glide.with(this@MainActivity7)
                                .load(baseUrl + it.image)
                                .into(exhibitionImageView)
                        } ?: run {
                            Toast.makeText(
                                this@MainActivity7,
                                "Детали выставки не найдены",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@MainActivity7,
                            "Ошибка загрузки данных: ${response.code()} ${response.message()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity7,
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
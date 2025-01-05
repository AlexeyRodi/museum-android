package com.example.project1

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
import com.example.project1.exhibition.ExhibitionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity3 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        enableEdgeToEdge()
        setContentView(R.layout.activity_main3)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val myToolbar: Toolbar = findViewById(R.id.my_toolbar2)
        setSupportActionBar(myToolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val buttonsContainer: LinearLayout = findViewById(R.id.buttonsContainer)

        val api = ApiClient.retrofit.create(ExhibitionRepository::class.java)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = api.getExhibitions().execute()

                if (response.isSuccessful) {
                    val exhibitions = response.body() ?: emptyList()

                    withContext(Dispatchers.Main) {
                        // Update UI with the exhibitions
                        for (exhibition in exhibitions) {
                            val button = Button(this@MainActivity3).apply {
                                text = exhibition.name
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
                                    this@MainActivity3,
                                    "Выбрана выставка: ${exhibition.name}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            buttonsContainer.addView(button)
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@MainActivity3,
                            "Ошибка: ${response.code()} ${response.message()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity3,
                        "Ошибка загрузки выставок: ${e.message}",
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
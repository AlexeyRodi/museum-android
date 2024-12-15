package com.example.project1

import android.os.Bundle
import android.view.MenuItem
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.Observer
import com.example.project1.exhibition.ExhibitionViewModel
import com.example.project1.exhibition.ExhibitionViewModelFactory
import com.example.project1.exhibition.FakeExhibitionRepository

class MainActivity3 : AppCompatActivity() {
    private lateinit var userViewModel: ExhibitionViewModel
    private lateinit var buttonsContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.getWindow().setFlags(
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

        val buttonsContainer = findViewById<LinearLayout>(R.id.buttonsContainer)

        val exhibitionRepository = FakeExhibitionRepository()
        val factory = ExhibitionViewModelFactory(exhibitionRepository)
        val exhibitionViewModel =
            ViewModelProvider(this, factory).get(ExhibitionViewModel::class.java)

        exhibitionViewModel.exhibitionNames.observe(this, Observer { names ->
            buttonsContainer.removeAllViews()

            names.forEach { name ->
                val button = Button(this).apply {
                    text = name
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(0, 0, 0, 16)
                    }
                    setTextColor(resources.getColor(android.R.color.white))
                    background = resources.getDrawable(R.drawable.button_bg, null)
                    setPadding(16, 16, 16, 16)
                    setAllCaps(false)
                    val typeface = ResourcesCompat.getFont(context, R.font.inter_light)
                    setTypeface(typeface)
                }
                buttonsContainer.addView(button)
            }
        })

        exhibitionViewModel.loadExhibitionsNames()


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
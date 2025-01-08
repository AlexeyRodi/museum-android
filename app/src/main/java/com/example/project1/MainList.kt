package com.example.project1

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.project1.exhibitActivities.ExhibitList
import com.example.project1.exhibitionActivities.ExhibitionList
import com.example.project1.roomActivities.RoomList

class MainList : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        enableEdgeToEdge()
        setContentView(R.layout.main_list)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val myToolbar: Toolbar = findViewById(R.id.my_toolbar)
        setSupportActionBar(myToolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val buttonExhibitions: Button = findViewById(R.id.buttonExhibitions)
        val buttonMuseumRooms: Button = findViewById(R.id.buttonMuseums_Rooms)
        val buttonExhibits: Button = findViewById(R.id.buttonExhibit)


        buttonExhibitions.setOnClickListener {
            val intent = Intent(this, ExhibitionList::class.java)
            startActivity(intent)
        }

        buttonExhibits.setOnClickListener {
            val intent = Intent(this, ExhibitList::class.java)
            startActivity(intent)
        }

        buttonMuseumRooms.setOnClickListener {
            val intent = Intent(this, RoomList::class.java)
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
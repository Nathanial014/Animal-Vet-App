package com.example.animals_vet_app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<View>(R.id.root).applySystemBarsPadding()

        findViewById<Button>(R.id.viewAnimalsButton).setOnClickListener {
            startActivity(Intent(this, AnimalListActivity::class.java))
        }

        findViewById<Button>(R.id.addAnimalButton).setOnClickListener {
            startActivity(Intent(this, AnimalEditActivity::class.java))
        }
    }
}


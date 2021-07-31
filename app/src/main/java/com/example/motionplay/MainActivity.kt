package com.example.motionplay

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.motionplay.ui.main.Play1Fragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, Play1Fragment.newInstance())
                .commitNow()
        }
    }
}
package com.lightspeed.app.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.lightspeed.app.R
import com.lightspeed.app.domain.Data

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Data.init(applicationContext)
        setContentView(R.layout.activity_main)
    }
}
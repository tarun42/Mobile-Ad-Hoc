package com.manet.mobile_ad_hoc

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.manet.mobile_ad_hoc.connection.Server

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Server.startServer(application)
    }
}
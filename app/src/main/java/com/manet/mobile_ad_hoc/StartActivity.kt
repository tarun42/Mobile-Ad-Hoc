package com.manet.mobile_ad_hoc

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class StartActivity : AppCompatActivity() {
    var ScanUid: Button? = null
    var UID: EditText? = null
    public var UserID : String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
        ScanUid = findViewById(R.id.ScanUid)
        UID = findViewById(R.id.UID)
        val intent = Intent(this, MainActivity::class.java)
        ScanUid!!.setOnClickListener {
            val data: String
            data = UID!!.text.toString()
            if (data.length == 0 || data.contains(" ") || data.contains("\\")) Toast.makeText(
                applicationContext, "Please enter a valid ID", Toast.LENGTH_LONG
            ).show() else {
                // Save data saomewhewe
                Toast.makeText(applicationContext, "User ID: $data", Toast.LENGTH_LONG).show()
                // make intent call;
                startActivity(intent)
            }
        }
    }
}





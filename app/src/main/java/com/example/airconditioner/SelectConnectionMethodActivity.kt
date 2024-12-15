package com.example.airconditoner

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class SelectConnectionMethodActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.select_connection_method)

        findViewById<Button>(R.id.btnConnectWifi).setOnClickListener {
            val intent = Intent(this, ConnectEspActivity::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnConnectBluetooth).setOnClickListener {
            val intent = Intent(this, ConnectBluetoothActivity::class.java)
            startActivity(intent)
        }
    }
}

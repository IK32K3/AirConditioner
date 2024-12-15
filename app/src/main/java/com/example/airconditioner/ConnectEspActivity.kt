package com.example.airconditoner

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import java.io.IOException

class ConnectEspActivity : AppCompatActivity() {
    private lateinit var preferencesHelper: PreferencesHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connect_esp)

        preferencesHelper = PreferencesHelper(this)

        val etEspIp = findViewById<EditText>(R.id.etEspIp)
        val btnConnect = findViewById<Button>(R.id.btnConnect)

        btnConnect.setOnClickListener {
            val ipAddress = etEspIp.text.toString().trim()
            if (ipAddress.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập địa chỉ IP", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            testConnectionToEsp(ipAddress) { success ->
                runOnUiThread {
                    if (success) {
                        preferencesHelper.setEsp32Ip(ipAddress)
                        Toast.makeText(this, "Kết nối thành công!", Toast.LENGTH_SHORT).show()

                        // Chuyển sang giao diện chọn điều hòa
                        val intent = Intent(this, AcListActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Kết nối thất bại. Vui lòng kiểm tra IP.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun testConnectionToEsp(ip: String, callback: (Boolean) -> Unit) {
        val url = "http://$ip/test_connection" // API test_connection trên ESP32
        val client = OkHttpClient()
        val request = Request.Builder().url(url).get().build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                callback(false)
            }

            override fun onResponse(call: Call, response: Response) {
                callback(response.isSuccessful)
            }
        })
    }
}
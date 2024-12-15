package com.example.airconditoner

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AcListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ac_list)

        val preferencesHelper = PreferencesHelper(this)
        val esp32Ip = preferencesHelper.getEsp32Ip()

        // Nếu chưa có địa chỉ ESP32, quay lại màn hình kết nối
        if (esp32Ip == null) {
            Toast.makeText(this, "Vui lòng kết nối với ESP32 trước!", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, ConnectEspActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        // Hiển thị danh sách điều hòa
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val acList = listOf("Mitsubishi", "Daikin", "Panasonic")
        val adapter = AcListAdapter(acList) { selectedAc ->
            val intent = Intent(this, AcControlActivity::class.java)
            intent.putExtra("selectedAc", selectedAc)
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        val btnCreateCustomAc = findViewById<Button>(R.id.btnCreateCustomAc)
        btnCreateCustomAc.setOnClickListener {
            val intent = Intent(this, CreateCustomAcActivity::class.java)
            startActivity(intent)
        }
    }
}
package com.example.airconditoner

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.Callback
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException

class CreateCustomAcActivity : AppCompatActivity() {

    private val customAcCommands = mutableMapOf<String, String>()
    private lateinit var tvInstructions: TextView
    private lateinit var btnFinish: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_custom_ac)

        tvInstructions = findViewById(R.id.tvInstructions)
        btnFinish = findViewById(R.id.btnFinish)

        // Học lệnh bật
        findViewById<Button>(R.id.btnLearnPowerOn).setOnClickListener {
            learnCommand("power_on", "Học lệnh bật. Nhấn nút bật trên điều khiển.")
        }

        // Học lệnh tắt
        findViewById<Button>(R.id.btnLearnPowerOff).setOnClickListener {
            learnCommand("power_off", "Học lệnh tắt. Nhấn nút tắt trên điều khiển.")
        }

        // Học lệnh tăng nhiệt độ
        findViewById<Button>(R.id.btnLearnTempUp).setOnClickListener {
            learnCommand("temp_up", "Học lệnh tăng nhiệt độ. Nhấn nút tăng nhiệt độ trên điều khiển.")
        }

        // Học lệnh giảm nhiệt độ
        findViewById<Button>(R.id.btnLearnTempDown).setOnClickListener {
            learnCommand("temp_down", "Học lệnh giảm nhiệt độ. Nhấn nút giảm nhiệt độ trên điều khiển.")
        }

        // Hoàn tất
        btnFinish.setOnClickListener {
            finishCustomAcSetup()
        }
    }

    private fun learnCommand(commandKey: String, instructions: String) {
        tvInstructions.text = instructions

        val url = "http://<ESP32_IP_ADDRESS>/learn"
        val client = OkHttpClient()
        val request = Request.Builder().url(url).get().build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@CreateCustomAcActivity, "Học lệnh thất bại!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val commandData = response.body?.string()
                if (response.isSuccessful && !commandData.isNullOrEmpty()) {
                    customAcCommands[commandKey] = commandData
                    runOnUiThread {
                        Toast.makeText(this@CreateCustomAcActivity, "Học lệnh thành công!", Toast.LENGTH_SHORT).show()
                        checkCompletion()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@CreateCustomAcActivity, "ESP32 không phản hồi!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun checkCompletion() {
        if (customAcCommands.size == 4) {
            tvInstructions.text = "Học lệnh hoàn tất! Nhấn 'Hoàn tất' để lưu."
            btnFinish.visibility = View.VISIBLE
        }
    }

    private fun finishCustomAcSetup() {
        val acName = "Điều hòa tùy chỉnh"
        saveCustomAc(acName, customAcCommands)

        Toast.makeText(this, "Điều hòa tùy chỉnh đã được lưu!", Toast.LENGTH_SHORT).show()
        finish() // Quay lại danh sách điều hòa
    }

    private fun saveCustomAc(name: String, commands: Map<String, String>) {
        val sharedPreferences = getSharedPreferences("CustomAcs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        val acJson = JSONObject()
        acJson.put("name", name)
        commands.forEach { (key, value) ->
            acJson.put(key, value)
        }

        val acList = sharedPreferences.getStringSet("acs", mutableSetOf()) ?: mutableSetOf()
        acList.add(acJson.toString())

        editor.putStringSet("acs", acList)
        editor.apply()
    }
}
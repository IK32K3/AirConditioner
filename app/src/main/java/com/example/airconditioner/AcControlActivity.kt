package com.example.airconditoner

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import java.io.IOException
import java.io.OutputStream
import java.util.*

class AcControlActivity : AppCompatActivity() {
    private lateinit var selectedAc: String
    private var currentTemperature = 24  // Nhiệt độ mặc định
    private lateinit var esp32Ip: String
    private var isBluetooth = false
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ac_control)

        val preferencesHelper = PreferencesHelper(this)
        esp32Ip = preferencesHelper.getEsp32Ip() ?: ""
        isBluetooth = intent.getBooleanExtra("isBluetooth", false)

        selectedAc = intent.getStringExtra("selectedAc") ?: "Unknown"
        findViewById<TextView>(R.id.tvSelectedAc).text = "Điều khiển điều hòa $selectedAc"
        val tvTemperature = findViewById<TextView>(R.id.tvTemperature)

        // Nút "Quay lại"
        findViewById<Button>(R.id.btnBack).setOnClickListener { finish() }

        // Cập nhật nhiệt độ hiển thị
        tvTemperature.text = "Nhiệt độ: $currentTemperature°C"

        // Nút tăng/giảm nhiệt độ
        findViewById<Button>(R.id.btnIncreaseTemp).setOnClickListener {
            if (currentTemperature < 30) {
                currentTemperature++
                updateTemperature(tvTemperature)
            }
        }
        findViewById<Button>(R.id.btnDecreaseTemp).setOnClickListener {
            if (currentTemperature > 17) {
                currentTemperature--
                updateTemperature(tvTemperature)
            }
        }

        // Nút bật/tắt điều hòa
        findViewById<Button>(R.id.btnPowerOn).setOnClickListener { sendAcCommand("on") }
        findViewById<Button>(R.id.btnPowerOff).setOnClickListener { sendAcCommand("off") }

        if (isBluetooth) {
            setupBluetoothConnection()
        } else {
            sendAcTypeToESP(selectedAc) // Gửi loại điều hòa khi vào màn hình
        }
    }

    private fun setupBluetoothConnection() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            println("Thiết bị không hỗ trợ Bluetooth")
            finish()
            return
        }

        val deviceAddress = intent.getStringExtra("bluetoothDeviceAddress")
        val device: BluetoothDevice = bluetoothAdapter.getRemoteDevice(deviceAddress)

        // Kiểm tra quyền BLUETOOTH_CONNECT (Android 12+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S &&
            checkSelfPermission(android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
        ) {
            println("Chưa được cấp quyền BLUETOOTH_CONNECT")
            requestPermissions(arrayOf(android.Manifest.permission.BLUETOOTH_CONNECT), 1)
            return
        }

        try {
            bluetoothSocket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"))
            bluetoothSocket?.connect()
            outputStream = bluetoothSocket?.outputStream
            sendAcTypeToESP(selectedAc) // Gửi loại điều hòa qua Bluetooth
        } catch (e: SecurityException) {
            e.printStackTrace()
            println("Quyền Bluetooth bị từ chối")
        } catch (e: IOException) {
            e.printStackTrace()
            println("Không thể kết nối Bluetooth")
            finish()
        }
    }

    private fun updateTemperature(tvTemperature: TextView) {
        tvTemperature.text = "Nhiệt độ: $currentTemperature°C"
        sendAcCommand("set_temp", currentTemperature)
    }

    private fun sendAcTypeToESP(acType: String) {
        val message = "ac_type:$acType"
        if (isBluetooth) {
            sendBluetoothMessage(message)
        } else {
            val url = "http://$esp32Ip/set_ac_type"
            val client = OkHttpClient()
            val requestBody = FormBody.Builder().add("ac_type", acType).build()

            val request = Request.Builder().url(url).post(requestBody).build()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        println("ESP32 đã nhận thông tin loại điều hòa: $acType")
                    }
                }
            })
        }
    }

    private fun sendAcCommand(command: String, temperature: Int? = null) {
        val message = if (command == "set_temp") {
            "command:set_temp,temp:$temperature"
        } else {
            "command:$command"
        }

        if (isBluetooth) {
            sendBluetoothMessage(message)
        } else {
            val url = if (command == "set_temp") {
                "http://$esp32Ip/set_temp?temp=$temperature"
            } else {
                "http://$esp32Ip/$command"
            }

            val client = OkHttpClient()
            val request = Request.Builder().url(url).get().build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        println("Đã gửi lệnh $command thành công")
                    }
                }
            })
        }
    }

    private fun sendBluetoothMessage(message: String) {
        try {
            outputStream?.write("$message\n".toByteArray())
            println("Đã gửi qua Bluetooth: $message")
        } catch (e: IOException) {
            e.printStackTrace()
            println("Không thể gửi dữ liệu qua Bluetooth")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            bluetoothSocket?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}

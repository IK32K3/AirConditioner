package com.example.airconditoner

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class ConnectBluetoothActivity : AppCompatActivity() {

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var pairedDevicesList: Set<BluetoothDevice>
    private lateinit var listView: ListView
    private lateinit var connectButton: Button

    companion object {
        private const val REQUEST_ENABLE_BLUETOOTH = 1
        private const val REQUEST_BLUETOOTH_PERMISSION = 2
    }
    private val REQUEST_BLUETOOTH_PERMISSIONS = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connect_bluetooth)

        listView = findViewById(R.id.listView)
        connectButton = findViewById(R.id.btnConnect)

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Thiết bị không hỗ trợ Bluetooth", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        checkPermissionsAndEnableBluetooth()

        connectButton.setOnClickListener {
            showPairedDevices()
        }
    }

    private fun checkPermissionsAndEnableBluetooth() {
        val permissions = arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN
        )

        val missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missingPermissions.toTypedArray(), REQUEST_BLUETOOTH_PERMISSIONS)
        } else {
            enableBluetooth()
        }
    }

    private fun enableBluetooth() {
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Thiết bị không hỗ trợ Bluetooth", Toast.LENGTH_SHORT).show()
            return
        }

        if (!bluetoothAdapter.isEnabled) {
            // Kiểm tra quyền BLUETOOTH_CONNECT
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLUETOOTH)
            } else {
                // Yêu cầu quyền nếu chưa được cấp
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                    REQUEST_BLUETOOTH_PERMISSION
                )
            }
        }
    }

    private fun showPairedDevices() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Thiếu quyền BLUETOOTH_CONNECT", Toast.LENGTH_SHORT).show()
            return
        }

        pairedDevicesList = bluetoothAdapter.bondedDevices
        val deviceNames = pairedDevicesList.map { "${it.name} - ${it.address}" }

        if (deviceNames.isNotEmpty()) {
            val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, deviceNames)
            listView.adapter = adapter

            listView.setOnItemClickListener { _, _, position, _ ->
                val selectedDevice = pairedDevicesList.elementAt(position)
                connectToDevice(selectedDevice)
            }
        } else {
            Toast.makeText(this, "Không tìm thấy thiết bị Bluetooth nào", Toast.LENGTH_SHORT).show()
        }
    }

    private fun connectToDevice(device: BluetoothDevice) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Thiếu quyền BLUETOOTH_CONNECT để kết nối", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val socket = device.createRfcommSocketToServiceRecord(device.uuids[0].uuid)
            socket.connect()

            Toast.makeText(this, "Kết nối thành công với ${device.name}", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, AcListActivity::class.java)
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Toast.makeText(this, "Không thể kết nối: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                enableBluetooth()
            } else {
                Toast.makeText(this, "Cấp quyền Bluetooth để tiếp tục", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
